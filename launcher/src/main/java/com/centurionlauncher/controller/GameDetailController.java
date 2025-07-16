package com.centurionlauncher.controller;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import javafx.fxml.FXML;

public class GameDetailController {
    private void launchGame(String stubPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(stubPath);
        pb.directory(new File(stubPath).getParentFile());
        Instant startTime = Instant.now();
        Process process = pb.start();
        process.onExit().thenAccept(exited -> {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
        });
    }

    @FXML
    private void handlePlayButtonAction() throws Throwable {
        String stubExecutablePath = "D:\\Undertale\\UNDERTALE.exe";
        launchGame(stubExecutablePath);
    }

}
