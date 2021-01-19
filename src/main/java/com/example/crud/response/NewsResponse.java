package com.example.crud.response;

import java.util.List;

public class NewsResponse {
    public long newsId;
    public String title;
    public String content;
    public String datePost;
    public String cover;
    public String userName;
    public List<String> tags;

    public NewsResponse(){

    }

    public NewsResponse(long newsId, String title, String content, String datePost, String cover, String userName, List<String> tags) {
        this.newsId = newsId;
        this.title = title;
        this.content = content;
        this.datePost = datePost;
        this.userName = userName;
        this.cover= cover;
        this.tags = tags;
    }

    public long getNewsId() {
        return newsId;
    }

    public void setNewsId(long newsId) {
        this.newsId = newsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDatePost() {
        return datePost;
    }

    public void setDatePost(String datePost) {
        this.datePost = datePost;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
