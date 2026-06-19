package com.example.kuwalog.service;

import com.example.kuwalog.dto.ReviewForm;
import com.example.kuwalog.entity.Review;
import com.example.kuwalog.entity.Transaction;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.ReviewRepository;
import com.example.kuwalog.repository.TransactionRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.kuwalog.entity.enums.ReviewType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Review> findByTransactionId(Long transactionId) {
        Transaction transaction = getTransaction(transactionId);
        return reviewRepository.findByTransactionWithUsers(transaction);
    }

    @Transactional
    public Review register(Long transactionId, ReviewForm form, String reviewerUsername) {
        Transaction transaction = transactionRepository.findByIdWithUsers(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "譲渡記録が見つかりません"));

        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        // 評価者はfrom_userまたはto_userのいずれかであること
        boolean isFromUser = transaction.getFromUser().getId().equals(reviewer.getId());
        boolean isToUser = transaction.getToUser().getId().equals(reviewer.getId());
        if (!isFromUser && !isToUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この譲渡記録に評価を登録する権限がありません");
        }

        // 被評価者は評価者の相手側
        User reviewee = isFromUser ? transaction.getToUser() : transaction.getFromUser();

        if (form.getReviewType() == ReviewType.FOLLOW_UP) {
            // 後追い評価は通常評価を塗り替える：既存のNORMALを削除してから登録
            reviewRepository.findByTransactionAndReviewerIdAndReviewType(
                    transaction, reviewer.getId(), ReviewType.NORMAL)
                    .ifPresent(reviewRepository::delete);
        } else {
            // 通常評価：後追い評価が既にある場合は登録不可
            if (reviewRepository.existsByTransactionAndReviewerIdAndReviewType(
                    transaction, reviewer.getId(), ReviewType.FOLLOW_UP)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "後追い評価が登録済みのため、通常評価は登録できません");
            }
            // 通常評価の重複チェック
            if (reviewRepository.existsByTransactionAndReviewerIdAndReviewType(
                    transaction, reviewer.getId(), ReviewType.NORMAL)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "通常評価はすでに登録されています");
            }
        }

        Review review = new Review();
        review.setTransaction(transaction);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(form.getRating());
        review.setReviewType(form.getReviewType());
        review.setComment(form.getComment());
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Set<ReviewType> findRegisteredTypes(Long transactionId, Long reviewerId) {
        Transaction transaction = getTransaction(transactionId);
        List<ReviewType> types = reviewRepository.findReviewTypesByTransactionAndReviewerId(transaction, reviewerId);
        return types.isEmpty() ? EnumSet.noneOf(ReviewType.class) : EnumSet.copyOf(types);
    }

    private Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "譲渡記録が見つかりません"));
    }
}
