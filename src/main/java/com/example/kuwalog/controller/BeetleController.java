package com.example.kuwalog.controller;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.enums.Classification;
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
    public String list(@RequestParam(required = false) Classification classification,
                       @RequestParam(required = false) Sex sex,
                       @RequestParam(required = false) Stage stage,
                       @RequestParam(required = false) String locality,
                       Model model) {
        model.addAttribute("beetles", beetleService.findWithFilters(classification, sex, stage, locality));
        model.addAttribute("classificationValues", Classification.values());
        model.addAttribute("sexValues", Sex.values());
        model.addAttribute("stageValues", Stage.values());
        model.addAttribute("selectedClassification", classification);
        model.addAttribute("selectedSex", sex);
        model.addAttribute("selectedStage", stage);
        model.addAttribute("selectedLocality", locality);
        return "beetles/list";
    }

    @GetMapping("/new")
    public String showForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        BeetleForm form = new BeetleForm();
        form.setBreederName(userDetails.getUsername());
        model.addAttribute("beetleForm", form);
        addFormAttributes(model, userDetails.getUsername());
        return "beetles/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Beetle beetle = beetleService.findById(id);
        model.addAttribute("beetle", beetle);
        model.addAttribute("loginUsername", userDetails.getUsername());
        model.addAttribute("breederExists",
                beetle.getBreederName() != null && beetleService.existsByUsername(beetle.getBreederName()));
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
        form.setClassification(beetle.getClassification() != null
                ? Classification.fromLabel(beetle.getClassification()) : null);
        form.setSex(Sex.fromLabel(beetle.getSex()));
        form.setStage(Stage.fromLabel(beetle.getStage()));
        form.setGeneration(beetle.getGeneration());
        form.setLocality(beetle.getLocality());
        form.setEmergenceDate(beetle.getEmergenceDate());
        form.setBreederName(beetle.getBreederName());
        form.setDescription(beetle.getDescription());
        form.setSizeMm(beetle.getSizeMm());
        form.setWeightG(beetle.getWeightG());
        if (beetle.getFather() != null) form.setFatherId(beetle.getFather().getId());
        if (beetle.getMother() != null) form.setMotherId(beetle.getMother().getId());

        model.addAttribute("beetleForm", form);
        model.addAttribute("beetleId", id);
        addFormAttributes(model, userDetails.getUsername());
        return "beetles/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("beetleForm") BeetleForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("beetleId", id);
            addFormAttributes(model, userDetails.getUsername());
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
            addFormAttributes(model, userDetails.getUsername());
            return "beetles/form";
        }

        beetleService.post(form, userDetails.getUsername());
        return "redirect:/beetles";
    }

    private void addFormAttributes(Model model, String username) {
        model.addAttribute("classificationValues", Classification.values());
        model.addAttribute("sexValues", Sex.values());
        model.addAttribute("stageValues", Stage.values());
        model.addAttribute("fatherCandidates", beetleService.findParentCandidates(Sex.MALE, username));
        model.addAttribute("motherCandidates", beetleService.findParentCandidates(Sex.FEMALE, username));
    }
}
