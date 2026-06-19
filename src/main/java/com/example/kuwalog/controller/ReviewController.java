package com.example.kuwalog.controller;

import com.example.kuwalog.dto.ReviewForm;
import com.example.kuwalog.entity.Transaction;
import com.example.kuwalog.entity.enums.ReviewType;
import com.example.kuwalog.service.ReviewService;
import com.example.kuwalog.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transactions/{transactionId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final TransactionService transactionService;

    public ReviewController(ReviewService reviewService, TransactionService transactionService) {
        this.reviewService = reviewService;
        this.transactionService = transactionService;
    }

    @GetMapping("/new")
    public String showForm(@PathVariable Long transactionId,
                           @RequestParam(required = false) ReviewType reviewType,
                           Model model) {
        ReviewForm form = new ReviewForm();
        form.setReviewType(reviewType);
        model.addAttribute("transaction", transactionService.findById(transactionId));
        model.addAttribute("reviewForm", form);
        return "reviews/new";
    }

    @PostMapping
    public String register(@PathVariable Long transactionId,
                           @Valid @ModelAttribute("reviewForm") ReviewForm form,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("transaction", transactionService.findById(transactionId));
            return "reviews/new";
        }

        try {
            reviewService.register(transactionId, form, userDetails.getUsername());
        } catch (org.springframework.web.server.ResponseStatusException e) {
            model.addAttribute("errorMessage", e.getReason());
            model.addAttribute("transaction", transactionService.findById(transactionId));
            return "reviews/new";
        }

        Transaction transaction = transactionService.findById(transactionId);
        return "redirect:/beetles/" + transaction.getBeetle().getId() + "/transactions";
    }
}
