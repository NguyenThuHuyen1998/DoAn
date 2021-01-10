package com.example.crud.controller;

import com.example.crud.entity.Tag;
import com.example.crud.response.MessageResponse;
import com.example.crud.service.JwtService;
import com.example.crud.service.TagService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class TagController {

    private TagService tagService;
    private JwtService jwtService;

    @Autowired
    private TagController(TagService tagService, JwtService jwtService){
        this.tagService= tagService;
        this.jwtService= jwtService;
    }

    @PostMapping(value = "adminPage/tags")
    public ResponseEntity<Tag> createTag(@RequestBody String data,
                                         HttpServletRequest request){
        if (jwtService.isAdmin(request)){
            String tagname= new JSONObject(data).getString("tagName");
            List<Tag> tagList= tagService.getListTags();
            for(Tag tag: tagList){
                if(tag.getTagName().equals(tagname)){
                    return new ResponseEntity(new MessageResponse().getResponse("Tag này đã tồn tại"), HttpStatus.BAD_REQUEST);
                }
            }
            tagService.save(tagname);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(value = "tags")
    public ResponseEntity<Tag> getlistTag(){
            List<Tag> tagList= tagService.getListTags();
            if (tagList!= null || tagList.size()> 0){
                return new ResponseEntity(tagList, HttpStatus.OK);
            }
            return new ResponseEntity(new MessageResponse().getResponse("Chưa có tag nào."), HttpStatus.NO_CONTENT);
    }
}
