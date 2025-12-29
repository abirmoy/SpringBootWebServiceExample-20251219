package com.example.ClassRosterWebService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------
    // UNAUTHENTICATED
    // -------------------------

    @Test
    void unauthenticated_courses_redirects_to_login() throws Exception {
        mockMvc.perform(get("/courses/list"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void unauthenticated_admin_redirects_to_login() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    // -------------------------
    // STUDENT ROLE
    // -------------------------

    @Test
    @WithMockUser(roles = "STUDENT")
    void student_access_students_allowed_but_not_found() throws Exception {
        mockMvc.perform(get("/students/profile"))
                .andExpect(status().isNotFound()); // SECURITY PASSED
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void student_access_courses_forbidden() throws Exception {
        mockMvc.perform(get("/courses/list"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // TEACHER ROLE
    // -------------------------

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacher_access_courses_allowed_but_not_found() throws Exception {
        mockMvc.perform(get("/courses/list"))
                .andExpect(status().isNotFound()); // SECURITY PASSED
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacher_access_admin_forbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // ADMIN ROLE
    // -------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_access_admin_allowed_but_not_found() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isNotFound()); // SECURITY PASSED
    }
}
