package com.campus.lostfound.service;

import com.campus.lostfound.model.Student;
import com.campus.lostfound.repository.StudentRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tells Spring Security how to load a student by email for login.
 */
@Service
public class StudentUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;

    public StudentUserDetailsService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student student = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for that email."));

        return User.withUsername(student.getEmail())
                .password(student.getPassword())
                .roles("STUDENT")
                .build();
    }
}
