package com.centurionlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.application.Application;
import java.awt.AWTException;
import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;

import com.centurionlauncher.controller.GameDetailController;
import com.centurionlauncher.manager.ProcessManager;
import com.centurionlauncher.manager.SceneManager;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private TrayIcon trayIcon;
    private GameDetailController gameDetailController;

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.getInstance().initialize(stage);
        Platform.setImplicitExit(false);
        javax.swing.SwingUtilities.invokeLater(() -> {
            createTrayIcon(stage);
        });
        stage.setOnCloseRequest(e -> {
            hide(stage);
            e.consume();
        });
        SceneManager.getInstance().switchToLogin();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private void createTrayIcon(Stage stage) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Exit");
            MenuItem showItem = new MenuItem("Open");
            showItem.addActionListener(e -> Platform.runLater(() -> show(stage)));
            exitItem.addActionListener(e -> {
                // kill all process trước khi thoát launcher
                ProcessManager.killAllProcesses();
                Platform.exit();
                System.exit(0);
            });
            popup.add(exitItem);
            popup.add(showItem);
            try {
                URL imageUrl = App.class.getResource("/com/centurionlauncher/icons/logo.png");
                if (imageUrl == null) {
                    throw new IOException("no icon");
                }
                java.awt.Image image = ImageIO.read(imageUrl);
                trayIcon = new TrayIcon(image, "Centurion Launcher", popup);
                trayIcon.setImageAutoSize(true);
                trayIcon.addActionListener(e -> Platform.runLater(() -> show(stage)));
                tray.add(trayIcon);

            } catch (AWTException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void hide(Stage stage) {
        stage.hide();
    }

    private void show(Stage stage) {
        stage.show();
        stage.toFront();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/centurionlauncher/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }

}