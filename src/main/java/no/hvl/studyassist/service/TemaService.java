package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.repository.EmneRepository;
import no.hvl.studyassist.repository.TemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemaService {

    private final TemaRepository temaRepository;
    private final EmneRepository emneRepository;

    public TemaService(TemaRepository temaRepository, EmneRepository emneRepository) {
        this.temaRepository = temaRepository;
        this.emneRepository = emneRepository;
    }

    public Tema save(Tema tema) {
        return temaRepository.save(tema);
    }

    public List<Tema> findByEmneId(int emneId) {
        return temaRepository.findByEmneEmneId(emneId);
    }

    public boolean eigesAvBrukar(int emneId, int brukarId) {
        return emneRepository.findById(emneId)
                .map(emne -> emne.getBrukar() != null && emne.getBrukar().getId() == brukarId)
                .orElse(false);
    }

    public Emne findEmneById(int emneId) {
        return emneRepository.findById(emneId).orElse(null);
    }
}
