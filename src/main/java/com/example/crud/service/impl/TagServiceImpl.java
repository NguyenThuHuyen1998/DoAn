package com.example.crud.service.impl;

import com.example.crud.entity.Tag;
import com.example.crud.repository.TagRepository;
import com.example.crud.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private TagRepository tagRepository;

    @Autowired
    private TagServiceImpl(TagRepository tagRepository){
        this.tagRepository= tagRepository;
    }

    @Override
    public void save(Tag tag) {
        tagRepository.save(tag);
    }

    @Override
    public void delete(Tag tag) {
        tagRepository.save(tag);
    }

    @Override
    public List<Tag> getListTags() {
        return (List<Tag>) tagRepository.findAll();
    }
}
