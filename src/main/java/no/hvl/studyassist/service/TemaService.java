package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.repository.TemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemaService {

    private final TemaRepository temaRepository;

    public TemaService(TemaRepository temaRepository) {
        this.temaRepository = temaRepository;
    }

    public Tema lagreTema(Tema tema) {
        return temaRepository.save(tema);
    }

    public List<Tema> hentTemaForEmne(Integer emneId) {
        return temaRepository.findByEmneEmneId(emneId);
    }
}