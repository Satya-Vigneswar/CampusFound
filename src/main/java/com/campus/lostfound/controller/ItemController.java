package com.campus.lostfound.controller;

import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.model.Student;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items/{id}")
    public String details(
            @PathVariable Long id,
            @ModelAttribute("currentStudent") Student currentStudent,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return itemService.findPublicById(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    model.addAttribute("matches", itemService.findPotentialMatches(item));
                    model.addAttribute("itemOwner", itemService.isOwnedBy(item.getId(), currentStudent));
                    return "item-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "That item report is no longer available.");
                    return "redirect:/";
                });
    }
}
