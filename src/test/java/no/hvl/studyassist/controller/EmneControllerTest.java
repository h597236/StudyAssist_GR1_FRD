package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.EmneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmneController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmneService emneService;

    @MockBean
    private BrukarService brukarService;

    private Brukar mockBrukar() {
        Brukar b = new Brukar();
        b.setId(1);
        return b;
    }

    @Test
    void get_emne_for_brukar() throws Exception {

        Brukar brukar = mockBrukar();

        when(brukarService.findById(anyInt()))
                .thenReturn(brukar);

        mockMvc.perform(get("/emne/brukar/1")
                        .sessionAttr("brukarId", 1))
                .andExpect(status().isOk());
    }

    @Test
    void create_emne() throws Exception {

        Brukar brukar = mockBrukar();

        when(brukarService.findById(anyInt()))
                .thenReturn(brukar);

        mockMvc.perform(post("/emne")
                        .sessionAttr("brukarId", 1)
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