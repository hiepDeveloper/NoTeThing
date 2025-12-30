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
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 * Quản lý danh sách ghi chú (Singleton).
 */
public class NoteManager {
    private static NoteManager instance;
    private ObservableList<Note> notes;
    private Stage noteListStage;
    
    // Đường dẫn lưu trữ: UserHome/.notething/notes.dat
    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + ".notething";
    private static final String DATA_FILE = DATA_DIR + File.separator + "notes.dat";

    private NoteManager() {
        notes = FXCollections.observableArrayList();
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
     * Xóa ghi chú và tự động lưu.
     */
    public void removeNote(Note note) {
        notes.remove(note);
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
                note.getX(),
                note.getY(),
                note.getWidth(),
                note.getHeight(),
                isOpen,
                note.isAlwaysOnTop()
            ));
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
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
     * Lấy danh sách Observable các ghi chú.
     */
    public ObservableList<Note> getNotes() {
        return notes;
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
}

