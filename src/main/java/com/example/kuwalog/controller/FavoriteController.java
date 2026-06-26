package com.example.kuwalog.controller;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.BeetleImage;
import com.example.kuwalog.repository.BeetleImageRepository;
import com.example.kuwalog.service.FavoriteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final BeetleImageRepository beetleImageRepository;

    public FavoriteController(FavoriteService favoriteService,
                              BeetleImageRepository beetleImageRepository) {
        this.favoriteService = favoriteService;
        this.beetleImageRepository = beetleImageRepository;
    }

    @GetMapping("/favorites")
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Beetle> beetles = favoriteService.findFavoritedBeetles(userDetails.getUsername());
        Map<Long, String> imageMap = new HashMap<>();
        for (Beetle b : beetles) {
            beetleImageRepository.findFirstByBeetleOrderBySortOrderAsc(b)
                    .map(BeetleImage::getImageUrl)
                    .ifPresent(url -> imageMap.put(b.getId(), url));
        }
        model.addAttribute("beetles", beetles);
        model.addAttribute("imageMap", imageMap);
        return "favorites/list";
    }

    @PostMapping("/beetles/{beetleId}/favorite")
    public String toggle(@PathVariable Long beetleId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        favoriteService.toggle(beetleId, userDetails.getUsername());
        return "redirect:/beetles/" + beetleId;
    }
}
