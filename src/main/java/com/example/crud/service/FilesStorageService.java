package com.example.crud.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService {
    void init();

    String save(MultipartFile file);
    Resource load(String filename);
    void deleteAll();
}
