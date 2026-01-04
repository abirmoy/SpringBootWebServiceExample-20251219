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
class BasicSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCanAccessStudentsPage() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotAccessTeachersPage() throws Exception {
        mockMvc.perform(get("/teachers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacherCanAccessTeachersPage() throws Exception {
        mockMvc.perform(get("/teachers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAllPages() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/teachers"))
                .andExpect(status().isOk());
    }

    @Test
    void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}