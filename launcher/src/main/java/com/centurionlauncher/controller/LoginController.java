package com.centurionlauncher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.manager.SceneManager;
import com.centurionlauncher.util.JwtUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField usernameField; // Đảm bảo bạn đã đặt fx:id="usernameField" trong FXML

    @FXML
    private PasswordField passwordField; // Đảm bảo bạn đã đặt fx:id="passwordField" trong FXML

    @FXML
    private Button logInButton;

    // HttpClient được tạo một lần và tái sử dụng
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password cannot be empty.");
            return;
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        logInButton.setDisable(true);
        logInButton.setText("Logging in...");

        try {
            // 1. Tạo đối tượng request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);

            // Chuyển đổi Map thành chuỗi JSON
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

            // 2. Tạo yêu cầu HTTP POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login")) 
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();

            // 3. Gửi yêu cầu bất đồng bộ để không làm đơ UI
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleSuccessfulResponse)
                    .exceptionally(this::handleFailedResponse);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while creating the request.");
            resetLoginButton();
        }
    }

    // Xử lý khi nhận được phản hồi thành công (status 2xx)
    private void handleSuccessfulResponse(String responseBody) {
        Platform.runLater(() -> {
            try {
                TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
                };
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, typeRef);

                if (responseBody.contains("Error") || responseBody.contains("Invalid")) {
                    showAlert("Failed", "Login Failed, check your account");
                } else {
                    showAlert("Success", "Login successful!");
                    String token = (String) responseMap.get("token");
                    if (responseMap.containsKey("token")) {
                        Long userId = JwtUtils.getUserId(token);
                        String username = (String) responseMap.get("username");
                        String role = JwtUtils.getRole(token).toString();
                        System.out.println("User ID: " + userId);
                        System.out.println("Username: " + username);
                        System.out.println("Role: " + role);
                        AuthContext.getInstance().setAuthentication(token, userId, username, role);
                        SceneManager.getInstance().switchToLibrary();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to parse server response.");
            } finally {
                resetLoginButton();
            }
        });
    }

    // Xử lý khi có lỗi (lỗi mạng hoặc server trả về lỗi)
    private Void handleFailedResponse(Throwable throwable) {
        Platform.runLater(() -> {
            // Throwable có thể chứa thông tin về lỗi HTTP
            System.err.println("Login failed: " + throwable.getMessage());
            showAlert("Login Failed", "Invalid username or password, or server is unavailable.");
            resetLoginButton();
        });
        return null;
    }

    // Đặt lại trạng thái của nút login
    private void resetLoginButton() {
        Platform.runLater(() -> {
            logInButton.setDisable(false);
            logInButton.setText("Login");
        });
    }

    // Hiển thị một dialog thông báo đơn giản
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
