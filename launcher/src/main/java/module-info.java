module com.kerilauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires com.auth0.jwt;
    requires javafx.graphics;

    opens com.centurionlauncher to javafx.fxml;
    opens com.centurionlauncher.controller to javafx.fxml;

    exports com.centurionlauncher;
}
