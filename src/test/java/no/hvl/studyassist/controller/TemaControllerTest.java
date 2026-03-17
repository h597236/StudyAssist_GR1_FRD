package no.hvl.studyassist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import no.hvl.studyassist.service.TemaService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemaController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 disable security
class TemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemaService temaService; // 🔥 REQUIRED

    @Test
    void get_tema_for_emne() throws Exception {
        mockMvc.perform(get("/tema/emne/1"))
                .andExpect(status().isOk());
    }
}