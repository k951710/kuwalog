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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private BeetleRepository beetleRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User poster;
    private User buyer;
    private Beetle beetle;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        poster = new User();
        poster.setId(1L);
        poster.setUsername("poster");

        buyer = new User();
        buyer.setId(2L);
        buyer.setUsername("buyer");

        beetle = new Beetle();
        beetle.setId(10L);
        beetle.setUser(poster);

        transaction = new Transaction();
        transaction.setBeetle(beetle);
        transaction.setFromUser(poster);
        transaction.setToUser(buyer);
        transaction.setTransferredOn(LocalDate.of(2025, 1, 1));
    }

    private TransactionForm basicForm() {
        TransactionForm form = new TransactionForm();
        form.setToUsername("buyer");
        form.setTransferredOn(LocalDate.of(2025, 6, 1));
        return form;
    }

    // --- register ---

    @Test
    void register_生体の投稿者は譲渡記録を登録できる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("poster")).thenReturn(Optional.of(poster));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> transactionService.register(10L, basicForm(), "poster"));
    }

    @Test
    void register_生体の投稿者以外は登録できない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));

        assertThatThrownBy(() -> transactionService.register(10L, basicForm(), "buyer"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("譲渡記録を登録できるのは生体の投稿者のみです");
    }

    @Test
    void register_存在しない譲渡先ユーザーはBAD_REQUEST() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("poster")).thenReturn(Optional.of(poster));
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.register(10L, basicForm(), "poster"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("譲渡先ユーザーが見つかりません");
    }

    @Test
    void register_自分自身への譲渡はBAD_REQUEST() {
        TransactionForm selfForm = new TransactionForm();
        selfForm.setToUsername("poster");
        selfForm.setTransferredOn(LocalDate.of(2025, 6, 1));

        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("poster")).thenReturn(Optional.of(poster));

        assertThatThrownBy(() -> transactionService.register(10L, selfForm, "poster"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分自身に譲渡することはできません");
    }

    // --- delete ---

    @Test
    void delete_fromUserは譲渡記録を削除できる() {
        when(transactionRepository.findByIdWithUsers(100L)).thenReturn(Optional.of(transaction));
        doNothing().when(reviewRepository).deleteByTransaction(transaction);
        doNothing().when(transactionRepository).delete(transaction);

        assertDoesNotThrow(() -> transactionService.delete(10L, 100L, "poster"));
    }

    @Test
    void delete_fromUser以外は削除できない() {
        when(transactionRepository.findByIdWithUsers(100L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.delete(10L, 100L, "buyer"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("削除権限がありません");
    }
}
