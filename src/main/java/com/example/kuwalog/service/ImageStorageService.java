package com.example.kuwalog.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String upload(MultipartFile file);
    String upload(MultipartFile file, String folder);
    void delete(String imageUrl);
}
