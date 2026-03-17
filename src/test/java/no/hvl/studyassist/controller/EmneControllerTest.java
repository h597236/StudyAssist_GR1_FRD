package no.hvl.studyassist.controller;

import no.hvl.studyassist.service.EmneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmneController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 THIS FIXES 401/403
class EmneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmneService emneService;

    @Test
    void get_emne_for_brukar() throws Exception {
        mockMvc.perform(get("/emne/brukar/1"))
                .andExpect(status().isOk());
    }

    @Test
    void create_emne() throws Exception {
        mockMvc.perform(post("/emne")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "namn": "DAT109",
                      "beskrivelse": "Test"
                    }
                """))
                .andExpect(status().isOk());
    }
}