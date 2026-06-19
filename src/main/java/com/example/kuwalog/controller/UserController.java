package com.example.kuwalog.controller;

import com.example.kuwalog.dto.UserRegisterForm;
import com.example.kuwalog.exception.DuplicateUserException;
import com.example.kuwalog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRegisterForm", new UserRegisterForm());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("userRegisterForm") UserRegisterForm form,
            BindingResult bindingResult) {

        // パスワード一致チェック（フォームの値が揃っている場合のみ）
        if (!bindingResult.hasFieldErrors("password")
                && !bindingResult.hasFieldErrors("passwordConfirm")
                && !form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "mismatch", "パスワードが一致しません");
        }

        if (bindingResult.hasErrors()) {
            return "users/register";
        }

        try {
            userService.register(form);
        } catch (DuplicateUserException e) {
            bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            return "users/register";
        }

        return "redirect:/login?registered";
    }
}
