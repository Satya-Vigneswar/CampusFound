package com.campus.lostfound.controller;

import com.campus.lostfound.model.Student;
import com.campus.lostfound.model.ItemMatch;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.StudentService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StudentService studentService;
    private final ItemService itemService;

    public DashboardController(StudentService studentService, ItemService itemService) {
        this.studentService = studentService;
        this.itemService = itemService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Student student = studentService.findByEmail(authentication.getName()).orElseThrow();
        var reports = itemService.findOwnedBy(student);
        Map<Long, List<ItemMatch>> matchesByReportId = new LinkedHashMap<>();
        reports.forEach(report -> matchesByReportId.put(
                report.getId(), itemService.findPotentialMatches(report)));
        model.addAttribute("reports", reports);
        model.addAttribute("matchesByReportId", matchesByReportId);
        return "dashboard";
    }
}
