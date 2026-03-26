package no.hvl.studyassist.service;

import no.hvl.studyassist.model.*;
import no.hvl.studyassist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SporsmalServiceTest {

    @Mock
    private SporsmalRepository sporsmalRepository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private BrukarRepository brukarRepository;

    @Mock
    private OpenAIService openAIService;

    @InjectMocks
    private SporsmalService sporsmalService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startSession_skalOppretteSession() {
        // Arrange
        Tema tema = new Tema();
        tema.setTemaId(1);

        Emne emne = new Emne();
        emne.setNamn("DAT109");
        tema.setEmne(emne);

        Brukar brukar = new Brukar();
        brukar.setId(1);

        when(temaRepository.findById(1))
                .thenReturn(Optional.of(tema));

        when(brukarRepository.findById(1))
                .thenReturn(Optional.of(brukar));

        AIResponse aiResponse = new AIResponse();
        aiResponse.setExplanation("Forklaring");
        aiResponse.setFollow_up_question("Oppfølgingsspørsmål?");

        when(openAIService.askAI(any()))
                .thenReturn(aiResponse);

        // 🔥 KRITISK FIX: sett sessionId manuelt når save() blir kalla
        when(sporsmalRepository.save(any()))
                .thenAnswer(invocation -> {
                    SporsmalSession s = invocation.getArgument(0);
                    s.setSessionId(1L);
                    return s;
                });

        // Act
        var response = sporsmalService.startSession(1, 1, "Hva er Scrum?");

        // Assert
        assertNotNull(response);
        assertNotNull(response.get("sessionId"));
        assertNotNull(response.get("oppfolgingsSporsmal"));
    }

    @Test
    void handleRefleksjon_skalReturnereVurderingOgFasit() {
        // Arrange
        Tema tema = new Tema();
        tema.setTemaId(1);
        tema.setNamn("Scrum");

        Emne emne = new Emne();
        emne.setNamn("DAT109");
        tema.setEmne(emne);

        SporsmalSession session = new SporsmalSession();
        session.setSessionId(1L);
        session.setTema(tema);
        session.setStartSporsmal("Hva er Scrum?");
        session.setOppfolgingsSporsmal("Hva vet du om Scrum?");

        when(sporsmalRepository.findById(1L))
                .thenReturn(Optional.of(session));

        AIResponse vurderingResponse = new AIResponse();
        vurderingResponse.setExplanation("Bra svar. RATING: 4");

        AIResponse fasitResponse = new AIResponse();
        fasitResponse.setExplanation("Scrum er en smidig metode...");

        when(openAIService.askAI(any()))
                .thenReturn(vurderingResponse)
                .thenReturn(fasitResponse);

        // Act
        var response = sporsmalService.handleRefleksjon(1L, "Mitt svar");

        // Assert
        assertNotNull(response);
        assertEquals("Bra svar.", response.get("vurdering"));
        assertEquals(4, response.get("rating"));
        assertEquals("Scrum er en smidig metode...", response.get("fasit"));
    }
}