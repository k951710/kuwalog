package com.example.kuwalog.service;

import com.example.kuwalog.dto.UserProfileDto;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Review;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.ReviewRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BeetleRepository beetleRepository;

    public UserProfileService(UserRepository userRepository,
                              ReviewRepository reviewRepository,
                              BeetleRepository beetleRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.beetleRepository = beetleRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        List<Review> receivedReviews = reviewRepository.findByRevieweeWithReviewer(user);
        List<Beetle> beetles = beetleRepository.findByUserOrderByCreatedAtDesc(user);

        return new UserProfileDto(user, receivedReviews, beetles);
    }
}
