package com.centurionlauncher.model;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
    private long gameId;
    private String name;
    private String shortDescription;
    private String fullDescription;
    private List<Tag> tags;
    private List<Media> media;
    private Boolean state;
    private String gameUrl;
    private String iconUrl;
    private String updateLog;

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    // public LocalDate getReleaseDate() {
    //     return releaseDate;
    // }

    // public void setReleaseDate(LocalDate releaseDate) {
    //     this.releaseDate = releaseDate;
    // }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }


    public String getHeaderImageUrl() {
        if (media != null) {
            for (Media m : media) {
                if ("image_header".equals(m.getType())) {
                    return m.getUrl();
                }
            }
        }
        return null; // Hoặc trả về một URL ảnh mặc định
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }
}