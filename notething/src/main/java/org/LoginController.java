/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 */
package org;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Điều khiển giao diện đăng nhập.
 */
public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginBtn;
    @FXML
    private Button registerBtn;

    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        setLoading(true);
        new Thread(() -> {
            String error = FirebaseAuthManager.getInstance().login(email, password);
            Platform.runLater(() -> {
                setLoading(false);
                if (error == null) {
                    closeWindow();
                    if (onLoginSuccess != null) onLoginSuccess.run();
                } else {
                    errorLabel.setText(error);
                }
            });
        }).start();
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        setLoading(true);
        new Thread(() -> {
            String error = FirebaseAuthManager.getInstance().register(email, password);
            Platform.runLater(() -> {
                setLoading(false);
                if (error == null) {
                    closeWindow();
                    if (onLoginSuccess != null) onLoginSuccess.run();
                } else {
                    errorLabel.setText(error);
                }
            });
        }).start();
    }

    private void setLoading(boolean loading) {
        loginBtn.setDisable(loading);
        registerBtn.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        if (loading) {
            errorLabel.setText("Đang xử lý...");
            errorLabel.setStyle("-fx-text-fill: -note-prompt-color;"); // Màu nhạt hơn khi đang load
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}
