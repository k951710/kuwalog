package com.example.kuwalog.controller;

import com.example.kuwalog.dto.UserProfileDto;
import com.example.kuwalog.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{username}")
    public String profile(@PathVariable String username,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        model.addAttribute("profile", userProfileService.getProfile(username));
        model.addAttribute("loginUsername", userDetails.getUsername());
        return "users/profile";
    }
}
