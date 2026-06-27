package com.example.kuwalog.controller;

import com.example.kuwalog.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/ranking")
    public String index(Model model) {
        model.addAttribute("rankings", rankingService.getRankings());
        return "ranking/index";
    }
}
