package com.centurionlauncher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryEntry {
    private Game gameDetail;

    // Getters and Setters
    public Game getGameDetail() {
        System.out.println(gameDetail);
        return gameDetail;
    }

    public void setGameDetail(Game gameDetail) {
        this.gameDetail = gameDetail;
    }

    public long getPlaytimeInMillis() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlaytimeInMillis'");
    }
}
