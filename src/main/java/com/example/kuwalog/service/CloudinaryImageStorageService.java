package com.example.kuwalog.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryImageStorageService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    @Override
    public String upload(MultipartFile file) {
        return upload(file, "kuwalog");
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("画像のアップロードに失敗しました", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            // URLからpublic_idを抽出（例: .../kuwalog/abc123 → kuwalog/abc123）
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("画像の削除に失敗しました", e);
        }
    }

    private String extractPublicId(String imageUrl) {
        // secure_urlの形式: https://res.cloudinary.com/{cloud}/image/upload/v{version}/{folder}/{id}.{ext}
        int uploadIdx = imageUrl.indexOf("/upload/");
        String afterUpload = imageUrl.substring(uploadIdx + 8);
        // バージョン番号(v1234/)があれば除去
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // 拡張子を除去
        int dotIdx = afterUpload.lastIndexOf('.');
        return dotIdx >= 0 ? afterUpload.substring(0, dotIdx) : afterUpload;
    }
}
