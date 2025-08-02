package com.centurionlauncher.dto;

import com.centurionlauncher.model.LibraryEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FamilyGameDTO extends LibraryEntry {
    private Boolean isPlayable;

    // Getters and Setters
    public boolean getIsPlayable() {
        return isPlayable;
    }

    public void setIsPlayable(Boolean isPlayable) {
        this.isPlayable = isPlayable;
    }
}
