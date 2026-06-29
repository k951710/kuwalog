package com.example.kuwalog.controller;

import com.example.kuwalog.dto.UserProfileDto;
import com.example.kuwalog.entity.BeetleImage;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.repository.BeetleImageRepository;
import com.example.kuwalog.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final BeetleImageRepository beetleImageRepository;

    public UserProfileController(UserProfileService userProfileService,
                                 BeetleImageRepository beetleImageRepository) {
        this.userProfileService = userProfileService;
        this.beetleImageRepository = beetleImageRepository;
    }

    @GetMapping("/{username}")
    public String profile(@PathVariable String username,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        UserProfileDto profile = userProfileService.getProfile(username);
        Map<Long, String> imageMap = new HashMap<>();
        for (Beetle b : profile.getBeetles()) {
            beetleImageRepository.findFirstByBeetleOrderBySortOrderAsc(b)
                    .map(BeetleImage::getImageUrl)
                    .ifPresent(url -> imageMap.put(b.getId(), url));
        }
        model.addAttribute("profile", profile);
        model.addAttribute("imageMap", imageMap);
        model.addAttribute("loginUsername", userDetails != null ? userDetails.getUsername() : null);
        return "users/profile";
    }
}
