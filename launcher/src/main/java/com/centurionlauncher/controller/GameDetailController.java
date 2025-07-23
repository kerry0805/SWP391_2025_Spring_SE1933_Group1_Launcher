package com.centurionlauncher.controller;

import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.model.Game;
import com.centurionlauncher.model.LibraryEntry; // Sử dụng LibraryEntry để khớp với DTO
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

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
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private Game currentGame;
    private LibraryEntry currentLibraryEntry;
    private LibraryController libraryController;

    public void setLibraryController(LibraryController libraryController) {
        this.libraryController = libraryController;
    }

    public void loadGameDetails(long gameId) {
        loadingIndicator.setVisible(true);
        mainContentPane.setVisible(false);

        Task<LibraryEntry> fetchGameTask = new Task<>() {
            @Override
            protected LibraryEntry call() throws Exception {
                Long userId = AuthContext.getInstance().getUserId();
                if (userId == null) {
                    throw new IllegalStateException("User ID is not available in AuthContext.");
                }
                String apiUrl = String.format("http://localhost:8080/user/library/%d/%d", userId, gameId);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("API Response: " + response.body());

                if (response.statusCode() != 200) {
                    throw new IOException("Lỗi khi gọi API: " + response.statusCode() + " - " + response.body());
                }
                return objectMapper.readValue(response.body(), LibraryEntry.class);
            }
        };

        fetchGameTask.setOnSucceeded(event -> {
            LibraryEntry fetchedEntry = fetchGameTask.getValue();
            Platform.runLater(() -> updateUI(fetchedEntry));
        });

        fetchGameTask.setOnFailed(event -> {
            fetchGameTask.getException().printStackTrace();
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                rootPane.getChildren()
                        .add(new Label("Error fetch game task: " + fetchGameTask.getException().getMessage()));
            });
        });

        new Thread(fetchGameTask).start();
    }

    @FXML
    private void handleBackToLibrary() {
        if (libraryController != null) {
            libraryController.showGameGrid();
        } else {
            System.err.println("LibraryController is null. Cannot navigate back.");
        }
    }

    private void updateUI(LibraryEntry libraryEntry) {
        this.currentLibraryEntry = libraryEntry;
        this.currentGame = libraryEntry.getGameDetail();

        if (this.currentGame == null) {
            loadingIndicator.setVisible(false);
            rootPane.getChildren().add(new Label("Invalid data"));
            return;
        }

        loadingIndicator.setVisible(false);
        mainContentPane.setVisible(true);

        gameTitleLabel.setText(currentGame.getName());
        gameDescriptionLabel.setText(currentGame.getShortDescription());

        String imageUrl = currentGame.getHeaderImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            gameHeaderImageView.setImage(new Image(imageUrl, true));
        }
        long playtimeMillis = libraryEntry.getPlaytimeInMillis();
        long hour = playtimeMillis / 3_600_000;
        long minute = (playtimeMillis % 3_600_000) / 60_000;
        String playtimeText = String.format("%d hours %d minutes", hour, minute);

        playTimeLabel.setText(playtimeText);
        myReviewLabel.setText("You've played for " + playtimeText);
        LocalDateTime lastPlayedTime = libraryEntry.getlastTimePlayed();
        if (LocalDateTime.now().compareTo(lastPlayedTime) > 0) {
            Duration duration = Duration.between(lastPlayedTime, LocalDateTime.now());
            String lastPlayedText;

            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;

            if (days > 30) {
                lastPlayedText = "Last played: long time ago";
            } else if (days > 0) {
                lastPlayedText = String.format("Last played: %d days ago", days);
            } else if (hours > 0) {
                lastPlayedText = String.format("Last played: %d hours ago", hours);
            } else if (minutes > 0) {
                lastPlayedText = String.format("Last played: %d minutes ago", minutes);
            } else {
                lastPlayedText = "Last played: just now";
            }

            lastPlayedLabel.setText(lastPlayedText);
        }
    }

    @FXML
    private void handlePlayButtonAction() {
        if (currentGame == null) {
            System.err.println("Chưa có thông tin game để khởi chạy.");
            return;
        }

        String stubExecutablePath;

        if ("Yume Nikki".equals(currentGame.getName())) {
            stubExecutablePath = "E:\\SteamLibrary\\steamapps\\common\\Yume Nikki\\yumenikki\\RPG_RT.exe";
        } else {
            stubExecutablePath = "C:\\Program Files (x86)\\Microsoft Games\\Age of Empires\\Empires.exe";
        }

        try {
            launchGame(stubExecutablePath, currentGame.getGameId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePlaytimeInLibrary(long playtimeMillis, long gameId) throws IOException, InterruptedException {
        String apiUrl = String.format("http://localhost:8080/user/library/%d/playtime", gameId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(String.valueOf(playtimeMillis)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.out.println("Failed to update playtime: " + response.body());
            return;
        }

    }

    private void launchGame(String executablePath, long gameId) throws IOException {
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
            System.out.println("Game closed. Time played: " + playtimeMillis + " ms.");
            try {
                updatePlaytimeInLibrary(playtimeMillis, gameId);
                System.out.println("Api reach successfully.");
            } catch (IOException | InterruptedException e) {
                System.out.println("deo dc");
                e.printStackTrace();
            }

        });
    }

}
