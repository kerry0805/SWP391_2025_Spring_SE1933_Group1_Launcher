package com.centurionlauncher.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LibraryEntry {
    private Game gameDetail;
    private long playtimeInMillis;
    private LocalDateTime lastPlayedTime;

    public LocalDateTime getLastPlayedTime() {
        return lastPlayedTime;
    }

    public void setLastPlayedTime(LocalDateTime lastPlayedTime) {
        this.lastPlayedTime = lastPlayedTime;
    }

    public Game getGameDetail() {
        System.out.println(gameDetail);
        return gameDetail;
    }

    public void setGameDetail(Game gameDetail) {
        this.gameDetail = gameDetail;
    }

    public long getPlaytimeInMillis() {
        return playtimeInMillis;
    }
}
