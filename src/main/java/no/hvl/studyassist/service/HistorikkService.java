package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Historikk;
import no.hvl.studyassist.model.SporsmalSession;
import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.repository.SporsmalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistorikkService {

    private final SporsmalRepository sporsmalRepository;

    public HistorikkService(SporsmalRepository sporsmalRepository) {
        this.sporsmalRepository = sporsmalRepository;
    }

    public List<Historikk> getHistorikk(int brukarId) {
        return sporsmalRepository.findByBrukarId(brukarId)
                .stream()
                .map(this::toHistorikk)
                .collect(Collectors.toList());
    }

    public Historikk getById(Long sessionId) {
        return sporsmalRepository.findById(sessionId)
                .map(this::toHistorikk)
                .orElse(null);
    }

    public List<Historikk> sokHistorikk(int brukarId, String query) {
        String q = query.toLowerCase();
        return getHistorikk(brukarId).stream()
                .filter(h ->
                        contains(h.getStartSporsmal(), q) ||
                                contains(h.getBrukarRefleksjon(), q) ||
                                contains(h.getFasitSvar(), q) ||
                                contains(h.getTemaNamn(), q) ||
                                contains(h.getEmneNamn(), q)
                )
                .collect(Collectors.toList());
    }

    private boolean contains(String text, String query) {
        return text != null && text.toLowerCase().contains(query);
    }

    private Historikk toHistorikk(SporsmalSession session) {
        Tema tema = session.getTema();
        Emne emne = tema != null ? tema.getEmne() : null;

        return new Historikk(
                session.getSessionId(),
                session.getStartSporsmal(),
                session.getOppfolgingsSporsmal(),
                session.getBrukarRefleksjon(),
                session.getVurdering(),
                session.getFasitSvar(),
                session.getRating(),
                tema != null ? String.valueOf(tema.getTemaId()) : null,
                tema != null ? tema.getNamn() : null,
                emne != null ? String.valueOf(emne.getEmneId()) : null,
                emne != null ? emne.getNamn() : null,
                session.getOpprettaTid(),
                session.getState() != null ? session.getState().name() : null
        );
    }
}