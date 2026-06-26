package com.example.kuwalog.controller;

import com.example.kuwalog.dto.ChatMessageRequest;
import com.example.kuwalog.dto.ChatMessageResponse;
import com.example.kuwalog.entity.Message;
import com.example.kuwalog.service.ConversationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatWebSocketController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ConversationService conversationService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(@Payload ChatMessageRequest request, Principal principal) {
        Message message = conversationService.saveMessage(
                request.getConversationId(), principal.getName(), request.getContent());

        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSenderUsername(message.getSender().getUsername());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt().format(FORMATTER));

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId(), response);
    }
}
