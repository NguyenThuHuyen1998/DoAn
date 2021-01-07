package com.example.crud.service;

import com.example.crud.entity.Tag;

import java.util.List;

public interface TagService {
    void save(Tag tag);
    void delete(Tag tag);
    List<Tag> getListTags();
}
