package com.example.kuwalog.service;

import com.example.kuwalog.dto.ReviewForm;
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

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

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

        transaction = new Transaction();
        transaction.setFromUser(fromUser);
        transaction.setToUser(toUser);

        when(transactionRepository.findByIdWithUsers(anyLong()))
                .thenReturn(Optional.of(transaction));
    }

    // --- 評価者の権限チェック ---

    @Test
    void register_fromUserは評価できる() {
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, normalForm(), "fromUser"));
    }

    @Test
    void register_toUserは評価できる() {
        when(userRepository.findByUsername("toUser")).thenReturn(Optional.of(toUser));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, normalForm(), "toUser"));
    }

    @Test
    void register_取引に関係ないユーザーは評価できない() {
        when(userRepository.findByUsername("outsider")).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> reviewService.register(1L, normalForm(), "outsider"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("権限がありません");
    }

    // --- 通常評価の重複チェック ---

    @Test
    void register_通常評価を2回登録しようとするとCONFLICTになる() {
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(
                transaction, 1L, ReviewType.FOLLOW_UP)).thenReturn(false);
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(
                transaction, 1L, ReviewType.NORMAL)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.register(1L, normalForm(), "fromUser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("通常評価はすでに登録されています");
    }

    @Test
    void register_後追い評価が登録済みの場合は通常評価を登録できない() {
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.existsByTransactionAndReviewerIdAndReviewType(
                transaction, 1L, ReviewType.FOLLOW_UP)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.register(1L, normalForm(), "fromUser"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("後追い評価が登録済みのため、通常評価は登録できません");
    }

    // --- 後追い評価が通常評価を置き換える ---

    @Test
    void register_後追い評価は通常評価が存在する場合にそれを削除して登録できる() {
        com.example.kuwalog.entity.Review existingNormal = new com.example.kuwalog.entity.Review();
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.findByTransactionAndReviewerIdAndReviewType(
                transaction, 1L, ReviewType.NORMAL)).thenReturn(Optional.of(existingNormal));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, followUpForm(), "fromUser"));

        // 既存のNORMALが削除されていること
        verify(reviewRepository).delete(existingNormal);
    }

    @Test
    void register_後追い評価は通常評価がない場合もそのまま登録できる() {
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.findByTransactionAndReviewerIdAndReviewType(
                transaction, 1L, ReviewType.NORMAL)).thenReturn(Optional.empty());
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> reviewService.register(1L, followUpForm(), "fromUser"));

        verify(reviewRepository, never()).delete(any());
    }

    // --- 被評価者の自動決定 ---

    @Test
    void register_fromUserが評価するとtoUserが被評価者になる() {
        when(userRepository.findByUsername("fromUser")).thenReturn(Optional.of(fromUser));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reviewService.register(1L, normalForm(), "fromUser");

        verify(reviewRepository).save(argThat(r -> r.getReviewee().getId().equals(2L)));
    }

    @Test
    void register_toUserが評価するとfromUserが被評価者になる() {
        when(userRepository.findByUsername("toUser")).thenReturn(Optional.of(toUser));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reviewService.register(1L, normalForm(), "toUser");

        verify(reviewRepository).save(argThat(r -> r.getReviewee().getId().equals(1L)));
    }

    // --- ヘルパー ---

    private ReviewForm normalForm() {
        ReviewForm form = new ReviewForm();
        form.setReviewType(ReviewType.NORMAL);
        form.setRating(4);
        return form;
    }

    private ReviewForm followUpForm() {
        ReviewForm form = new ReviewForm();
        form.setReviewType(ReviewType.FOLLOW_UP);
        form.setRating(5);
        return form;
    }
}
