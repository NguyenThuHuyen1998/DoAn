package com.example.crud.controller;

import com.example.crud.entity.News;
import com.example.crud.entity.PostTag;
import com.example.crud.entity.Tag;
import com.example.crud.entity.User;
import com.example.crud.response.MessageResponse;
import com.example.crud.response.NewsReponse;
import com.example.crud.service.FilesStorageService;
import com.example.crud.service.JwtService;
import com.example.crud.service.NewsService;
import com.example.crud.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class NewsController {

    public static final Logger logger = LoggerFactory.getLogger(NewsController.class);
    private NewsService newsService;
    private JwtService jwtService;
    private TagService tagService;
    private FilesStorageService storageService;

    public NewsController(NewsService newsService, JwtService jwtService, TagService tagService,  FilesStorageService filesStorageService) {
        this.newsService = newsService;
        this.jwtService = jwtService;
        this.tagService = tagService;
        this.storageService = filesStorageService;
    }

    @GetMapping(value = "/news")
    public ResponseEntity<News> getAllNews() {
        List<News> listNews = newsService.getListNews();
        if (listNews.size()>0){
            for (News news: listNews){
                NewsReponse newsReponse = new NewsReponse(news.getNewsId(), news.getTitle(), news.getCover(), news.getContent());
                List<Tag> tagList= tagService.getListTagOfNews(news);
                newsReponse.setTagList(tagList);
            }
            return new ResponseEntity(listNews, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse(" Chưa có bài viết nào."), HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "adminPage/news")
    public ResponseEntity<News> postNews(@RequestParam("title") String title,
                                         @RequestParam("content") String content,
                                         @RequestParam("cover") MultipartFile cover,
                                         @RequestParam(value = "tags", required = false) String[] tags,
                                         HttpServletRequest request) {
        if (jwtService.isAdmin(request)) {
            User user = jwtService.getCurrentUser(request);
            News news = new News();
            news.setUser(user);
            news.setContent(content);
            news.setTitle(title);
            String fileName = storageService.save(cover);
            news.setCover(fileName);
            news.setDatePost(new Date().getTime());
            newsService.saveNews(news);
            List<Tag> tagList=  new ArrayList<>();
            for (String tagName: tags){
                Tag tag= tagService.getTagByName(tagName);
                if (tag!= null){
                    tagList.add(tag);
                }
                else return new ResponseEntity(new MessageResponse().getResponse("Tag không tồn tại."), HttpStatus.BAD_REQUEST);
            }
            if (tagList.size()>0){
                for (Tag tag: tagList){
                    PostTag postTag= new PostTag(tag, news);
                    tagService.savePostTag(postTag);
                }
            }
            return new ResponseEntity<>(news, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PutMapping(value = "/news/{id}")
    public ResponseEntity<News> updateNews(@RequestParam(value = "title", required = true, defaultValue = "") String title,
                                           @RequestParam(value = "content", required = true, defaultValue = "") String content,
                                           @RequestParam(value = "cover", required = false) MultipartFile cover,
                                           @RequestParam(value = "tags", required = false, defaultValue = "") String[] tagNames,
                                           @PathVariable(name = "id") long newsId,
                                           HttpServletRequest request) {
        if (jwtService.isAdmin(request)) {
            News news = newsService.getNewsById(newsId);
            if (news == null) {
                return new ResponseEntity(new MessageResponse().getResponse("Tin tức không tồn tại."), HttpStatus.BAD_REQUEST);
            }
            news.setTitle(title.equals("") ? news.getTitle() : title);
            news.setContent(content.equals("") ? news.getContent() : content);
            if (cover!= null){
                news.setCover(storageService.save(cover));
            }
            newsService.saveNews(news);
            return new ResponseEntity<>(news, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không thể sửa bài viết này."), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "/news/{id}")
    public ResponseEntity<News> deleteNews(@PathVariable(name = "id") long newsId,
                                           HttpServletRequest request) {
        if (jwtService.isAdmin(request)) {
            try {
                News news = newsService.getNewsById(newsId);
                newsService.deleteNews(news);
                return new ResponseEntity(new MessageResponse().getResponse("Xóa bài viết thành công!"), HttpStatus.OK);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }
}
