package com.centurionlauncher.manager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private Scene mainScene;

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

    public void switchToLogin() {
        loadScene("/com/centurionlauncher/fxml/login.fxml", "Đăng nhập");
    }

    public void switchToLibrary() {
        loadScene("/com/centurionlauncher/fxml/library.fxml", "Thư viện Game");
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            primaryStage.setTitle(title);

            if (mainScene == null) {
                mainScene = new Scene(root);
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
            }

            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
