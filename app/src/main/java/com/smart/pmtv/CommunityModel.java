package com.smart.pmtv;

import com.google.firebase.Timestamp;
import java.util.List;

public class CommunityModel {
    private String postId;
    private String authorId;
    private String authorName;
    private String content;
    private String imageUrl;
    private Timestamp timestamp;
    private int likesCount;
    private int dislikesCount;
    private int commentsCount;
    private List<String> likedBy; // List of UIDs who liked

    public CommunityModel() {
        // Empty constructor for Firestore
    }

    public CommunityModel(String postId, String authorId, String authorName, String content, String imageUrl, Timestamp timestamp) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.commentsCount = 0;
    }

    public String getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getLikesCount() { return likesCount; }
    public int getDislikesCount() { return dislikesCount; }
    public int getCommentsCount() { return commentsCount; }
    public List<String> getLikedBy() { return likedBy; }
}
