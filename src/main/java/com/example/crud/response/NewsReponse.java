package com.example.crud.response;

import com.example.crud.entity.Tag;

import java.util.List;

public class NewsReponse {
    private long newsIds;
    private String title;
    private String content;
    private String cover;
    private List<Tag> tagList;

    public NewsReponse(long newsIds, String title,String cover, String content) {
        this.newsIds = newsIds;
        this.title = title;
        this.content = content;
        this.cover= cover;
    }

    public NewsReponse(long newsIds, String title, String content, String cover, List<Tag> tagList) {
        this.newsIds = newsIds;
        this.title = title;
        this.content = content;
        this.cover= cover;
        this.tagList = tagList;
    }

    public long getNewsIds() {
        return newsIds;
    }

    public void setNewsIds(long newsIds) {
        this.newsIds = newsIds;
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

    public List<Tag> getTagList() {
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }
}
