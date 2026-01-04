/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 */
package org;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Quản lý xác thực người dùng qua Firebase REST API.
 */
public class FirebaseAuthManager {
    private static final String API_KEY;
    
    static {
        // Ưu tiên load từ file config.properties trong resources (được đóng gói trong JAR)
        String apiKey = null;
        try (java.io.InputStream input = FirebaseAuthManager.class.getResourceAsStream("/org/config.properties")) {
            if (input != null) {
                java.util.Properties prop = new java.util.Properties();
                prop.load(input);
                apiKey = prop.getProperty("firebase_api_key");
            }
        } catch (IOException ex) {
            System.err.println("Không thể load config.properties: " + ex.getMessage());
        }

        // Nếu không có trong resources (môi trường dev), thử load từ .env
        if (apiKey == null || apiKey.isEmpty()) {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
            apiKey = dotenv.get("FIREBASE_API_KEY", "YOUR_API_KEY_HERE");
        }
        
        API_KEY = apiKey;
    }
    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + ".notething";
    private static final String AUTH_FILE = DATA_DIR + File.separator + "auth.json";
    private static FirebaseAuthManager instance;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private String idToken;
    private String localId;
    private String email;

    private FirebaseAuthManager() {
        new File(DATA_DIR).mkdirs(); // Đảm bảo thư mục tồn tại
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        loadSession();
    }

    public static FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }

    /**
     * @return null nếu thành công, hoặc chuỗi thông báo lỗi nếu thất bại.
     */
    public String login(String email, String password) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());

            if (response.statusCode() == 200) {
                this.idToken = node.get("idToken").asText();
                this.localId = node.get("localId").asText();
                this.email = node.get("email").asText();
                saveSession();
                return null;
            } else {
                String error = node.get("error").get("message").asText();
                return translateError(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi kết nối: " + e.getMessage();
        }
    }

    public String register(String email, String password) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());

            if (response.statusCode() == 200) {
                return login(email, password);
            } else {
                String error = node.get("error").get("message").asText();
                return translateError(error);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Lỗi kết nối: " + e.getMessage();
        }
    }

    private String translateError(String firebaseError) {
        if (firebaseError == null || firebaseError.isEmpty()) return "Lỗi không xác định từ Firebase.";
        
        // Debug: In ra lỗi thực tế để kiểm tra
        System.err.println("Firebase API Error: " + firebaseError);
        
        // Firebase message thường có định dạng "ERROR_CODE : [Info]"
        String errorCode = firebaseError.split(":")[0].trim();
        
        switch (errorCode) {
            case "EMAIL_EXISTS": return "Email này đã được đăng ký.";
            case "INVALID_EMAIL": return "Địa chỉ email không hợp lệ.";
            case "EMAIL_NOT_FOUND":
            case "INVALID_PASSWORD":
            case "INVALID_LOGIN_CREDENTIALS": return "Email hoặc mật khẩu không đúng.";
            case "USER_DISABLED": return "Tài khoản này đã bị khóa.";
            case "WEAK_PASSWORD": return "Mật khẩu phải có ít nhất 6 ký tự.";
            case "TOO_MANY_ATTEMPTS_TRY_LATER": return "Quá nhiều lần thử. Vui lòng thử lại sau.";
            default: return "Lỗi Firebase: " + errorCode;
        }
    }

    public boolean isLoggedIn() {
        return idToken != null;
    }

    public String getLocalId() {
        return localId;
    }

    public String getEmail() {
        return email;
    }

    public String getIdToken() {
        return idToken;
    }

    public void logout() {
        this.idToken = null;
        this.localId = null;
        this.email = null;
        clearSession();
    }

    private void saveSession() {
        if (idToken == null) return;
        
        java.util.Map<String, String> sessionData = new java.util.HashMap<>();
        sessionData.put("idToken", idToken);
        sessionData.put("localId", localId);
        sessionData.put("email", email);
        
        try {
            objectMapper.writeValue(new File(AUTH_FILE), sessionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSession() {
        File file = new File(AUTH_FILE);
        if (!file.exists()) return;

        try {
            JsonNode node = objectMapper.readTree(file);
            this.idToken = node.has("idToken") ? node.get("idToken").asText() : null;
            this.localId = node.has("localId") ? node.get("localId").asText() : null;
            this.email = node.has("email") ? node.get("email").asText() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearSession() {
        File file = new File(AUTH_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
