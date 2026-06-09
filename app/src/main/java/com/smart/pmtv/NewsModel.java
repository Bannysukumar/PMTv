package com.smart.pmtv;

import com.google.firebase.Timestamp;

public class NewsModel {
    private String newsId;
    private String title;
    private String content;
    private String category;
    private String imageUrl;
    private String author;
    private Timestamp timestamp;

    public NewsModel() {
        // Empty constructor needed for Firestore serialization
    }

    public NewsModel(String newsId, String title, String content, String category, String imageUrl, String author, Timestamp timestamp) {
        this.newsId = newsId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl;
        this.author = author;
        this.timestamp = timestamp;
    }

    public String getNewsId() { return newsId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthor() { return author; }
    public Timestamp getTimestamp() { return timestamp; }
}
