//package com.example.crud.entity;
//
//import javax.persistence.*;
//import java.io.Serializable;
//
//@Entity
//@Table(name = "tbl_post_tag")
//public class PostTag implements Serializable {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private long postTagId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tag_id", nullable = false)
//    private Tag tag;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "news_id", nullable = false)
//    private News news;
//
//    public PostTag(Tag tag, News news) {
//        this.tag= tag;
//        this.news= news;
//    }
//
//    public PostTag() {
//    }
//
//    public long getPostTagId() {
//        return postTagId;
//    }
//
//    public void setPostTagId(long postTagId) {
//        this.postTagId = postTagId;
//    }
//
//    public Tag getTag() {
//        return tag;
//    }
//
//    public void setTag(Tag tag) {
//        this.tag = tag;
//    }
//
//    public News getNews() {
//        return news;
//    }
//
//    public void setNews(News news) {
//        this.news = news;
//    }
//}
