package com.example.crud.service.impl;

import com.example.crud.entity.News;
import com.example.crud.entity.PostTag;
import com.example.crud.entity.Tag;
import com.example.crud.repository.PostTagRepository;
import com.example.crud.repository.TagRepository;
import com.example.crud.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private TagRepository tagRepository;
    private PostTagRepository postTagRepository;

    @Autowired
    private TagServiceImpl(TagRepository tagRepository, PostTagRepository postTagRepository){
        this.tagRepository= tagRepository;
        this.postTagRepository= postTagRepository;
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

    @Override
    public void savePostTag(PostTag postTag) {
        postTagRepository.save(postTag);
    }

    @Override
    public void deletePostTag(PostTag postTag) {
        postTagRepository.delete(postTag);
    }

    @Override
    public List<PostTag> getListPostTag() {
        return (List<PostTag>) postTagRepository.findAll();
    }

    @Override
    public List<PostTag> getListPostTag(long newsId) {
        List<PostTag> postTagAll= (List<PostTag>) postTagRepository.findAll();
        List<PostTag> result= new ArrayList<>();
        if (postTagAll!= null && postTagAll.size()>0){
            for (PostTag postTag: postTagAll){
                if (postTag.getNews().getNewsId()== newsId) result.add(postTag);
            }
        }
        return result;
    }

    @Override
    public List<Tag> getListTagOfNews(News news) {
        List<PostTag> postTagList= (List<PostTag>) postTagRepository.findAll();
        List<PostTag> newsListHasTag= new ArrayList<>();
        for (PostTag postTag: postTagList){
            if (postTag.getNews().getNewsId()== news.getNewsId()){
                newsListHasTag.add(postTag);
            }
        }
        List<Tag> tagList= new ArrayList<>();
        if (newsListHasTag.size()>0){
            for (PostTag postTag: newsListHasTag){
                tagList.add(postTag.getTag());
            }
        }
        return tagList;
    }

    @Override
    public List<String> getListTagnameOfNews(News news) {
        List<Tag> tagList= getListTagOfNews(news);
        List<String> tagNames= new ArrayList<>();
        for (Tag tag: tagList){
            tagNames.add(tag.getTagName());
        }
        return tagNames;
    }


}
