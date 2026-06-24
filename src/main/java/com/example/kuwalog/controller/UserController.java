package com.example.kuwalog.controller;

import com.example.kuwalog.dto.UserProfileForm;
import com.example.kuwalog.dto.UserRegisterForm;
import com.example.kuwalog.entity.User;
import com.example.kuwalog.exception.DuplicateUserException;
import com.example.kuwalog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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

    @GetMapping("/{username}/edit")
    public String showEditForm(@PathVariable String username,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        if (!username.equals(userDetails.getUsername())) {
            return "redirect:/users/" + username;
        }
        User user = userService.findByUsername(username);
        UserProfileForm form = new UserProfileForm();
        form.setBio(user.getBio());
        model.addAttribute("userProfileForm", form);
        model.addAttribute("username", username);
        return "users/edit";
    }

    @PostMapping("/{username}/edit")
    public String updateProfile(@PathVariable String username,
                                @Valid @ModelAttribute("userProfileForm") UserProfileForm form,
                                BindingResult bindingResult,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", username);
            return "users/edit";
        }
        userService.updateProfile(username, form, profileImage, userDetails.getUsername());
        return "redirect:/users/" + username;
    }
}
