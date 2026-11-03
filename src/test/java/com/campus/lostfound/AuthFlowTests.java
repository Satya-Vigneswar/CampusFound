package com.campus.lostfound;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.campus.lostfound.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campusfindauthtest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class AuthFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void homepageIsPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CampusFind")));
    }

    @Test
    void dashboardRedirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void reportRoutesRedirectAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/report"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void studentCanRegisterAndLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Test Student")
                        .param("email", "test.student@college.edu")
                        .param("studentId", "STU1001")
                        .param("password", "campusfind")
                        .param("confirmPassword", "campusfind"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        org.junit.jupiter.api.Assertions.assertTrue(
                studentRepository.findByEmailIgnoreCase("test.student@college.edu").isPresent()
        );

        String storedPassword = studentRepository.findByEmailIgnoreCase("test.student@college.edu")
                .orElseThrow()
                .getPassword();
        org.junit.jupiter.api.Assertions.assertFalse(storedPassword.equals("campusfind"));
        org.junit.jupiter.api.Assertions.assertTrue(storedPassword.startsWith("$2"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "test.student@college.edu")
                        .param("password", "campusfind"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void registrationRejectsShortPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Short Pass")
                        .param("email", "short.pass@college.edu")
                        .param("studentId", "STU1002")
                        .param("password", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password must be between 8 and 72 characters.")));
    }

    @Test
    void registrationShowsFriendlyMessageForDuplicateEmail() throws Exception {
        com.campus.lostfound.model.Student existing = new com.campus.lostfound.model.Student();
        existing.setName("Existing Student");
        existing.setEmail("duplicate@college.edu");
        existing.setStudentId("DUP1001");
        existing.setPassword("$2a$10$test");
        studentRepository.save(existing);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Another Student")
                        .param("email", "DUPLICATE@college.edu")
                        .param("studentId", "DUP1002")
                        .param("password", "campusfind")
                        .param("confirmPassword", "campusfind"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("An account with this email already exists.")));
    }

    @Test
    void registrationShowsFriendlyMessageForDuplicateStudentId() throws Exception {
        com.campus.lostfound.model.Student existing = new com.campus.lostfound.model.Student();
        existing.setName("Existing Student");
        existing.setEmail("unique@college.edu");
        existing.setStudentId("STU-DUPLICATE");
        existing.setPassword("$2a$10$test");
        studentRepository.save(existing);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Another Student")
                        .param("email", "another@college.edu")
                        .param("studentId", "stu-duplicate")
                        .param("password", "campusfind")
                        .param("confirmPassword", "campusfind"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("An account with this student ID already exists.")));
    }
}
