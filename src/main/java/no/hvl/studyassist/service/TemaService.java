package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.repository.EmneRepository;
import no.hvl.studyassist.repository.SporsmalRepository;
import no.hvl.studyassist.repository.TemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TemaService {

    private final TemaRepository temaRepository;
    private final EmneRepository emneRepository;
    private final SporsmalRepository sporsmalRepository;

    public TemaService(TemaRepository temaRepository, EmneRepository emneRepository, SporsmalRepository sporsmalRepository) {
        this.temaRepository = temaRepository;
        this.emneRepository = emneRepository;
        this.sporsmalRepository = sporsmalRepository;
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

    public Tema findById(int temaId) {
        return temaRepository.findById(temaId).orElse(null);
    }

    @Transactional
    public void deleteById(int temaId) {
        sporsmalRepository.deleteByTemaTemaId(temaId);
        temaRepository.deleteById(temaId);
    }
}
