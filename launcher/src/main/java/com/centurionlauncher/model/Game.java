package com.centurionlauncher.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
    private long gameId;
    private String name;
    private String shortDescription;
    private List<Tag> tags;
    private List<Media> media;

    // Helper method để lấy ảnh header
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

    // Getters and Setters cho các trường khác...
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