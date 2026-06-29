package com.example.kuwalog.controller;

import com.example.kuwalog.dto.TransactionForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.enums.ReviewType;
import com.example.kuwalog.repository.ConversationRepository;
import com.example.kuwalog.service.BeetleService;
import com.example.kuwalog.service.ReviewService;
import com.example.kuwalog.service.TransactionService;
import com.example.kuwalog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/beetles/{beetleId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final BeetleService beetleService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final ConversationRepository conversationRepository;

    public TransactionController(TransactionService transactionService,
                                 BeetleService beetleService,
                                 ReviewService reviewService,
                                 UserService userService,
                                 ConversationRepository conversationRepository) {
        this.transactionService = transactionService;
        this.beetleService = beetleService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.conversationRepository = conversationRepository;
    }

    @GetMapping
    public String list(@PathVariable Long beetleId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        model.addAttribute("beetle", beetleService.findById(beetleId));
        java.util.List<com.example.kuwalog.entity.Transaction> transactions = transactionService.findByBeetleId(beetleId);
        model.addAttribute("transactions", transactions);
        model.addAttribute("loginUsername", userDetails.getUsername());

        java.util.Map<Long, java.util.List<com.example.kuwalog.entity.Review>> reviewsMap = new java.util.HashMap<>();
        transactions.forEach(t ->
            reviewsMap.put(t.getId(), reviewService.findByTransactionId(t.getId()))
        );
        model.addAttribute("reviewsMap", reviewsMap);

        Long loginUserId = userService.findByUsername(userDetails.getUsername()).getId();
        java.util.Map<Long, java.util.Set<String>> registeredTypesMap = new java.util.HashMap<>();
        transactions.forEach(t -> {
            java.util.Set<String> names = new java.util.HashSet<>();
            reviewService.findRegisteredTypes(t.getId(), loginUserId)
                    .forEach(rt -> names.add(rt.name()));
            registeredTypesMap.put(t.getId(), names);
        });
        model.addAttribute("registeredTypesMap", registeredTypesMap);
        return "transactions/list";
    }

    @GetMapping("/new")
    public String showForm(@PathVariable Long beetleId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Beetle beetle = beetleService.findById(beetleId);
        Long ownerId = userService.findByUsername(userDetails.getUsername()).getId();
        model.addAttribute("beetle", beetle);
        model.addAttribute("transactionForm", new TransactionForm());
        model.addAttribute("inquirers", conversationRepository.findInitiatorsByBeetleIdAndOwnerId(beetleId, ownerId));
        return "transactions/new";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long beetleId,
                         @PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        transactionService.delete(beetleId, id, userDetails.getUsername());
        return "redirect:/beetles/" + beetleId + "/transactions";
    }

    @PostMapping
    public String register(@PathVariable Long beetleId,
                           @Valid @ModelAttribute("transactionForm") TransactionForm form,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {

        if (bindingResult.hasErrors()) {
            Beetle beetle = beetleService.findById(beetleId);
            Long ownerId = userService.findByUsername(userDetails.getUsername()).getId();
            model.addAttribute("beetle", beetle);
            model.addAttribute("inquirers", conversationRepository.findInitiatorsByBeetleIdAndOwnerId(beetleId, ownerId));
            return "transactions/new";
        }

        try {
            transactionService.register(beetleId, form, userDetails.getUsername());
        } catch (org.springframework.web.server.ResponseStatusException e) {
            bindingResult.rejectValue("toUsername", "error", e.getReason());
            Beetle beetle = beetleService.findById(beetleId);
            Long ownerId = userService.findByUsername(userDetails.getUsername()).getId();
            model.addAttribute("beetle", beetle);
            model.addAttribute("inquirers", conversationRepository.findInitiatorsByBeetleIdAndOwnerId(beetleId, ownerId));
            return "transactions/new";
        }

        return "redirect:/beetles/" + beetleId + "/transactions";
    }
}
