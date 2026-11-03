package com.campus.lostfound;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemForm;
import com.campus.lostfound.model.ReportType;
import com.campus.lostfound.model.Student;
import com.campus.lostfound.repository.ItemRepository;
import com.campus.lostfound.repository.StudentRepository;
import com.campus.lostfound.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campusfinditemtest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class ItemReportingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    void cleanDatabase() {
        itemRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void reportCanBeCreatedAndBrowsedWithoutExposingReporterAccountData() throws Exception {
        createStudent("owner@college.edu", "Owner Student");

        mockMvc.perform(post("/report")
                        .with(csrf())
                        .with(user("owner@college.edu").roles("STUDENT"))
                        .param("title", "Blue water bottle")
                        .param("description", "Found beside the north library entrance.")
                        .param("category", "Drinkware")
                        .param("location", "North Library")
                        .param("reportType", "FOUND"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items/*"));
        Long itemId = itemRepository.findAll().get(0).getId();

        mockMvc.perform(get("/?q=library&type=FOUND"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Blue water bottle")))
                .andExpect(content().string(containsString("North Library")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("owner@college.edu"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Owner Student"))));
        mockMvc.perform(get("/?q=drinkware&type=FOUND"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Blue water bottle")));
        mockMvc.perform(get("/?q=library&type=LOST"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No reports match your search yet.")));

        mockMvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Found beside the north library entrance.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("owner@college.edu"))));
    }

    @Test
    @WithMockUser(username = "owner@college.edu", roles = "STUDENT")
    void invalidReportShowsFriendlyFieldValidation() throws Exception {
        createStudent("owner@college.edu", "Owner Student");

        mockMvc.perform(post("/report")
                        .with(csrf())
                        .param("title", "")
                        .param("description", "")
                        .param("category", "")
                        .param("location", "")
                        .param("reportType", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please enter an item title.")))
                .andExpect(content().string(containsString("Choose whether this item was lost or found.")));
    }

    @Test
    void authenticatedReportPagesRenderAndMissingPublicItemsShowAHelpfulMessage() throws Exception {
        createStudent("owner@college.edu", "Owner Student");

        mockMvc.perform(get("/report").with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Publish report")));
        mockMvc.perform(get("/reports").with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No reports yet")));
        mockMvc.perform(get("/dashboard").with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("YOUR PROFILE")));
        mockMvc.perform(get("/items/999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void openOppositeTypeReportsAreRankedAndClosedReportsAreIgnored() throws Exception {
        Student owner = createStudent("matching@college.edu", "Matching Student");
        Item lost = itemService.create(form("Blue water bottle"), owner);
        ItemForm foundForm = form("Blue water bottle");
        foundForm.setReportType(ReportType.FOUND);
        Item found = itemService.create(foundForm, owner);
        ItemForm closedForm = form("Blue water bottle");
        closedForm.setReportType(ReportType.FOUND);
        Item closed = itemService.create(closedForm, owner);
        closed.setStatus(com.campus.lostfound.model.ItemStatus.RESOLVED);
        itemRepository.save(closed);

        var matches = itemService.findPotentialMatches(lost);
        assertEquals(1, matches.size());
        assertEquals(found.getId(), matches.get(0).item().getId());
        assertEquals(100, matches.get(0).score());

        mockMvc.perform(get("/items/" + lost.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Potential matches")))
                .andExpect(content().string(containsString("100% similar")));
        mockMvc.perform(get("/dashboard").with(user("matching@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Potential matches")))
                .andExpect(content().string(containsString("100% similar")));
    }

    @Test
    void reportOwnerCanEditAndDeleteAndOtherStudentsCannot() throws Exception {
        Student owner = createStudent("owner@college.edu", "Owner Student");
        createStudent("other@college.edu", "Other Student");
        Item item = itemService.create(form("Original title"), owner);

        mockMvc.perform(get("/reports").with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Original title")))
                .andExpect(content().string(containsString("Delete")));
        mockMvc.perform(get("/dashboard").with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Original title")))
                .andExpect(content().string(containsString("Edit")))
                .andExpect(content().string(containsString("Delete")));
        mockMvc.perform(get("/reports/" + item.getId() + "/edit")
                        .with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Save changes")));
        mockMvc.perform(get("/items/" + item.getId())
                        .with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Edit report")));
        mockMvc.perform(get("/items/" + item.getId())
                        .with(user("other@college.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Delete report"))));

        mockMvc.perform(post("/reports/" + item.getId())
                        .with(csrf())
                        .with(user("other@college.edu").roles("STUDENT"))
                        .param("title", "Changed by another student")
                        .param("description", "Description")
                        .param("category", "Other")
                        .param("location", "Campus")
                        .param("reportType", "LOST"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));

        mockMvc.perform(get("/reports/" + item.getId() + "/edit")
                        .with(user("other@college.edu").roles("STUDENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));

        mockMvc.perform(post("/reports/" + item.getId() + "/delete")
                        .with(csrf())
                        .with(user("other@college.edu").roles("STUDENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));

        assertEquals("Original title", itemRepository.findById(item.getId()).orElseThrow().getTitle());

        mockMvc.perform(post("/reports/" + item.getId())
                        .with(csrf())
                        .with(user("owner@college.edu").roles("STUDENT"))
                        .param("title", "Updated title")
                        .param("description", "Description")
                        .param("category", "Other")
                        .param("location", "Campus")
                        .param("reportType", "LOST"))
                .andExpect(status().is3xxRedirection());

        assertEquals("Updated title", itemRepository.findById(item.getId()).orElseThrow().getTitle());

        mockMvc.perform(post("/reports/" + item.getId() + "/delete")
                        .with(csrf())
                        .with(user("owner@college.edu").roles("STUDENT")))
                .andExpect(status().is3xxRedirection());

        assertTrue(itemRepository.findById(item.getId()).isEmpty());
    }

    private Student createStudent(String email, String name) {
        Student student = new Student();
        student.setEmail(email);
        student.setName(name);
        student.setStudentId(email.substring(0, email.indexOf('@')));
        student.setPassword("test-password-hash");
        return studentRepository.save(student);
    }

    private ItemForm form(String title) {
        ItemForm form = new ItemForm();
        form.setTitle(title);
        form.setDescription("Details about the item.");
        form.setCategory("Accessories");
        form.setLocation("Main quad");
        form.setReportType(ReportType.LOST);
        return form;
    }
}
