package com.example.kuwalog.service;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Conversation;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.ConversationReadRepository;
import com.example.kuwalog.repository.ConversationRepository;
import com.example.kuwalog.repository.MessageRepository;
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
class ConversationServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationReadRepository conversationReadRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private BeetleRepository beetleRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ConversationService conversationService;

    private User owner;
    private User initiator;
    private User outsider;
    private Beetle beetle;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        initiator = new User();
        initiator.setId(2L);
        initiator.setUsername("initiator");

        outsider = new User();
        outsider.setId(3L);
        outsider.setUsername("outsider");

        beetle = new Beetle();
        beetle.setId(10L);
        beetle.setUser(owner);

        conversation = new Conversation();
        conversation.setBeetle(beetle);
        conversation.setInitiator(initiator);
        conversation.setOwner(owner);
    }

    // --- findOrCreate ---

    @Test
    void findOrCreate_生体の投稿者以外は問い合わせできる() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("initiator")).thenReturn(Optional.of(initiator));
        when(conversationRepository.findByBeetleIdAndInitiatorId(10L, 2L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> conversationService.findOrCreate(10L, "initiator"));
    }

    @Test
    void findOrCreate_生体の投稿者自身は問い合わせできない() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> conversationService.findOrCreate(10L, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("自分の投稿には問い合わせできません");
    }

    @Test
    void findOrCreate_同じ生体への重複問い合わせは既存会話を返す() {
        when(beetleRepository.findByIdWithUser(10L)).thenReturn(Optional.of(beetle));
        when(userRepository.findByUsername("initiator")).thenReturn(Optional.of(initiator));
        when(conversationRepository.findByBeetleIdAndInitiatorId(10L, 2L))
                .thenReturn(Optional.of(conversation));

        conversationService.findOrCreate(10L, "initiator");

        // 既存会話が返るため新規保存は呼ばれない
        verify(conversationRepository, never()).save(any());
    }

    // --- findById ---

    @Test
    void findById_参加者initiatorは会話を取得できる() {
        when(conversationRepository.findByIdWithAll(100L)).thenReturn(Optional.of(conversation));

        assertDoesNotThrow(() -> conversationService.findById(100L, "initiator"));
    }

    @Test
    void findById_参加者ownerは会話を取得できる() {
        when(conversationRepository.findByIdWithAll(100L)).thenReturn(Optional.of(conversation));

        assertDoesNotThrow(() -> conversationService.findById(100L, "owner"));
    }

    @Test
    void findById_参加者でないユーザーは403になる() {
        when(conversationRepository.findByIdWithAll(100L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.findById(100L, "outsider"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("権限がありません");
    }

    // --- saveMessage ---

    @Test
    void saveMessage_参加者はメッセージを送信できる() {
        when(conversationRepository.findByIdWithAll(100L)).thenReturn(Optional.of(conversation));
        when(userRepository.findByUsername("initiator")).thenReturn(Optional.of(initiator));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> conversationService.saveMessage(100L, "initiator", "こんにちは"));
    }

    @Test
    void saveMessage_空メッセージはBAD_REQUESTになる() {
        assertThatThrownBy(() -> conversationService.saveMessage(100L, "initiator", "  "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("メッセージが空です");
    }

    @Test
    void saveMessage_nullメッセージはBAD_REQUESTになる() {
        assertThatThrownBy(() -> conversationService.saveMessage(100L, "initiator", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("メッセージが空です");
    }

    @Test
    void saveMessage_参加者でないユーザーは403になる() {
        when(conversationRepository.findByIdWithAll(100L)).thenReturn(Optional.of(conversation));
        when(userRepository.findByUsername("outsider")).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> conversationService.saveMessage(100L, "outsider", "メッセージ"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("権限がありません");
    }
}
