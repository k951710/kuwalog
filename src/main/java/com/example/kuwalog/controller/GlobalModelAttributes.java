package com.example.kuwalog.controller;

import com.example.kuwalog.repository.UserRepository;
import com.example.kuwalog.service.ConversationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    public GlobalModelAttributes(ConversationService conversationService,
                                  UserRepository userRepository) {
        this.conversationService = conversationService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    @ModelAttribute("loginUsername")
    public String loginUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    @ModelAttribute("loginUserProfileImageUrl")
    public String loginUserProfileImageUrl() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName())
                .map(u -> u.getProfileImageUrl())
                .orElse(null);
    }

    @ModelAttribute("unreadMessageCount")
    public long unreadMessageCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0L;
        }
        return conversationService.countUnreadConversations(auth.getName());
    }
}
