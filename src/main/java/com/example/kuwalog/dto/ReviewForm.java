package com.example.kuwalog.dto;

import com.example.kuwalog.entity.enums.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReviewForm {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotNull
    private ReviewType reviewType;

    private String comment;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public ReviewType getReviewType() { return reviewType; }
    public void setReviewType(ReviewType reviewType) { this.reviewType = reviewType; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
