package com.example.kuwalog.service;

import com.example.kuwalog.dto.UserProfileForm;
import com.example.kuwalog.dto.UserRegisterForm;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.exception.DuplicateUserException;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private static final long MAX_PROFILE_IMAGE_BYTES = 10 * 1024 * 1024; // 10MB

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    }

    @Transactional
    public void updateProfile(String username, UserProfileForm form,
                              MultipartFile profileImage, String loginUsername) {
        if (!username.equals(loginUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "編集権限がありません");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
        user.setBio(form.getBio());

        if (profileImage != null && !profileImage.isEmpty()) {
            if (profileImage.getSize() > MAX_PROFILE_IMAGE_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "画像は10MB以下にしてください");
            }
            String url = imageStorageService.upload(profileImage, "kuwalog/profiles");
            user.setProfileImageUrl(url);
        }
    }

    @Transactional
    public User register(UserRegisterForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new DuplicateUserException("username", "このユーザー名はすでに使われています");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new DuplicateUserException("email", "このメールアドレスはすでに登録されています");
        }

        // パスワード一致チェックはフォームバインド後にService層で行う
        // (ControllerのBindingResultに追加できないため、呼び出し元で処理する)
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        return userRepository.save(user);
    }
}
