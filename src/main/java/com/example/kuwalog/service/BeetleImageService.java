package com.example.kuwalog.service;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.BeetleImage;
import com.example.kuwalog.repository.BeetleImageRepository;
import com.example.kuwalog.repository.BeetleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BeetleImageService {

    private static final int MAX_IMAGES = 4;

    private final BeetleImageRepository beetleImageRepository;
    private final BeetleRepository beetleRepository;
    private final ImageStorageService imageStorageService;

    public BeetleImageService(BeetleImageRepository beetleImageRepository,
                              BeetleRepository beetleRepository,
                              ImageStorageService imageStorageService) {
        this.beetleImageRepository = beetleImageRepository;
        this.beetleRepository = beetleRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<BeetleImage> findByBeetle(Beetle beetle) {
        return beetleImageRepository.findByBeetleOrderBySortOrderAsc(beetle);
    }

    @Transactional
    public void upload(Long beetleId, MultipartFile file, String username) {
        Beetle beetle = getBeetleAndCheckOwner(beetleId, username);

        if (beetleImageRepository.countByBeetle(beetle) >= MAX_IMAGES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "画像は最大" + MAX_IMAGES + "枚までです");
        }

        String url = imageStorageService.upload(file);

        BeetleImage image = new BeetleImage();
        image.setBeetle(beetle);
        image.setImageUrl(url);
        image.setSortOrder(beetleImageRepository.countByBeetle(beetle));

        // 1枚目は自動的に代表画像にする
        if (beetleImageRepository.countByBeetle(beetle) == 0) {
            image.setPrimary(true);
        }

        beetleImageRepository.save(image);
    }

    @Transactional
    public void setPrimary(Long beetleId, Long imageId, String username) {
        Beetle beetle = getBeetleAndCheckOwner(beetleId, username);
        BeetleImage image = beetleImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "画像が見つかりません"));

        if (!image.getBeetle().getId().equals(beetle.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この画像を操作する権限がありません");
        }

        beetleImageRepository.clearPrimaryByBeetle(beetle);
        image.setPrimary(true);
        beetleImageRepository.save(image);
    }

    @Transactional
    public void delete(Long beetleId, Long imageId, String username) {
        Beetle beetle = getBeetleAndCheckOwner(beetleId, username);
        BeetleImage image = beetleImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "画像が見つかりません"));

        if (!image.getBeetle().getId().equals(beetle.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この画像を操作する権限がありません");
        }

        boolean wasPrimary = image.isPrimary();
        imageStorageService.delete(image.getImageUrl());
        beetleImageRepository.delete(image);

        // 削除した画像が代表だった場合、残りの先頭を代表に昇格
        if (wasPrimary) {
            List<BeetleImage> remaining = beetleImageRepository.findByBeetleOrderBySortOrderAsc(beetle);
            if (!remaining.isEmpty()) {
                remaining.get(0).setPrimary(true);
                beetleImageRepository.save(remaining.get(0));
            }
        }
    }

    @Transactional
    public void uploadAll(Long beetleId, List<MultipartFile> files, String username) {
        if (files == null) return;
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                upload(beetleId, file, username);
            }
        }
    }

    // 生体リストの先頭画像URLをまとめて取得する（一覧表示用）
    @Transactional(readOnly = true)
    public Map<Long, String> buildImageMap(List<Beetle> beetles) {
        Map<Long, String> imageMap = new HashMap<>();
        for (Beetle b : beetles) {
            beetleImageRepository.findFirstByBeetleOrderBySortOrderAsc(b)
                    .map(BeetleImage::getImageUrl)
                    .ifPresent(url -> imageMap.put(b.getId(), url));
        }
        return imageMap;
    }

    // 生体削除時にCloudinaryとDBから全画像を削除する
    @Transactional
    public void deleteAllForBeetle(Beetle beetle) {
        beetleImageRepository.findByBeetleOrderBySortOrderAsc(beetle).forEach(img -> {
            imageStorageService.delete(img.getImageUrl());
            beetleImageRepository.delete(img);
        });
    }

    private Beetle getBeetleAndCheckOwner(Long beetleId, String username) {
        Beetle beetle = beetleRepository.findByIdWithUser(beetleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));
        if (!beetle.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "操作する権限がありません");
        }
        return beetle;
    }
}
