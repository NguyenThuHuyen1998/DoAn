package com.example.crud.service;

//import com.example.crud.entity.PostTag;
import com.example.crud.entity.Tag;

import java.util.List;

public interface TagService {
    boolean save(String tagName);
    void delete(Tag tag);
    List<Tag> getListTags();
    Tag getTagByName(String tagName);
//    void savePostTag(PostTag postTag);
}
