package com.centurionlauncher.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường không cần thiết
public class GamePage {
    private List<LibraryEntry> content;
    private int totalPages;
    private long totalElements;
    private int number;

    // Getters and Setters
    public List<LibraryEntry> getContent() {
        return content;
    }

    public void setContent(List<LibraryEntry> content) {
        this.content = content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}