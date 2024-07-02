package com.app.pinterest.models;

public class PinsModelClass {
    private String id;
    private String title;
    private String description;
    private String link;
    private String picUrl;
    private String userId;
    private String boardId;

    public PinsModelClass() {}

    public PinsModelClass(String id, String title, String description, String link, String picUrl, String userId, String boardId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
        this.picUrl = picUrl;
        this.userId = userId;
        this.boardId = boardId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getBoardId() {
        return boardId;
    }
}
