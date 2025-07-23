package com.centurionlauncher.controller;

import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.model.Game;
import com.centurionlauncher.model.GamePage;
import com.centurionlauncher.model.LibraryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
 * Main Library Controller
 * manage layout and game grid
 */
public class LibraryController implements Initializable {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Label usernameLabel;
    @FXML
    private TilePane gameGrid;
    @FXML
    private Pagination pagination;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AuthContext.getInstance().isAuthenticated()) {
            usernameLabel.setText(AuthContext.getInstance().getUsername());
            loadLibraryData(0);
            setupPagination();
        } else {
            // Xử lý nếu chưa đăng nhập
            usernameLabel.setText("Guest");
            gameGrid.getChildren().add(new Label("Please login to view your library."));
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
            Parent gameGridView = FXMLLoader
                    .load(getClass().getResource("/com/centurionlauncher/fxml/gamegrid-view.fxml"));

            this.gameGrid = (TilePane) gameGridView.lookup("#gameGrid");
            this.pagination = (Pagination) gameGridView.lookup("#pagination");

            if (this.gameGrid == null || this.pagination == null) {
                System.err.println("no game grid or pagination in gamegrid-view.fxml");
                return;
            }

            mainPane.setCenter(gameGridView);

            loadLibraryData(0);
            setupPagination();

        } catch (IOException e) {
            System.err.println("error reloading gamegrid-view.fxml");
            e.printStackTrace();
        }
    }

    private void loadLibraryData(int pageNumber) {
        gameGrid.getChildren().clear();

        String apiUrl = String.format(
                "https://swp3912025springse1933group1backend-production.up.railway.app/user/library?page=%d&size=12",
                pageNumber);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(this::processHttpResponse)
                .exceptionally(error -> {
                    error.printStackTrace();
                    Platform.runLater(() -> showError("Error load api library data"));
                    return null;
                });
    }

    private void processHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() == 200) {
            handleLibraryResponse(response.body());
        } else {
            Platform.runLater(() -> {
                showError("Err server (Code: " + response.statusCode() + ")");
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
            Platform.runLater(() -> showError("Error hanlde lib response"));
        }
    }

    /**
     *
     * @param gameId
     */
    public void showGameDetail(long gameId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/centurionlauncher/fxml/gamedetail-view.fxml"));
            Parent view = loader.load();

            GameDetailController controller = loader.getController();
            controller.loadGameDetails(gameId);

            mainPane.setCenter(view);

        } catch (IOException e) {
            System.err.println("Error loading gamedetail-view.fxml");
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
        } else {
            coverImage
                    .setImage(new Image("https://upload.wikimedia.org/wikipedia/commons/7/75/No_image_available.png"));
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

        card.setOnMouseClicked(event -> {
            System.out.println("Clicked on game ID: " + game.getGameId());
            showGameDetail(game.getGameId());
        });

        return card;
    }
}
