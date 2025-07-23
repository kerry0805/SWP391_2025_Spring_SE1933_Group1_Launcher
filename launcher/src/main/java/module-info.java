module com.centurionlauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires com.auth0.jwt;
    requires javafx.graphics;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.centurionlauncher to javafx.fxml;
    opens com.centurionlauncher.controller to javafx.fxml;
    opens com.centurionlauncher.model to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.centurionlauncher;
}
