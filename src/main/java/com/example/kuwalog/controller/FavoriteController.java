package com.example.kuwalog.controller;

import com.example.kuwalog.service.FavoriteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/beetles/{beetleId}/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public String toggle(@PathVariable Long beetleId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        favoriteService.toggle(beetleId, userDetails.getUsername());
        return "redirect:/beetles/" + beetleId;
    }
}
