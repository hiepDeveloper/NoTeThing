/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 */
package org;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Quản lý kết nối và đồng bộ hóa với Firebase.
 */
public class FirebaseManager {
    private static FirebaseManager instance;
    private Firestore db;
    private boolean initialized = false;

    private FirebaseManager() {}

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    /**
     * Khởi tạo Firebase với tệp khóa dịch vụ (Service Account Key).
     * @param serviceAccountPath Đường dẫn đến tệp JSON serviceAccountKey.
     */
    public boolean initialize(String serviceAccountPath) {
        try {
            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            db = FirestoreClient.getFirestore();
            initialized = true;
            System.out.println("Firebase đã được khởi tạo thành công.");
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Firebase: " + e.getMessage());
            return false;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Đồng bộ hóa danh sách ghi chú lên đám mây.
     */
    public void syncToCloud(List<NoteData> notes, String userId) {
        if (!initialized || db == null) return;

        // Lưu từng ghi chú vào collection "notes" của người dùng
        for (NoteData note : notes) {
            db.collection("users").document(userId)
              .collection("notes").document(note.getId())
              .set(note);
        }
        System.out.println("Đã đồng bộ " + notes.size() + " ghi chú lên đám mây.");
    }

    /**
     * Tải danh sách ghi chú từ đám mây.
     */
    public List<NoteData> loadFromCloud(String userId) {
        List<NoteData> notes = new ArrayList<>();
        if (!initialized || db == null) return notes;

        try {
            db.collection("users").document(userId)
              .collection("notes").get().get().getDocuments().forEach(doc -> {
                  notes.add(doc.toObject(NoteData.class));
              });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notes;
    }
}
