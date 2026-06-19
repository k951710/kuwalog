package com.example.kuwalog.service;

import com.example.kuwalog.dto.UserRegisterForm;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.exception.DuplicateUserException;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
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
