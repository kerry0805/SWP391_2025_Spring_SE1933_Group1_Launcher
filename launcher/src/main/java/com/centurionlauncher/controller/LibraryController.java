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

/**
 * Controller này bây giờ đóng vai trò là Controller chính,
 * quản lý cả layout chính và lưới game.
 */
public class LibraryController implements Initializable {

    // --- FXML Injections (Bao gồm các component từ layout chính và view lưới game)
    // ---
    @FXML
    private BorderPane mainPane; // Component từ layout chính (library.fxml)
    @FXML
    private Label usernameLabel; // Component từ layout chính
    @FXML
    private TilePane gameGrid; // Component được include từ gamegrid-view.fxml
    @FXML
    private Pagination pagination; // Component được include từ gamegrid-view.fxml

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AuthContext.getInstance().isAuthenticated()) {
            usernameLabel.setText(AuthContext.getInstance().getUsername());
            loadLibraryData(0); // Tải dữ liệu cho trang đầu tiên
            setupPagination();
        } else {
            // Xử lý nếu chưa đăng nhập
            usernameLabel.setText("Guest");
            gameGrid.getChildren().add(new Label("Vui lòng đăng nhập để xem thư viện."));
        }
    }

    private void setupPagination() {
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            loadLibraryData(newIndex.intValue());
        });
    }

    @FXML
    void showGameGrid() {
        try {
            // Tải lại file FXML chứa lưới game và phân trang
            Parent gameGridView = FXMLLoader
                    .load(getClass().getResource("/com/centurionlauncher/fxml/gamegrid-view.fxml"));

            this.gameGrid = (TilePane) gameGridView.lookup("#gameGrid");
            this.pagination = (Pagination) gameGridView.lookup("#pagination");

            if (this.gameGrid == null || this.pagination == null) {
                System.err.println("Lỗi: Không tìm thấy #gameGrid hoặc #pagination trong gamegrid-view.fxml!");
                return;
            }

            mainPane.setCenter(gameGridView);

            loadLibraryData(0);
            setupPagination();

        } catch (IOException e) {
            System.err.println("Lỗi khi tải lại gamegrid-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadLibraryData(int pageNumber) {
        gameGrid.getChildren().clear(); // Xóa các game cũ trước khi tải mới

        String apiUrl = String.format("http://localhost:8080/user/library?page=%d&size=12", pageNumber);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(this::processHttpResponse)
                .exceptionally(error -> {
                    error.printStackTrace();
                    Platform.runLater(() -> showError("Lỗi kết nối đến server."));
                    return null;
                });
    }

    private void processHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() == 200) {
            handleLibraryResponse(response.body());
        } else {
            Platform.runLater(() -> {
                showError("Lỗi từ server (Code: " + response.statusCode() + ")");
                System.err.println("Error Body: " + response.body());
            });
        }
    }

    private void handleLibraryResponse(String responseBody) {
        try {
            GamePage pageData = objectMapper.readValue(responseBody, GamePage.class);
            Platform.runLater(() -> {
                pagination.setPageCount(pageData.getTotalPages());
                pagination.setCurrentPageIndex(pageData.getNumber());

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
            Platform.runLater(() -> showError("Lỗi phân tích dữ liệu từ server."));
        }
    }

    /**
     * ✅ Phương thức mới để chuyển sang màn hình chi tiết game.
     * Nó sẽ tải gamedetail-view.fxml và đặt nó vào trung tâm của mainPane.
     * 
     * @param gameId ID của game cần hiển thị.
     */
    public void showGameDetail(long gameId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/centurionlauncher/fxml/gamedetail-view.fxml"));
            Parent view = loader.load();

            GameDetailController controller = loader.getController();
            controller.loadGameDetails(gameId); // Truyền gameId vào controller mới

            // Đặt view mới vào trung tâm của BorderPane
            mainPane.setCenter(view);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải gamedetail-view.fxml");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 16px;");
        gameGrid.getChildren().add(errorLabel);
    }

    private Node createGameCard(Game game) {
        ImageView coverImage = new ImageView();
        coverImage.setFitHeight(220);
        coverImage.setFitWidth(150);
        coverImage.setPreserveRatio(true);
        String imageUrl = game.getHeaderImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            coverImage.setImage(new Image(imageUrl, true));
        }

        Label title = new Label(game.getName());
        title.setFont(new Font("System Bold", 14));
        title.setStyle("-fx-text-fill: white;");
        title.setWrapText(true);
        title.setMaxWidth(150);

        VBox card = new VBox(10, coverImage, title);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        card.getStyleClass().add("game-card");
        card.setCursor(Cursor.HAND);

        // ✅ SỬA LẠI SỰ KIỆN CLICK: Gọi trực tiếp phương thức showGameDetail của lớp
        // này.
        card.setOnMouseClicked(event -> {
            System.out.println("Clicked on game ID: " + game.getGameId());
            showGameDetail(game.getGameId());
        });

        return card;
    }
}
