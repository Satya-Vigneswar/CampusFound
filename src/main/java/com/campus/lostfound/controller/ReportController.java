package com.campus.lostfound.controller;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemForm;
import com.campus.lostfound.model.Student;
import com.campus.lostfound.service.ItemNotFoundException;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReportController {

    private final ItemService itemService;
    private final StudentService studentService;

    public ReportController(ItemService itemService, StudentService studentService) {
        this.itemService = itemService;
        this.studentService = studentService;
    }

    @GetMapping("/report")
    public String newReport(Model model) {
        model.addAttribute("form", new ItemForm());
        model.addAttribute("editing", false);
        model.addAttribute("formAction", "/report");
        return "item-form";
    }

    @PostMapping("/report")
    public String createReport(
            @Valid @ModelAttribute("form") ItemForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "item-form";
        }
        Student student = currentStudent(authentication);
        Item item = itemService.create(form, student);
        redirectAttributes.addFlashAttribute("successMessage", "Your report has been posted.");
        return "redirect:/items/" + item.getId();
    }

    @GetMapping("/reports")
    public String myReports(Authentication authentication, Model model) {
        model.addAttribute("reports", itemService.findOwnedBy(currentStudent(authentication)));
        return "reports";
    }

    @GetMapping("/reports/{id}/edit")
    public String editReport(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Item item = itemService.findOwnedById(id, currentStudent(authentication));
            model.addAttribute("form", ItemForm.from(item));
            model.addAttribute("itemId", item.getId());
            model.addAttribute("editing", true);
            model.addAttribute("formAction", "/reports/" + item.getId());
            return "item-form";
        } catch (AccessDeniedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own reports.");
        } catch (ItemNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "That report could not be found.");
        }
        return "redirect:/reports";
    }

    @PostMapping("/reports/{id}")
    public String updateReport(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ItemForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", id);
            model.addAttribute("editing", true);
            model.addAttribute("formAction", "/reports/" + id);
            return "item-form";
        }
        try {
            itemService.update(id, form, currentStudent(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Your report has been updated.");
        } catch (AccessDeniedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own reports.");
            return "redirect:/reports";
        } catch (ItemNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "That report could not be found.");
            return "redirect:/reports";
        }
        return "redirect:/reports";
    }

    @PostMapping("/reports/{id}/delete")
    public String deleteReport(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            itemService.delete(id, currentStudent(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Your report has been deleted.");
        } catch (AccessDeniedException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only delete your own reports.");
        } catch (ItemNotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "That report could not be found.");
        }
        return "redirect:/reports";
    }

    private Student currentStudent(Authentication authentication) {
        return studentService.findByEmail(authentication.getName()).orElseThrow();
    }
}
