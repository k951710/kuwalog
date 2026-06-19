package com.example.kuwalog.dto;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Review;
import com.example.kuwalog.entity.User;

import java.util.List;

public class UserProfileDto {

    private final User user;
    private final List<Review> receivedReviews;
    private final List<Beetle> beetles;

    public UserProfileDto(User user, List<Review> receivedReviews, List<Beetle> beetles) {
        this.user = user;
        this.receivedReviews = receivedReviews;
        this.beetles = beetles;
    }

    public User getUser() { return user; }
    public List<Review> getReceivedReviews() { return receivedReviews; }
    public List<Beetle> getBeetles() { return beetles; }

    public long getReviewCount() {
        return receivedReviews.size();
    }

    public Double getAvgRating() {
        if (receivedReviews.isEmpty()) return null;
        return receivedReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
