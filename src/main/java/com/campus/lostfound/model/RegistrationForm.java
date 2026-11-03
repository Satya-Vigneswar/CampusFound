package com.campus.lostfound.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form values from the register page. This is not a database entity.
 */
public class RegistrationForm {

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 120, message = "Email is too long.")
    private String email;

    @NotBlank(message = "Student ID is required.")
    @Size(min = 3, max = 30, message = "Student ID must be between 3 and 30 characters.")
    private String studentId;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
