package com.centurionlauncher.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.model.Game;

public class LibraryController implements Initializable {



    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label usernameLabel;

    @FXML
    private TilePane gameGrid;

    @FXML
    private Pagination pagination;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private void loadLibraryData() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/user/library"))
                .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleLibraryResponse)
                .join();

    }

    private void handleLibraryResponse(String responseBody) {
        System.out.println(responseBody);
        gameGrid.getChildren().clear();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap = (Map<String, Object>) responseMap;
        if (responseMap.containsKey("gameDetail")) {
            Map<String, Object> gamesMap = (Map<String, Object>) responseMap.get("gameDetail");
            for (Map.Entry<String, Object> entry : gamesMap.entrySet()) {
                
            }
        }
    }
    
    private Node createGameCard(Game game) {
        // Bạn có thể tạo một card phức tạp với ảnh, nút bấm,...
        // Ví dụ đơn giản:
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-border-color: #555; -fx-padding: 10;");

        Label title = new Label(game.getName());
        // ImageView cover = new ImageView(new Image(game.getCoverUrl()));

        card.getChildren().addAll(title /* , cover */);
        return card;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AuthContext.getInstance().isAuthenticated()) {
            String username = AuthContext.getInstance().getUsername();
            usernameLabel.setText(username);
            loadLibraryData();
        }
    }

}
