package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.model.Historikk;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.HistorikkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistorikkController.class)
@AutoConfigureMockMvc(addFilters = false)
class HistorikkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistorikkService historikkService;

    @MockBean
    private BrukarService brukarService;

    private Brukar mockBrukar() {
        Brukar b = new Brukar();
        b.setId(1);
        b.setEmail("test@test.no");
        return b;
    }

    private Historikk mockHistorikk() {
        return new Historikk(
                1L,
                "Hva er Scrum?",
                "Hva vet du om Scrum?",
                "Scrum er agilt",
                "Bra svar",
                "Scrum er en smidig metode",
                4,
                "1",
                "Scrum",
                "1",
                "DAT109",
                LocalDateTime.now(),
                "COMPLETED"
        );
    }

    @Test
    void getHistorikk_skalReturnere200_naarLoggatInn() throws Exception {
        when(brukarService.findById(1)).thenReturn(mockBrukar());
        when(historikkService.getHistorikk(1)).thenReturn(List.of(mockHistorikk()));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("brukarId", 1);

        mockMvc.perform(get("/api/historikk").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startSporsmal").value("Hva er Scrum?"))
                .andExpect(jsonPath("$[0].emneNamn").value("DAT109"))
                .andExpect(jsonPath("$[0].rating").value(4));
    }

    @Test
    void getHistorikk_skalReturnere401_naarIkkjeLoggatInn() throws Exception {
        mockMvc.perform(get("/api/historikk"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_skalReturnere200_naarSessionFinst() throws Exception {
        when(brukarService.findById(1)).thenReturn(mockBrukar());
        when(historikkService.getById(1L)).thenReturn(mockHistorikk());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("brukarId", 1);

        mockMvc.perform(get("/api/historikk/1").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startSporsmal").value("Hva er Scrum?"));
    }

    @Test
    void getById_skalReturnere404_naarSessionIkkjeFinst() throws Exception {
        when(brukarService.findById(1)).thenReturn(mockBrukar());
        when(historikkService.getById(999L)).thenReturn(null);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("brukarId", 1);

        mockMvc.perform(get("/api/historikk/999").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void sokHistorikk_skalReturnereResultat() throws Exception {
        when(brukarService.findById(1)).thenReturn(mockBrukar());
        when(historikkService.sokHistorikk(eq(1), eq("scrum")))
                .thenReturn(List.of(mockHistorikk()));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("brukarId", 1);

        mockMvc.perform(get("/api/historikk/sok")
                        .param("q", "scrum")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startSporsmal").value("Hva er Scrum?"));
    }

    @Test
    void sokHistorikk_skalReturnere401_naarIkkjeLoggatInn() throws Exception {
        mockMvc.perform(get("/api/historikk/sok").param("q", "scrum"))
                .andExpect(status().isUnauthorized());
    }
}