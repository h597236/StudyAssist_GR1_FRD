package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.repository.EmneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmneService {

    private final EmneRepository emneRepository;

    public EmneService(EmneRepository emneRepository) {
        this.emneRepository = emneRepository;
    }

    public Emne lagreEmne(Emne emne) {
        return emneRepository.save(emne);
    }

    public List<Emne> hentAlleEmner() {
        return emneRepository.findAll();
    }
}