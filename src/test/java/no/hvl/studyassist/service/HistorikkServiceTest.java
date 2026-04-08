package no.hvl.studyassist.service;

import no.hvl.studyassist.model.*;
import no.hvl.studyassist.repository.SporsmalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistorikkServiceTest {

    @Mock
    private SporsmalRepository sporsmalRepository;

    @InjectMocks
    private HistorikkService historikkService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private SporsmalSession lagSession(Long id, String sporsmal, String refleksjon, SporsmalSession.SessionState state) {
        Emne emne = new Emne();
        emne.setEmneId(1);
        emne.setNamn("DAT109");

        Tema tema = new Tema();
        tema.setTemaId(1);
        tema.setNamn("Scrum");
        tema.setEmne(emne);

        Brukar brukar = new Brukar();
        brukar.setId(1);

        SporsmalSession session = new SporsmalSession();
        session.setSessionId(id);
        session.setStartSporsmal(sporsmal);
        session.setBrukarRefleksjon(refleksjon);
        session.setTema(tema);
        session.setBrukar(brukar);
        session.setState(state);
        session.setRating(4);
        session.setOpprettaTid(LocalDateTime.now());
        return session;
    }

    private SporsmalSession lagSessionMedTema(Long id, String sporsmal, String refleksjon,
                                              SporsmalSession.SessionState state,
                                              String emneNamn, String temaNamn) {
        Emne emne = new Emne();
        emne.setEmneId(2);
        emne.setNamn(emneNamn);

        Tema tema = new Tema();
        tema.setTemaId(2);
        tema.setNamn(temaNamn);
        tema.setEmne(emne);

        Brukar brukar = new Brukar();
        brukar.setId(1);

        SporsmalSession session = new SporsmalSession();
        session.setSessionId(id);
        session.setStartSporsmal(sporsmal);
        session.setBrukarRefleksjon(refleksjon);
        session.setTema(tema);
        session.setBrukar(brukar);
        session.setState(state);
        session.setRating(3);
        session.setOpprettaTid(LocalDateTime.now());
        return session;
    }

    @Test
    void getHistorikk_skalReturnereAlleSessionarForBrukar() {
        when(sporsmalRepository.findByBrukarId(1))
                .thenReturn(List.of(
                        lagSession(1L, "Hva er Scrum?", "Scrum er agilt", SporsmalSession.SessionState.COMPLETED),
                        lagSession(2L, "Hva er Git?", "Git er versjonskontroll", SporsmalSession.SessionState.COMPLETED)
                ));

        var result = historikkService.getHistorikk(1);

        assertEquals(2, result.size());
        assertEquals("Hva er Scrum?", result.get(0).getStartSporsmal());
        assertEquals("DAT109", result.get(0).getEmneNamn());
        assertEquals("Scrum", result.get(0).getTemaNamn());
        assertEquals(4, result.get(0).getRating());
    }

    @Test
    void getHistorikk_tomListe_naarIngenSessionar() {
        when(sporsmalRepository.findByBrukarId(99))
                .thenReturn(List.of());

        var result = historikkService.getHistorikk(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_skalReturnereRiktigSession() {
        SporsmalSession session = lagSession(1L, "Hva er Scrum?", "Scrum er agilt", SporsmalSession.SessionState.COMPLETED);

        when(sporsmalRepository.findById(1L))
                .thenReturn(Optional.of(session));

        var result = historikkService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSessionId());
        assertEquals("Hva er Scrum?", result.getStartSporsmal());
    }

    @Test
    void getById_skalReturnereNull_naarIkkjeFunne() {
        when(sporsmalRepository.findById(999L))
                .thenReturn(Optional.empty());

        var result = historikkService.getById(999L);

        assertNull(result);
    }

    @Test
    void sokHistorikk_skalFinneSesjonarSomMatcherSporsmal() {
        SporsmalSession session1 = lagSession(1L, "Hva er Scrum?", "Scrum er agilt", SporsmalSession.SessionState.COMPLETED);
        SporsmalSession session2 = lagSessionMedTema(2L, "Hva er Git?", "Git er versjonskontroll",
                SporsmalSession.SessionState.COMPLETED, "DAT110", "Versjonskontroll");

        when(sporsmalRepository.findByBrukarId(1))
                .thenReturn(List.of(session1, session2));

        var result = historikkService.sokHistorikk(1, "scrum");

        assertEquals(1, result.size());
        assertEquals("Hva er Scrum?", result.get(0).getStartSporsmal());
    }

    @Test
    void sokHistorikk_skalFinneSesjonarSomMatcherEmne() {
        when(sporsmalRepository.findByBrukarId(1))
                .thenReturn(List.of(
                        lagSession(1L, "Hva er Scrum?", "Scrum er agilt", SporsmalSession.SessionState.COMPLETED)
                ));

        var result = historikkService.sokHistorikk(1, "dat109");

        assertEquals(1, result.size());
    }

    @Test
    void sokHistorikk_skalReturnereTomt_naarIngenMatch() {
        when(sporsmalRepository.findByBrukarId(1))
                .thenReturn(List.of(
                        lagSession(1L, "Hva er Scrum?", "Scrum er agilt", SporsmalSession.SessionState.COMPLETED)
                ));

        var result = historikkService.sokHistorikk(1, "kubernetes");

        assertTrue(result.isEmpty());
    }

    @Test
    void sokHistorikk_skalVereCase_insensitiv() {
        when(sporsmalRepository.findByBrukarId(1))
                .thenReturn(List.of(
                        lagSession(1L, "Hva er Scrum?", "Scrum er agilt", SporsmalSession.SessionState.COMPLETED)
                ));

        var result = historikkService.sokHistorikk(1, "SCRUM");

        assertEquals(1, result.size());
    }
}