/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 */
package org;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Quản lý kết nối và đồng bộ hóa với Firebase qua REST API.
 * Không yêu cầu Service Account Key (an toàn hơn cho Client App).
 */
public class FirebaseManager {
    private static FirebaseManager instance;
    private static final String PROJECT_ID = "notething-600b5"; // ID Project của bạn
    private static final String FIRESTORE_URL = "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private FirebaseManager() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public boolean isInitialized() {
        // Với REST API, luôn sẵn sàng miễn là đã đăng nhập
        return FirebaseAuthManager.getInstance().isLoggedIn();
    }

    /**
     * Đồng bộ hóa danh sách ghi chú lên đám mây (Sử dụng REST API).
     */
    public void syncToCloud(List<NoteData> notes, String userId) {
        String authToken = FirebaseAuthManager.getInstance().getIdToken();
        if (authToken == null) return;

        for (NoteData note : notes) {
            try {
                String url = FIRESTORE_URL + "/users/" + userId + "/notes/" + note.getId() + "?updateMask.fieldPaths=id&updateMask.fieldPaths=title&updateMask.fieldPaths=content&updateMask.fieldPaths=richContent&updateMask.fieldPaths=x&updateMask.fieldPaths=y&updateMask.fieldPaths=width&updateMask.fieldPaths=height&updateMask.fieldPaths=isOpen&updateMask.fieldPaths=isAlwaysOnTop&updateMask.fieldPaths=color&updateMask.fieldPaths=opacity&updateMask.fieldPaths=isDeleted";
                
                String jsonBody = convertToFirestoreJson(note);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        // System.out.println("Sync result for " + note.getId() + ": " + response);
                    });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Đang đồng bộ " + notes.size() + " ghi chú lên đám mây...");
    }

    /**
     * Tải danh sách ghi chú từ đám mây.
     */
    public List<NoteData> loadFromCloud(String userId) {
        List<NoteData> notes = new ArrayList<>();
        String authToken = FirebaseAuthManager.getInstance().getIdToken();
        if (authToken == null) return notes;

        try {
            String url = FIRESTORE_URL + "/users/" + userId + "/notes?pageSize=100";
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("documents")) {
                    for (JsonNode doc : root.get("documents")) {
                        NoteData note = convertFromFirestoreJson(doc);
                        if (note != null) {
                            notes.add(note);
                        }
                    }
                }
            } else {
                System.err.println("Load failed: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return notes;
    }

    // Helper: NoteData -> Firestore JSON
    private String convertToFirestoreJson(NoteData note) throws JsonProcessingException {
        ObjectNode fields = objectMapper.createObjectNode();
        
        fields.set("id", stringField(note.getId()));
        fields.set("title", stringField(note.getTitle()));
        fields.set("content", stringField(note.getContent()));
        fields.set("richContent", stringField(note.getRichContent())); // Giả sử richContent là chuỗi
        fields.set("x", doubleField(note.getX()));
        fields.set("y", doubleField(note.getY()));
        fields.set("width", doubleField(note.getWidth()));
        fields.set("height", doubleField(note.getHeight()));
        fields.set("isOpen", booleanField(note.isOpen()));
        fields.set("isAlwaysOnTop", booleanField(note.isAlwaysOnTop()));
        fields.set("color", stringField(note.getColor()));
        fields.set("opacity", doubleField(note.getOpacity()));
        fields.set("isDeleted", booleanField(note.isDeleted()));

        ObjectNode root = objectMapper.createObjectNode();
        root.set("fields", fields);
        return objectMapper.writeValueAsString(root);
    }

    // Helper: Firestore JSON -> NoteData
    private NoteData convertFromFirestoreJson(JsonNode doc) {
        try {
            JsonNode fields = doc.get("fields");
            if (fields == null) return null;

            String id = getString(fields, "id");
            String title = getString(fields, "title");
            String content = getString(fields, "content");
            String richContent = getString(fields, "richContent");
            double x = getDouble(fields, "x");
            double y = getDouble(fields, "y");
            double width = getDouble(fields, "width");
            double height = getDouble(fields, "height");
            boolean isOpen = getBoolean(fields, "isOpen");
            boolean isAlwaysOnTop = getBoolean(fields, "isAlwaysOnTop");
            String color = getString(fields, "color");
            double opacity = getDouble(fields, "opacity");
            boolean isDeleted = getBoolean(fields, "isDeleted");

            return new NoteData(id, title, content, richContent, x, y, width, height, isOpen, isAlwaysOnTop, color, opacity, isDeleted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Type Helpers
    private ObjectNode stringField(String val) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("stringValue", val == null ? "" : val);
        return node;
    }
    
    private ObjectNode doubleField(double val) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("doubleValue", val);
        return node;
    }
    
    private ObjectNode booleanField(boolean val) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("booleanValue", val);
        return node;
    }

    private String getString(JsonNode fields, String key) {
        return fields.has(key) && fields.get(key).has("stringValue") ? fields.get(key).get("stringValue").asText() : "";
    }
    
    private double getDouble(JsonNode fields, String key) {
        return fields.has(key) && fields.get(key).has("doubleValue") ? fields.get(key).get("doubleValue").asDouble() : 0.0;
    }
    
    private boolean getBoolean(JsonNode fields, String key) {
        return fields.has(key) && fields.get(key).has("booleanValue") && fields.get(key).get("booleanValue").asBoolean();
    }
}
