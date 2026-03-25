package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.repository.BrukarRepository;
import no.hvl.studyassist.repository.EmneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmneService {

    private final EmneRepository emneRepository;
    private final BrukarRepository brukarRepository;

    public EmneService(EmneRepository emneRepository, BrukarRepository brukarRepository) {
        this.emneRepository = emneRepository;
        this.brukarRepository = brukarRepository;
    }

    public Emne save(Emne emne) {

        if (emne.getBrukar() != null && emne.getBrukar().getId() != 0) {

            Brukar brukar = brukarRepository.findById(emne.getBrukar().getId())
                    .orElseThrow(() -> new RuntimeException("Brukar finst ikkje"));

            emne.setBrukar(brukar);
        }

        return emneRepository.save(emne);
    }

    public List<Emne> findByBrukarId(int brukarId) {
        return emneRepository.findByBrukar_Id(brukarId);
    }

    public boolean eigesAvBrukar(int emneId, int brukarId) {
        return emneRepository.findById(emneId)
                .map(emne -> emne.getBrukar() != null && emne.getBrukar().getId() == brukarId)
                .orElse(false);
    }

    public void deleteById(int emneId) {
        emneRepository.deleteById(emneId);
    }
}
