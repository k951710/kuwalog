package com.example.kuwalog.service;

import com.example.kuwalog.dto.TransactionForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Transaction;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.BeetleRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BeetleRepository beetleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User owner;
    private User other;
    private Beetle beetle;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        other = new User();
        other.setId(2L);
        other.setUsername("other");

        beetle = new Beetle();
        beetle.setId(10L);
        beetle.setUser(owner);

        transaction = new Transaction();
        transaction.setBeetle(beetle);
        transaction.setFromUser(owner);
        transaction.setToUser(other);
    }

    // --- register: 投稿者権限チェック ---

    @Test
    void register_生体の投稿者は譲渡記録を登録できる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionForm form = validForm("other");
        assertDoesNotThrow(() -> transactionService.register(10L, form, "owner"));
    }

    @Test
    void register_投稿者以外は譲渡記録を登録できない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        TransactionForm form = validForm("other");
        assertThatThrownBy(() -> transactionService.register(10L, form, "other"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("譲渡記録を登録できるのは生体の投稿者のみです");
    }

    // --- register: 自己譲渡禁止 ---

    @Test
    void register_自分自身への譲渡はできない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        // toUsernameも"owner"（自分自身）
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));

        TransactionForm form = validForm("owner");
        assertThatThrownBy(() -> transactionService.register(10L, form, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分自身に譲渡することはできません");
    }

    // --- delete: 投稿者権限チェック ---

    @Test
    void delete_fromUserは譲渡記録を削除できる() {
        when(transactionRepository.findByIdWithUsers(20L)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.delete(10L, 20L, "owner"));

        // FK制約のためreviewsが先に削除されること
        verify(reviewRepository).deleteByTransaction(transaction);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void delete_fromUser以外は譲渡記録を削除できない() {
        when(transactionRepository.findByIdWithUsers(20L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.delete(10L, 20L, "other"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("削除権限がありません");
    }

    // --- delete: 関連レビューが先に削除される ---

    @Test
    void delete_紐づく評価がreviewsより先に削除される() {
        when(transactionRepository.findByIdWithUsers(20L)).thenReturn(Optional.of(transaction));

        transactionService.delete(10L, 20L, "owner");

        // deleteByTransaction → delete の順を検証
        var inOrder = org.mockito.Mockito.inOrder(reviewRepository, transactionRepository);
        inOrder.verify(reviewRepository).deleteByTransaction(transaction);
        inOrder.verify(transactionRepository).delete(transaction);
    }

    // --- ヘルパー ---

    private TransactionForm validForm(String toUsername) {
        TransactionForm form = new TransactionForm();
        form.setToUsername(toUsername);
        form.setTransferredOn(LocalDate.of(2026, 1, 1));
        return form;
    }
}
