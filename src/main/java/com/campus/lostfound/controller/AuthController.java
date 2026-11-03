package com.campus.lostfound.controller;

import com.campus.lostfound.model.RegistrationForm;
import com.campus.lostfound.service.RegistrationException;
import com.campus.lostfound.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final StudentService studentService;

    public AuthController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model
    ) {
        if (isLoggedIn()) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created. You can sign in now.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        if (isLoggedIn()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegistrationForm form,
            BindingResult bindingResult
    ) {
        if (isLoggedIn()) {
            return "redirect:/dashboard";
        }

        if (form.getPassword() != null
                && form.getConfirmPassword() != null
                && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            studentService.register(form);
        } catch (RegistrationException exception) {
            bindingResult.reject("registrationError", exception.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }

    private boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
