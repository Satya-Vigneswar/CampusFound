package com.campus.lostfound.service;

import com.campus.lostfound.model.RegistrationForm;
import com.campus.lostfound.model.Student;
import com.campus.lostfound.repository.StudentRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new student after checking uniqueness. The password is hashed before save.
     */
    @Transactional
    public Student register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase();
        String studentId = form.getStudentId().trim();

        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new RegistrationException("An account with this email already exists.");
        }
        if (studentRepository.existsByStudentIdIgnoreCase(studentId)) {
            throw new RegistrationException("An account with this student ID already exists.");
        }

        Student student = new Student();
        student.setName(form.getName().trim());
        student.setEmail(email);
        student.setStudentId(studentId);
        student.setPassword(passwordEncoder.encode(form.getPassword()));

        return studentRepository.save(student);
    }

    public Optional<Student> findByEmail(String email) {
        return studentRepository.findByEmailIgnoreCase(email);
    }
}
