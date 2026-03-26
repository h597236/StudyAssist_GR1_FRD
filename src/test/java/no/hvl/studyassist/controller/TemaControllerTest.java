package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.TemaService;
import no.hvl.studyassist.service.BrukarService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemaController.class)
@AutoConfigureMockMvc(addFilters = false)
class TemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemaService temaService;

    @MockBean
    private BrukarService brukarService;

    @Test
    void endpoint_exists() throws Exception {

        // 🔥 Mock session brukar (ellers får du 401)
        Brukar brukar = new Brukar();
        brukar.setId(1);

        when(brukarService.findById(1)).thenReturn(brukar);

        // 🔥 Denne er KRITISK
        when(temaService.eigesAvBrukar(anyInt(), anyInt()))
                .thenReturn(true);

        when(temaService.findByEmneId(anyInt()))
                .thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("brukarId", 1);

        mockMvc.perform(get("/tema/emne/1")
                        .session(session))
                .andExpect(status().isOk());
    }
}