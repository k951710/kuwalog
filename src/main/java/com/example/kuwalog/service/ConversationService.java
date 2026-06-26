package com.example.kuwalog.service;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.Conversation;
import com.example.kuwalog.entity.ConversationRead;
import com.example.kuwalog.entity.Message;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.repository.BeetleRepository;
import com.example.kuwalog.repository.ConversationReadRepository;
import com.example.kuwalog.repository.ConversationRepository;
import com.example.kuwalog.repository.MessageRepository;
import com.example.kuwalog.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationReadRepository conversationReadRepository;
    private final MessageRepository messageRepository;
    private final BeetleRepository beetleRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationReadRepository conversationReadRepository,
                               MessageRepository messageRepository,
                               BeetleRepository beetleRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationReadRepository = conversationReadRepository;
        this.messageRepository = messageRepository;
        this.beetleRepository = beetleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Conversation findOrCreate(Long beetleId, String initiatorUsername) {
        Beetle beetle = beetleRepository.findByIdWithUser(beetleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "生体が見つかりません"));
        User initiator = getUser(initiatorUsername);

        if (beetle.getUser().getId().equals(initiator.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自分の投稿には問い合わせできません");
        }

        return conversationRepository.findByBeetleIdAndInitiatorId(beetleId, initiator.getId())
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setBeetle(beetle);
                    c.setInitiator(initiator);
                    c.setOwner(beetle.getUser());
                    return conversationRepository.save(c);
                });
    }

    @Transactional(readOnly = true)
    public Conversation findById(Long id, String username) {
        Conversation c = conversationRepository.findByIdWithAll(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会話が見つかりません"));
        checkParticipant(c, username);
        return c;
    }

    @Transactional(readOnly = true)
    public List<Conversation> findByParticipant(String username) {
        User user = getUser(username);
        return conversationRepository.findByParticipantIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Message> findMessages(Long conversationId) {
        return messageRepository.findByConversationIdWithSender(conversationId);
    }

    @Transactional
    public Message saveMessage(Long conversationId, String senderUsername, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "メッセージが空です");
        }
        Conversation c = conversationRepository.findByIdWithAll(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会話が見つかりません"));
        User sender = getUser(senderUsername);
        checkParticipant(c, senderUsername);

        Message m = new Message();
        m.setConversation(c);
        m.setSender(sender);
        m.setContent(content.trim());
        return messageRepository.save(m);
    }

    @Transactional
    public void markAsRead(Long conversationId, String username) {
        User user = getUser(username);
        ConversationRead cr = conversationReadRepository
                .findByConversationIdAndUserId(conversationId, user.getId())
                .orElseGet(() -> {
                    ConversationRead newCr = new ConversationRead();
                    Conversation c = conversationRepository.findById(conversationId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                    newCr.setConversation(c);
                    newCr.setUser(user);
                    return newCr;
                });
        cr.setLastReadAt(LocalDateTime.now());
        conversationReadRepository.save(cr);
    }

    @Transactional(readOnly = true)
    public long countUnreadConversations(String username) {
        return userRepository.findByUsername(username)
                .map(u -> conversationReadRepository.countUnreadConversations(u.getId()))
                .orElse(0L);
    }

    private void checkParticipant(Conversation c, String username) {
        boolean isParticipant = c.getInitiator().getUsername().equals(username)
                || c.getOwner().getUsername().equals(username);
        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "この会話を閲覧する権限がありません");
        }
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
