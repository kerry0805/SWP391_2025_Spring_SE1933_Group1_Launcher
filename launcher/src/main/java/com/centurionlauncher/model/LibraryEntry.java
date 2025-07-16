package com.centurionlauncher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryEntry {
    private Game gameDetail;

    // Getters and Setters
    public Game getGameDetail() {
        return gameDetail;
    }

    public void setGameDetail(Game gameDetail) {
        this.gameDetail = gameDetail;
    }
}
