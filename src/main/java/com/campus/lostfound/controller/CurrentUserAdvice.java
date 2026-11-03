package com.campus.lostfound.controller;

import com.campus.lostfound.model.Student;
import com.campus.lostfound.service.StudentService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds the signed-in student (if any) to every page as currentStudent.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    private final StudentService studentService;

    public CurrentUserAdvice(StudentService studentService) {
        this.studentService = studentService;
    }

    @ModelAttribute("currentStudent")
    public Student currentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return studentService.findByEmail(authentication.getName()).orElse(null);
    }
}
