package com.example.kuwalog.service;

import com.example.kuwalog.dto.ReviewForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Review;
import com.example.kuwalog.entity.Transaction;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.entity.enums.ReviewType;
import com.example.kuwalog.repository.ReviewRepository;
import com.example.kuwalog.repository.TransactionRepository;
import com.example.kuwalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User fromUser;
    private User toUser;
    private User outsider;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        fromUser = new User();
        fromUser.setId(1L);
        fromUser.setUsername("fromUser");

        toUser = new User();
        toUser.setId(2L);
        toUser.setUsername("toUser");

        outsider = new User();
        outsider.setId(3L);
        outsider.setUsername("outsider");

        Beetle beetle = new Beetle();
        beetle.setId(10L);

        transaction = new Transaction();
        transaction.setBeetle(beetle);
        transaction.setFromUser(fromUser);
        transaction.setToUser(toUser);
    }

    private ReviewForm normalForm() {
        ReviewForm form = new ReviewForm();
        form.setReviewType(ReviewType.NORMAL);
        form.setRating(5);
        return form;
    }

    private ReviewForm followUpForm() {
        ReviewForm form = new ReviewForm();
        form.setReviewType(ReviewType.FOLLOW_UP);
        form.setRating(4);
        return form;
    }

    // --- 評価権限チェック ---

    @Test
    void register_fromUserは評価できる() {
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(any(), any(), any())).thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, normalForm(), "fromUser"));
    }

    @Test
    void register_toUserは評価できる() {
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("toUser")).thenReturn(Optional.of(toUser));
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(any(), any(), any())).thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, normalForm(), "toUser"));
    }

    @Test
    void register_取引に無関係なユーザーは403になる() {
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("outsider")).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> reviewService.register(1L, normalForm(), "outsider"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("この譲渡記録に評価を登録する権限がありません");
    }

    // --- 通常評価の重複チェック ---

    @Test
    void register_通常評価の重複登録はCONFLICT() {
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        // 後追い評価なし、通常評価あり
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(transaction, 1L, ReviewType.FOLLOW_UP)).thenReturn(false);
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(transaction, 1L, ReviewType.NORMAL)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.register(1L, normalForm(), "fromUser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("通常評価はすでに登録されています");
    }

    @Test
    void register_後追い評価が登録済みの場合は通常評価を登録できない() {
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        // 後追い評価あり
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(transaction, 1L, ReviewType.FOLLOW_UP)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.register(1L, normalForm(), "fromUser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("後追い評価が登録済みのため、通常評価は登録できません");
    }

    // --- 後追い評価 ---

    @Test
    void register_後追い評価は通常評価を塗り替えて登録できる() {
        Review existingNormal = new Review();
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.findByTransactionAndReviewerIdAndReviewType(transaction, 1L, ReviewType.NORMAL))
                .thenReturn(Optional.of(existingNormal));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, followUpForm(), "fromUser"));

        // 既存の通常評価が削除されること
        verify(reviewRepository).delete(existingNormal);
    }

    @Test
    void register_後追い評価は通常評価がなくても登録できる() {
        when(transactionRepository.findByIdWithUsers(1L)).thenReturn(Optional.of(transaction));
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.findByTransactionAndReviewerIdAndReviewType(transaction, 1L, ReviewType.NORMAL))
                .thenReturn(Optional.empty());
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, followUpForm(), "fromUser"));

        verify(reviewRepository, never()).delete(any(Review.class));
    }
}
