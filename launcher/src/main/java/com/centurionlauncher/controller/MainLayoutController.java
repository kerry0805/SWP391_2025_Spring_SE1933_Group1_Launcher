package com.centurionlauncher.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller này quản lý layout chính của ứng dụng (chứa BorderPane).
 * Nhiệm vụ của nó là tải và chuyển đổi các view con vào khu vực trung tâm.
 */
public class MainLayoutController implements Initializable {

    // Inject BorderPane từ file MainLayout.fxml
    @FXML
    private BorderPane mainPane;

    /**
     * Phương thức này được gọi tự động khi MainLayout.fxml được tải.
     * Nó sẽ tải view mặc định là lưới game.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showGameGrid(); // Tải màn hình thư viện làm màn hình mặc định
    }

    /**
     * Tải và hiển thị view lưới game (gamegrid-view.fxml) vào trung tâm.
     */
    @FXML
    void showGameGrid() {
        // Tải view và truyền một tham chiếu của chính nó (this) vào LibraryController
        loadViewAndSetupControllers("/com/centurionlauncher/fxml/gamegrid-view.fxml", -1); // -1 là ID không hợp lệ, chỉ
                                                                                           // để phân biệt
    }

    /**
     * Phương thức này được gọi bởi LibraryController để hiển thị chi tiết game.
     * 
     * @param gameId ID của game cần hiển thị.
     */
    public void showGameDetail(long gameId) {
        System.out.println("MainLayoutController: Nhận yêu cầu hiển thị chi tiết cho game ID: " + gameId);
        loadViewAndSetupControllers("/com/centurionlauncher/fxml/gamedetail-view.fxml", gameId);
    }

    /**
     * Phương thức trợ giúp chung để tải FXML, thiết lập controller và đặt vào trung
     * tâm.
     * 
     * @param fxmlPath Đường dẫn đến file FXML.
     * @param gameId   ID của game (dùng cho view chi tiết, nếu không thì bỏ qua).
     */
    private void loadViewAndSetupControllers(String fxmlPath, long gameId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Kiểm tra xem controller của view mới là loại nào để xử lý phù hợp
            Object controller = loader.getController();

            if (controller instanceof LibraryController) {
                // Nếu là LibraryController, truyền tham chiếu của MainLayoutController vào nó
                LibraryController libraryController = (LibraryController) controller;
                libraryController.setMainLayoutController(this);
            } else if (controller instanceof GameDetailController) {
                // Nếu là GameDetailController, truyền gameId vào nó
                GameDetailController gameDetailController = (GameDetailController) controller;
                gameDetailController.loadGameDetails(gameId);
            }

            // Đặt view mới vào trung tâm của BorderPane
            mainPane.setCenter(view);

        } catch (IOException e) {
            System.err.println("Lỗi khi tải file FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}