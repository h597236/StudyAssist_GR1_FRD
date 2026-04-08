package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.repository.BrukarRepository;
import no.hvl.studyassist.repository.EmneRepository;
import no.hvl.studyassist.repository.SporsmalRepository;
import no.hvl.studyassist.repository.TemaRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmneService {

    private final EmneRepository emneRepository;
    private final BrukarRepository brukarRepository;
    private final TemaRepository temaRepository;
    private final SporsmalRepository sporsmalRepository;

    public EmneService(EmneRepository emneRepository, BrukarRepository brukarRepository, TemaRepository temaRepository, SporsmalRepository sporsmalRepository) {
        this.emneRepository = emneRepository;
        this.brukarRepository = brukarRepository;
        this.temaRepository = temaRepository;
        this.sporsmalRepository = sporsmalRepository;
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

    @Transactional
    public void deleteById(int emneId) {
        List<Tema> temaListe = temaRepository.findByEmneEmneId(emneId);
        for (Tema tema : temaListe) {
            sporsmalRepository.deleteByTemaTemaId(tema.getTemaId());
        }
        temaRepository.deleteAll(temaListe);
        emneRepository.deleteById(emneId);
    }
}
