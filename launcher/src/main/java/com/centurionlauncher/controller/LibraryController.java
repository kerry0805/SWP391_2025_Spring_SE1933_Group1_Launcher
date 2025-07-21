package com.centurionlauncher.controller;

import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.model.Game;
import com.centurionlauncher.model.GamePage;
import com.centurionlauncher.model.LibraryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

public class LibraryController implements Initializable {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label usernameLabel;
    @FXML
    private TilePane gameGrid;
    @FXML
    private Pagination pagination;

    // Sử dụng một instance duy nhất cho HttpClient và ObjectMapper
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AuthContext.getInstance().isAuthenticated()) {
            usernameLabel.setText(AuthContext.getInstance().getUsername());
            // Bắt đầu tải dữ liệu ngay khi màn hình được khởi tạo
            loadLibraryData();
        } else {
            // Xử lý trường hợp người dùng chưa đăng nhập
            usernameLabel.setText("Guest");
            // Có thể hiển thị một thông báo yêu cầu đăng nhập
        }
    }

    private void loadLibraryData() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/library")) // Thay đổi URL nếu cần
                .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                .build();

        // Gửi request bất đồng bộ và KHÔNG block luồng UI với .join()
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleLibraryResponse) // Xử lý response
                .exceptionally(error -> { // Xử lý lỗi nếu có
                    error.printStackTrace();
                    Platform.runLater(() -> {
                        Label errorLabel = new Label("Error fetching games");
                        errorLabel.setStyle(
                                "-fx-text-fill: #FF6B6B;" + "-fx-font-size: 16px;" + "-fx-font-weight: bold;");
                        gameGrid.getChildren().add(errorLabel);
                    });
                    return null;
                });

    }

    private void handleLibraryResponse(String responseBody) {
        try {
            loadView("gamegrid-view");
            // Phân tích chuỗi JSON thành đối tượng GamePage
            GamePage pageData = objectMapper.readValue(responseBody, GamePage.class);

            // Mọi thao tác cập nhật giao diện phải được thực hiện trên luồng JavaFX
            Platform.runLater(() -> {
                // Cấu hình thanh phân trang
                pagination.setPageCount(pageData.getTotalPages());
                // TODO: Thêm logic xử lý khi người dùng nhấn vào trang khác

                // Xóa dữ liệu cũ và hiển thị dữ liệu mới
                gameGrid.getChildren().clear();
                for (LibraryEntry entry : pageData.getContent()) {
                    Game game = entry.getGameDetail();
                    if (game != null) {
                        Node gameCard = createGameCard(game);
                        gameGrid.getChildren().add(gameCard);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Label errorLabel = new Label("Error fetching games");
                errorLabel.setStyle(
                        "-fx-text-fill: #FF6B6B;" + "-fx-font-size: 16px;" + "-fx-font-weight: bold;");
                gameGrid.getChildren().add(errorLabel);
            });
        }
    }

    @FXML
    private BorderPane mainPane;

    @FXML
    void showGameGrid() {
        loadView("gamegrid-view");
    }

    public void showGameDetail(long gameId) {
        try {
            // 1. Tạo FXMLLoader
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/centurionlauncher/fxml/gamedetail-view.fxml"));

            // 2. Tải FXML
            Parent view = loader.load();

            GameDetailController controller = loader.getController();

            controller.loadGameDetail(gameId);

            mainPane.setCenter(view);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải gamedetail-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadView(String fxml) {
        try {
            URL fileUrl = getClass().getResource("/com/centurionlauncher/fxml/" + fxml + ".fxml");
            if (fileUrl == null) {
                throw new java.io.FileNotFoundException("Không tìm thấy file: " + fxml);
            }
            Parent view = FXMLLoader.load(fileUrl);
            mainPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Lỗi khi tải view: " + fxml);
            e.printStackTrace();
        }
    }

    private Node createGameCard(Game game) {
        // Ảnh bìa
        ImageView coverImage = new ImageView();
        coverImage.setFitHeight(440);
        coverImage.setFitWidth(300);
        coverImage.setPreserveRatio(true);
        String imageUrl = game.getHeaderImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            coverImage.setImage(new Image(imageUrl, true));
        } else {
        }

        // Tên game
        Label title = new Label(game.getName());
        title.setFont(new Font("System Bold", 14));
        title.setStyle("-fx-text-fill: white;");
        title.setWrapText(true);
        title.setMaxWidth(150);

        // Card container
        VBox card = new VBox(10, coverImage, title);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        card.getStyleClass().add("game-card");
        card.setCursor(Cursor.HAND);

        card.setOnMouseClicked(event -> {
            System.out.println("Clicked on game ID: " + game.getGameId());
            if (mainLayoutController != null) {
                mainLayoutController.showGameDetail(game.getGameId());
            } else {
                System.err.println("MainLayoutController chưa được thiết lập!");
            }
        });

        return card;
    }
}