package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.repository.TemaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TemaServiceTest {

    @Mock
    private TemaRepository temaRepository;

    @InjectMocks
    private TemaService temaService;

    public TemaServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void hentTema_skalReturnereTema() {
        Tema tema = new Tema();
        tema.setTemaId(1);
        tema.setNamn("Scrum");

        when(temaRepository.findByEmneEmneId(1))
                .thenReturn(java.util.List.of(tema));

        var result = temaService.findByEmneId(1);

        assertEquals(1, result.size());
        assertEquals("Scrum", result.get(0).getNamn());
    }
}