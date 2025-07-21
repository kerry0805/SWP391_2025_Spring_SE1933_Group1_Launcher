package com.centurionlauncher.controller;

import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.model.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Controller này quản lý màn hình chi tiết của một game.
 */
public class GameDetailController {

    // --- FXML Injections ---
    @FXML
    private StackPane rootPane;
    @FXML
    private BorderPane mainContentPane;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label gameTitleLabel;
    @FXML
    private Label gameDescriptionLabel;
    @FXML
    private ImageView gameHeaderImageView;
    @FXML
    private Button playButton;
    @FXML
    private Label lastPlayedLabel;
    @FXML
    private Label playTimeLabel;
    @FXML
    private Label myReviewLabel;

    // --- Dependencies & State ---
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Game currentGame; // Lưu trữ thông tin của game đang được hiển thị

    /**
     * Phương thức này được gọi từ MainLayoutController để bắt đầu quá trình tải dữ
     * liệu.
     * 
     * @param gameId ID của game cần hiển thị chi tiết.
     */
    public void loadGameDetails(long gameId) {
        // Đảm bảo vòng xoay tải đang hiển thị và nội dung chính bị ẩn
        loadingIndicator.setVisible(true);
        mainContentPane.setVisible(false);

        // Tạo một Task để thực hiện cuộc gọi API trên luồng nền
        Task<Game> fetchGameTask = new Task<>() {
            @Override
            protected Game call() throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/games/" + gameId))
                        .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new IOException("Lỗi khi gọi API: " + response.statusCode() + " " + response.body());
                }
                return objectMapper.readValue(response.body(), Game.class);
            }
        };

        // Xử lý khi task thành công
        fetchGameTask.setOnSucceeded(event -> {
            Game fetchedGame = fetchGameTask.getValue();
            Platform.runLater(() -> updateUI(fetchedGame));
        });

        // Xử lý khi task thất bại
        fetchGameTask.setOnFailed(event -> {
            fetchGameTask.getException().printStackTrace();
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                // Hiển thị thông báo lỗi thay vì nội dung chính
                rootPane.getChildren().add(new Label("Lỗi: Không thể tải chi tiết game."));
            });
        });

        new Thread(fetchGameTask).start();
    }

    /**
     * Cập nhật các thành phần giao diện với dữ liệu từ đối tượng Game.
     * 
     * @param game Đối tượng Game chứa thông tin chi tiết.
     */
    private void updateUI(Game game) {
        this.currentGame = game;

        // Ẩn vòng xoay tải và hiển thị nội dung chính
        loadingIndicator.setVisible(false);
        mainContentPane.setVisible(true);

        // Cập nhật các thành phần giao diện
        gameTitleLabel.setText(game.getName());
        gameDescriptionLabel.setText(game.getShortDescription());

        String imageUrl = game.getHeaderImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            gameHeaderImageView.setImage(new Image(imageUrl, true));
        }

        // TODO: Cập nhật thông tin lastPlayedLabel từ dữ liệu thực tế
        // lastPlayedLabel.setText(...);

        // Cập nhật thời gian chơi
        // long playtimeMillis = game.getPlaytimeInMillis(); // Giả sử có trường này
        // long hours = playtimeMillis / 3_600_000;
        // double minutes = (playtimeMillis % 3_600_000) / 60_000.0;
        // playTimeLabel.setText(String.format("%d.%.1f hours", hours, minutes / 10.0));
        // myReviewLabel.setText(String.format("You've played for %d.%.1f hours", hours,
        // minutes / 10.0));

        // TODO: Kiểm tra xem game đã được cài đặt chưa để thay đổi nút Play/Install
    }

    /**
     * Xử lý sự kiện khi nhấn nút Play/Install.
     */
    @FXML
    private void handlePlayButtonAction() {
        if (currentGame == null) {
            System.err.println("Chưa có thông tin game để khởi chạy.");
            return;
        }
        String stubExecutablePath = "D:\\Undertale\\UNDERTALE.exe"; // Tạm thời hardcode
        try {
            launchGame(stubExecutablePath);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Hiển thị dialog báo lỗi cho người dùng
        }
    }

    /**
     * Khởi chạy file thực thi của game và tính toán thời gian chơi.
     * 
     * @param executablePath Đường dẫn đến file .exe của game.
     */
    private void launchGame(String executablePath) throws IOException {
        System.out.println("Đang khởi chạy: " + executablePath);
        ProcessBuilder pb = new ProcessBuilder(executablePath);
        File gameDirectory = new File(executablePath).getParentFile();
        if (gameDirectory != null && gameDirectory.exists()) {
            pb.directory(gameDirectory);
        }
        Instant startTime = Instant.now();
        Process process = pb.start();
        process.onExit().thenAccept(exitedProcess -> {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
            long playtimeMillis = duration.toMillis();
            System.out.println("Game đã đóng. Thời gian chơi: " + playtimeMillis + " ms.");
            // TODO: Gọi một service để gửi thời gian chơi này về backend
        });
    }
}
