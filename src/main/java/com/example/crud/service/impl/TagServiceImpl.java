package com.example.crud.service.impl;

//import com.example.crud.entity.PostTag;
import com.example.crud.entity.Tag;
//import com.example.crud.repository.PostTagRepository;
import com.example.crud.repository.TagRepository;
import com.example.crud.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private TagRepository tagRepository;
    //private PostTagRepository postTagRepository;

    @Autowired
    private TagServiceImpl(TagRepository tagRepository){
        this.tagRepository= tagRepository;
//        this.postTagRepository= postTagRepository;
    }

    @Override
    public boolean save(String tagName) {
        Tag tag= getTagByName(tagName);
        if (tag== null){
            tag= new Tag(tagName);
            tagRepository.save(tag);
            return true;
        }
        return false;
    }

    @Override
    public void delete(Tag tag) {
        tagRepository.save(tag);
    }

    @Override
    public List<Tag> getListTags() {
        return (List<Tag>) tagRepository.findAll();
    }

    @Override
    public Tag getTagByName(String tagName) {
        List<Tag> tagList= (List<Tag>) tagRepository.findAll();
        if (tagList.size()>0){
            for (Tag tag: tagList){
                if(tag.getTagName().equals(tagName))  {
                    return tag;
                }
            }
        }
        return null;
    }

//    @Override
//    public void savePostTag(PostTag postTag) {
//        postTagRepository.save(postTag);
//    }


}
