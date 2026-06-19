package com.example.kuwalog.repository;

import com.example.kuwalog.entity.Review;
import com.example.kuwalog.entity.Transaction;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.entity.enums.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer JOIN FETCH r.reviewee WHERE r.transaction = :transaction ORDER BY r.createdAt ASC")
    List<Review> findByTransactionWithUsers(@Param("transaction") Transaction transaction);

    boolean existsByTransactionAndReviewerIdAndReviewType(Transaction transaction, Long reviewerId, ReviewType reviewType);

    @Query("SELECT r FROM Review r WHERE r.transaction = :transaction AND r.reviewer.id = :reviewerId AND r.reviewType = :reviewType")
    java.util.Optional<Review> findByTransactionAndReviewerIdAndReviewType(
            @Param("transaction") Transaction transaction,
            @Param("reviewerId") Long reviewerId,
            @Param("reviewType") ReviewType reviewType);

    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer WHERE r.reviewee = :reviewee ORDER BY r.createdAt DESC")
    List<Review> findByRevieweeWithReviewer(@Param("reviewee") User reviewee);

    void deleteByTransaction(Transaction transaction);

    @Query("SELECT r.reviewType FROM Review r WHERE r.transaction = :transaction AND r.reviewer.id = :reviewerId")
    List<ReviewType> findReviewTypesByTransactionAndReviewerId(
            @Param("transaction") Transaction transaction,
            @Param("reviewerId") Long reviewerId);

}
