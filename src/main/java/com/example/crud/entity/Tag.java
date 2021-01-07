package com.example.crud.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tbl_tag")
public class Tag implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long tagId;

    @Column(name = "tag_name")
    private String tagName;

    public Tag(long tagId, String tagName) {
        this.tagId = tagId;
        this.tagName = tagName;
    }

    public Tag() {
    }

    public Tag(String tagName){
        this.tagName= tagName;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
