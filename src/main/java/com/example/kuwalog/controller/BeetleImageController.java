package com.example.kuwalog.controller;

import com.example.kuwalog.service.BeetleImageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/beetles/{beetleId}/images")
public class BeetleImageController {

    private final BeetleImageService beetleImageService;

    public BeetleImageController(BeetleImageService beetleImageService) {
        this.beetleImageService = beetleImageService;
    }

    @PostMapping
    public String upload(@PathVariable Long beetleId,
                         @RequestParam("file") MultipartFile file,
                         @AuthenticationPrincipal UserDetails userDetails) {
        beetleImageService.upload(beetleId, file, userDetails.getUsername());
        return "redirect:/beetles/" + beetleId;
    }

    @PostMapping("/{imageId}/primary")
    public String setPrimary(@PathVariable Long beetleId,
                             @PathVariable Long imageId,
                             @AuthenticationPrincipal UserDetails userDetails) {
        beetleImageService.setPrimary(beetleId, imageId, userDetails.getUsername());
        return "redirect:/beetles/" + beetleId;
    }

    @PostMapping("/{imageId}/delete")
    public String delete(@PathVariable Long beetleId,
                         @PathVariable Long imageId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        beetleImageService.delete(beetleId, imageId, userDetails.getUsername());
        return "redirect:/beetles/" + beetleId;
    }
}
