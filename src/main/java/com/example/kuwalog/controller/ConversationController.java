package com.example.kuwalog.controller;

import com.example.kuwalog.entity.Conversation;
import com.example.kuwalog.service.ConversationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/conversations")
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("conversations", conversationService.findByParticipant(userDetails.getUsername()));
        return "conversations/list";
    }

    @GetMapping("/conversations/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Conversation conversation = conversationService.findById(id, userDetails.getUsername());
        conversationService.markAsRead(id, userDetails.getUsername());
        model.addAttribute("conversation", conversation);
        model.addAttribute("messages", conversationService.findMessages(id));
        return "conversations/detail";
    }

    @PostMapping("/beetles/{beetleId}/conversations")
    public String startOrGet(@PathVariable Long beetleId,
                             @AuthenticationPrincipal UserDetails userDetails) {
        Conversation c = conversationService.findOrCreate(beetleId, userDetails.getUsername());
        return "redirect:/conversations/" + c.getId();
    }
}
