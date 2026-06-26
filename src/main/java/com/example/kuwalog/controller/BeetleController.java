package com.example.kuwalog.controller;

import com.example.kuwalog.dto.BeetleForm;
import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.enums.Classification;
import com.example.kuwalog.entity.enums.Sex;
import com.example.kuwalog.entity.enums.Stage;
import com.example.kuwalog.entity.BeetleImage;
import com.example.kuwalog.repository.BeetleImageRepository;
import com.example.kuwalog.service.BeetleImageService;
import com.example.kuwalog.service.BeetleService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/beetles")
public class BeetleController {

    private final BeetleService beetleService;
    private final BeetleImageService beetleImageService;
    private final BeetleImageRepository beetleImageRepository;
    private final com.example.kuwalog.service.FavoriteService favoriteService;

    public BeetleController(BeetleService beetleService, BeetleImageService beetleImageService,
                            BeetleImageRepository beetleImageRepository,
                            com.example.kuwalog.service.FavoriteService favoriteService) {
        this.beetleService = beetleService;
        this.beetleImageService = beetleImageService;
        this.beetleImageRepository = beetleImageRepository;
        this.favoriteService = favoriteService;
    }

    @org.springframework.web.bind.annotation.InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Classification.class, new java.beans.PropertyEditorSupport() {
            @Override public void setAsText(String text) {
                setValue(text == null || text.isEmpty() ? null : Classification.valueOf(text));
            }
        });
    }

    @GetMapping
    public String list(@RequestParam(required = false) Classification classification,
                       @RequestParam(required = false) Sex sex,
                       @RequestParam(required = false) Stage stage,
                       @RequestParam(required = false) String locality,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Beetle> beetlePage = beetleService.findWithFilters(classification, sex, stage, locality, page);
        Map<Long, String> imageMap = new java.util.HashMap<>();
        for (Beetle b : beetlePage.getContent()) {
            beetleImageRepository.findFirstByBeetleOrderBySortOrderAsc(b)
                    .map(BeetleImage::getImageUrl)
                    .ifPresent(url -> imageMap.put(b.getId(), url));
        }
        model.addAttribute("beetlePage", beetlePage);
        model.addAttribute("imageMap", imageMap);
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
        String username = userDetails != null ? userDetails.getUsername() : null;
        model.addAttribute("beetle", beetle);
        model.addAttribute("loginUsername", username);
        model.addAttribute("breederExists",
                beetle.getBreederName() != null && beetleService.existsByUsername(beetle.getBreederName()));
        model.addAttribute("images", beetleImageService.findByBeetle(beetle));
        model.addAttribute("favoriteCount", favoriteService.countByBeetleId(id));
        model.addAttribute("favorited", favoriteService.isFavorited(id, username));
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
        model.addAttribute("images", beetleImageService.findByBeetle(beetle));
        addFormAttributes(model, userDetails.getUsername(), id);
        return "beetles/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("beetleForm") BeetleForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "images", required = false) List<MultipartFile> images,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Beetle beetle = beetleService.findById(id);
            model.addAttribute("beetleId", id);
            model.addAttribute("images", beetleImageService.findByBeetle(beetle));
            addFormAttributes(model, userDetails.getUsername(), id);
            return "beetles/edit";
        }

        beetleService.update(id, form, userDetails.getUsername());
        beetleImageService.uploadAll(id, images, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("updated", true);
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
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addFormAttributes(model, userDetails.getUsername());
            return "beetles/form";
        }

        Beetle beetle = beetleService.post(form, userDetails.getUsername());
        beetleImageService.uploadAll(beetle.getId(), images, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("posted", true);
        return "redirect:/beetles/" + beetle.getId();
    }

    private void addFormAttributes(Model model, String username) {
        addFormAttributes(model, username, null);
    }

    private void addFormAttributes(Model model, String username, Long excludeId) {
        model.addAttribute("classificationValues", Classification.values());
        model.addAttribute("sexValues", Sex.values());
        model.addAttribute("stageValues", Stage.values());
        List<Beetle> fathers = beetleService.findParentCandidates(Sex.MALE, username);
        List<Beetle> mothers = beetleService.findParentCandidates(Sex.FEMALE, username);
        if (excludeId != null) {
            fathers = fathers.stream().filter(b -> !b.getId().equals(excludeId)).toList();
            mothers = mothers.stream().filter(b -> !b.getId().equals(excludeId)).toList();
        }
        model.addAttribute("fatherCandidates", fathers);
        model.addAttribute("motherCandidates", mothers);
    }
}
