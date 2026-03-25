package no.hvl.studyassist.service;

import no.hvl.studyassist.model.*;
import no.hvl.studyassist.repository.BrukarRepository;
import no.hvl.studyassist.repository.SporsmalRepository;
import no.hvl.studyassist.repository.TemaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SporsmalService {

    private final SporsmalRepository sporsmalRepository;
    private final OpenAIService openAIService;
    private final TemaRepository temaRepository;
    private final BrukarRepository brukarRepository;

    public SporsmalService(SporsmalRepository sporsmalRepository,
                           OpenAIService openAIService,
                           TemaRepository temaRepository,
                           BrukarRepository brukarRepository) {
        this.sporsmalRepository = sporsmalRepository;
        this.openAIService = openAIService;
        this.temaRepository = temaRepository;
        this.brukarRepository = brukarRepository;
    }

    // Diagram 1: Start session + generate follow-up question
    public Map<String, Object> startSession(int brukarId, int temaId, String sporsmal) {
        Brukar brukar = brukarRepository.findById(brukarId).orElseThrow();
        Tema tema = temaRepository.findById(temaId).orElseThrow();

        SporsmalSession session = new SporsmalSession();
        session.setBrukar(brukar);
        session.setTema(tema);
        session.setStartSporsmal(sporsmal);
        session.setOpprettaTid(LocalDateTime.now());

        // 🔥 Start i INITIAL
        session.setState(SporsmalSession.SessionState.INITIAL);
        sporsmalRepository.save(session);

        // Ask AI for follow-up question
        AIRequest request = new AIRequest();
        request.setSubject(tema.getEmne() != null ? tema.getEmne().getNamn() : "");
        request.setTopic(tema.getNamn());
        request.setQuestion(
                "Du er ein hjelpsam lærar. Pass på at du brukar avsnitt slik at der er meir lesbart. " +
                        "Brukaren har stilt dette spørsmålet: \"" + sporsmal + "\". " +
                        "Ikkje gi svar eller forklaring. " +
                        "Still EITT sokratisk oppfølgingsspørsmål som hjelper brukaren å reflektere og vise kva dei allereie kan om temaet. " +
                        "Spørsmålet skal vere ope og krevje forklaring, ikkje ja/nei."
        );

        AIResponse aiResponse = openAIService.askAI(request);

        session.setOppfolgingsSporsmal(aiResponse.getFollow_up_question());

        // 🔥 No er vi i FOLLOW_UP
        session.setState(SporsmalSession.SessionState.FOLLOW_UP);
        sporsmalRepository.save(session);

        return Map.of(
                "sessionId", session.getSessionId(),
                "oppfolgingsSporsmal", aiResponse.getFollow_up_question() != null ? aiResponse.getFollow_up_question() : ""
        );
    }

    // Diagram 2: Handle reflection → get vurdering + fasit
    public Map<String, Object> handleRefleksjon(Long sessionId, String svar) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();

        Tema tema = session.getTema();
        String emneNamn = tema.getEmne() != null ? tema.getEmne().getNamn() : "";

        // Get vurdering
        AIRequest vurderingRequest = new AIRequest();
        vurderingRequest.setSubject(emneNamn);
        vurderingRequest.setTopic(tema.getNamn());
        vurderingRequest.setQuestion(
                "Du er ein faglærar. Pass på at du brukar avsnitt slik at der er meir lesbart. " +
                        "Originalt spørsmål: \"" + session.getStartSporsmal() + "\". " +
                        "Oppfølgingsspørsmål: \"" + session.getOppfolgingsSporsmal() + "\". " +
                        "Brukarens svar: \"" + svar + "\". " +
                        "Gi ei kort, konkret og konstruktiv vurdering. " +
                        "Sei kva som er bra og kva som kan forbedrast. " +
                        "Avslutt med: RATING: X (1-5), der 5 er svært bra."
        );

        AIResponse vurderingResponse = openAIService.askAI(vurderingRequest);
        String vurdering = vurderingResponse.getExplanation();

        Integer rating = extractRating(vurdering);
        session.setRating(rating);

        // Get fasit
        AIRequest fasitRequest = new AIRequest();
        fasitRequest.setSubject(emneNamn);
        fasitRequest.setTopic(tema.getNamn());
        fasitRequest.setQuestion(
                "Du er ein faglærar. Pass på at du brukar avsnitt slik at der er meir lesbart. " +
                        "Brukaren stilte dette originale spørsmålet: \"" + session.getStartSporsmal() + "\". " +
                        "Gi eit klart, korrekt og pedagogisk svar på dette spørsmålet."
        );

        AIResponse fasitResponse = openAIService.askAI(fasitRequest);
        String fasit = fasitResponse.getExplanation();

        // 🔥 Lagre ALT samtidig (ingen ANSWERED state)
        session.setBrukarRefleksjon(svar);
        String cleanText = vurdering.replaceAll("RATING:\\s*\\d", "").trim();
        session.setVurdering(cleanText);
        session.setFasitSvar(fasit);
        session.setState(SporsmalSession.SessionState.FINAL_ANSWER);

        sporsmalRepository.save(session);

        return Map.of(
                "vurdering", cleanText,
                "rating", rating,
                "fasit", fasit != null ? fasit : ""
        );
    }

    // Diagram 3: Handle tilbakemelding → mark session COMPLETED
    public void handleTilbakemelding(Long sessionId, String tekst) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();
        session.setState(SporsmalSession.SessionState.COMPLETED);
        sporsmalRepository.save(session);
    }

    private Integer extractRating(String text) {
        if (text == null) return null;

        java.util.regex.Matcher match = java.util.regex.Pattern
                .compile("RATING:\\s*(\\d)")
                .matcher(text);

        if (match.find()) {
            int rating = Integer.parseInt(match.group(1));

            if (rating >= 1 && rating <= 5) {
                return rating;
            }
        }

        return null;
    }

    public long getSessionCount(int brukarId) {
        return sporsmalRepository.countByBrukarId(brukarId);
    }
}