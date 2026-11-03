package com.campus.lostfound.controller;

import com.campus.lostfound.model.ReportType;
import com.campus.lostfound.service.ItemService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ItemService itemService;

    public HomeController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ALL") String type,
            Model model
    ) {
        ReportType reportType = null;
        if ("LOST".equalsIgnoreCase(type) || "FOUND".equalsIgnoreCase(type)) {
            reportType = ReportType.valueOf(type.toUpperCase());
        } else {
            type = "ALL";
        }
        model.addAttribute("items", itemService.search(q, reportType));
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("type", type.toUpperCase());
        return "index";
    }
}
