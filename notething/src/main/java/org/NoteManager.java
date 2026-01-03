/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.Stage;

/**
 * Quản lý danh sách ghi chú (Singleton).
 */
public class NoteManager {
    private static NoteManager instance;
    private ObservableList<Note> notes;
    private FilteredList<Note> visibleNotes;
    private FilteredList<Note> deletedNotes;
    private Stage noteListStage;
    
    // Đường dẫn lưu trữ: UserHome/.notething/notes.dat
    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + ".notething";
    private static final String DATA_FILE = DATA_DIR + File.separator + "notes.dat";
    private static final String SETTINGS_FILE = DATA_DIR + File.separator + "settings.json";
    private final ObjectMapper objectMapper;

    private NoteManager() {
        // Sử dụng extractor để theo dõi thay đổi của thuộc tính deleted (và các thuộc tính khác nếu cần)
        // Điều này giúp FilteredList tự động cập nhật khi thuộc tính thay đổi
        notes = FXCollections.observableArrayList(note -> 
            new javafx.beans.Observable[] { note.deletedProperty() }
        );
        
        visibleNotes = new FilteredList<>(notes, n -> !n.isDeleted());
        deletedNotes = new FilteredList<>(notes, n -> n.isDeleted());
        
        objectMapper = new ObjectMapper();
        new File(DATA_DIR).mkdirs();
    }

    public static NoteManager getInstance() {
        if (instance == null) {
            instance = new NoteManager();
        }
        return instance;
    }

    /**
     * Thêm ghi chú và tự động lưu.
     */
    public void addNote(Note note) {
        notes.add(note);
        saveNotes();
    }

    /**
     * Xóa vĩnh viễn ghi chú và tự động lưu.
     */
    public void hardDeleteNote(Note note) {
        if (note.getStage() != null) {
            note.getStage().close();
        }
        notes.remove(note);
        saveNotes();
    }
    
    /**
     * Chuyển ghi chú vào thùng rác (Soft Delete).
     */
    public void softDeleteNote(Note note) {
        note.setDeleted(true);
        if (note.getStage() != null) {
            note.getStage().close();
        }
        saveNotes();
    }
    
    /**
     * Khôi phục ghi chú từ thùng rác.
     */
    public void restoreNote(Note note) {
        note.setDeleted(false);
        saveNotes();
    }
    
    /**
     * Làm sạch thùng rác (Xóa tất cả ghi chú đã đánh dấu xóa).
     */
    public void emptyTrash() {
        List<Note> trash = new ArrayList<>(deletedNotes); // Copy list to avoid concurrent mod
        notes.removeAll(trash);
        saveNotes();
    }

    /**
     * Lưu danh sách ghi chú xuống file.
     */
    public void saveNotes() {
        List<NoteData> dataList = new ArrayList<>();
        for (Note note : notes) {
            boolean isOpen = (note.getStage() != null && note.getStage().isShowing());
            dataList.add(new NoteData(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getRichContent(),
                note.getX(),
                note.getY(),
                note.getWidth(),
                note.getHeight(),
                isOpen,
                note.isAlwaysOnTop(),
                note.getColor(),
                note.getOpacity(),
                note.isDeleted() // Lưu trạng thái deleted
            ));
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        App.triggerSyncCloud();
    }

    /**
     * Tải danh sách ghi chú từ file.
     */
    @SuppressWarnings("unchecked")
    public List<NoteData> loadNotes() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new ArrayList<>();
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<NoteData>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Nếu lỗi đọc file (do thay đổi version class NoteData), trả về list rỗng hoặc backup
            System.err.println("Không thể đọc file dữ liệu cũ: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tìm ghi chú theo Stage.
     */
    public Note findNoteByStage(Stage stage) {
        return notes.stream()
            .filter(n -> n.getStage() == stage)
            .findFirst()
            .orElse(null);
    }

    /**
     * Tìm ghi chú theo ID.
     */
    public Note findNoteById(String id) {
        return notes.stream()
            .filter(n -> n.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Lấy danh sách gốc (Tất cả ghi chú bao gồm đã xóa).
     * Chỉ dùng nội bộ hoặc cho Sync. Dùng getVisibleNotes() cho UI chính.
     */
    public ObservableList<Note> getAllNotes() {
        return notes;
    }

    /**
     * @deprecated Sử dụng getAllNotes() hoặc getVisibleNotes() tùy mục đích.
     */
    @Deprecated
    public ObservableList<Note> getNotes() {
        return notes;
    }
    
    /**
     * Lấy danh sách ghi chú đang hiển thị (chưa xóa).
     */
    public FilteredList<Note> getVisibleNotes() {
        return visibleNotes;
    }
    
    /**
     * Lấy danh sách ghi chú trong thùng rác.
     */
    public FilteredList<Note> getDeletedNotes() {
        return deletedNotes;
    }

    /**
     * Đóng và lưu tất cả ghi chú.
     */
    public void closeAllNotes() {
        saveNotes();
        new ArrayList<>(notes).forEach(note -> {
            if (note.getStage() != null) note.getStage().close();
        });
        notes.clear();
    }

    public Stage getNoteListStage() {
        return noteListStage;
    }

    public void setNoteListStage(Stage noteListStage) {
        this.noteListStage = noteListStage;
    }

    /**
     * Lấy tên mặc định cho ghi chú mới (New Note N).
     */
    public String getNextDefaultTitle() {
        int count = 1;
        String title;
        do {
            title = "New Note " + count++;
        } while (isTitleExists(title));
        return title;
    }

    private boolean isTitleExists(String title) {
        return notes.stream().anyMatch(n -> n.getTitle() != null && n.getTitle().equalsIgnoreCase(title));
    }

    /**
     * Lưu cài đặt ứng dụng.
     */
    public void saveSettings(String themeMode, boolean glassEffect, boolean autoHideTitle, int fontSize, boolean cloudSync) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("themeMode", themeMode);
        settings.put("glassEffect", glassEffect);
        settings.put("autoHideTitle", autoHideTitle);
        settings.put("fontSize", fontSize);
        settings.put("cloudSync", cloudSync);

        try {
            objectMapper.writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonNode loadSettingsNode() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try {
                return objectMapper.readTree(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String loadThemeMode() {
        JsonNode node = loadSettingsNode();
        if (node != null && node.has("themeMode")) {
            return node.get("themeMode").asText();
        }
        return "SYSTEM";
    }

    public boolean loadGlassEffect() {
        JsonNode node = loadSettingsNode();
        return node == null || !node.has("glassEffect") || node.get("glassEffect").asBoolean();
    }

    public boolean loadAutoHideTitle() {
        JsonNode node = loadSettingsNode();
        return node == null || !node.has("autoHideTitle") || node.get("autoHideTitle").asBoolean();
    }

    public int loadFontSize() {
        JsonNode node = loadSettingsNode();
        return (node != null && node.has("fontSize")) ? node.get("fontSize").asInt() : 18;
    }

    public boolean loadCloudSync() {
        JsonNode node = loadSettingsNode();
        return node != null && node.has("cloudSync") && node.get("cloudSync").asBoolean();
    }
}
