package com.example.kuwalog.controller;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import com.example.kuwalog.service.BeetleService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/beetles")
public class BeetleController {

    private final BeetleService beetleService;

    public BeetleController(BeetleService beetleService) {
        this.beetleService = beetleService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("beetles", beetleService.findByUsername(userDetails.getUsername()));
        return "beetles/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("beetleForm", new BeetleForm());
        model.addAttribute("sexValues", Sex.values());
        model.addAttribute("stageValues", Stage.values());
        return "beetles/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        model.addAttribute("beetle", beetleService.findById(id));
        model.addAttribute("loginUsername", userDetails.getUsername());
        return "beetles/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Beetle beetle = beetleService.findById(id);

        if (!beetle.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/beetles/" + id;
        }

        BeetleForm form = new BeetleForm();
        form.setName(beetle.getName());
        form.setSex(Sex.fromLabel(beetle.getSex()));
        form.setStage(Stage.fromLabel(beetle.getStage()));
        form.setGeneration(beetle.getGeneration());
        form.setLocality(beetle.getLocality());
        form.setEmergenceDate(beetle.getEmergenceDate());
        form.setDescription(beetle.getDescription());

        model.addAttribute("beetleForm", form);
        model.addAttribute("sexValues", Sex.values());
        model.addAttribute("stageValues", Stage.values());
        model.addAttribute("beetleId", id);
        return "beetles/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("beetleForm") BeetleForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("sexValues", Sex.values());
            model.addAttribute("stageValues", Stage.values());
            model.addAttribute("beetleId", id);
            return "beetles/edit";
        }

        beetleService.update(id, form, userDetails.getUsername());
        return "redirect:/beetles/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        beetleService.delete(id, userDetails.getUsername());
        return "redirect:/beetles";
    }

    @PostMapping
    public String post(
            @Valid @ModelAttribute("beetleForm") BeetleForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("sexValues", Sex.values());
            model.addAttribute("stageValues", Stage.values());
            return "beetles/form";
        }

        beetleService.post(form, userDetails.getUsername());
        return "redirect:/beetles";
    }
}
