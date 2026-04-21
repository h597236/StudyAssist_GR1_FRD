package no.hvl.studyassist.service;

import no.hvl.studyassist.model.*;
import no.hvl.studyassist.repository.BrukarRepository;
import no.hvl.studyassist.repository.SporsmalRepository;
import no.hvl.studyassist.repository.TemaRepository;
import no.hvl.studyassist.service.ai.AiModelService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SporsmalService {

    private final SporsmalRepository sporsmalRepository;
    private final AiModelService aiModelService;
    private final TemaRepository temaRepository;
    private final BrukarRepository brukarRepository;
    private final AdminPromptService adminPromptService;

    public SporsmalService(SporsmalRepository sporsmalRepository,
                           AiModelService aiModelService,
                           TemaRepository temaRepository,
                           BrukarRepository brukarRepository,
                           AdminPromptService adminPromptService) {
        this.sporsmalRepository = sporsmalRepository;
        this.aiModelService = aiModelService;
        this.temaRepository = temaRepository;
        this.brukarRepository = brukarRepository;
        this.adminPromptService = adminPromptService;
    }

    private static final String FALLBACK_OPPFOLGINGSSPORSMAL =
            "Du er ein hjelpsam lærar. Pass på at du brukar avsnitt slik at der er meir lesbart. " +
                    "Brukaren har stilt dette spørsmålet: \"{{sporsmal}}\". " +
                    "{{laeringsmaal}}" +
                    "Ikkje gi svar eller forklaring. " +
                    "Still EITT sokratisk oppfølgingsspørsmål som hjelper brukaren å reflektere og vise kva dei allereie kan om temaet. " +
                    "Spørsmålet skal vere ope og krevje forklaring, ikkje ja/nei.";

    private static final String FALLBACK_VURDERING =
            "Du er ein faglærar. Pass på at du brukar avsnitt slik at der er meir lesbart. " +
                    "Originalt spørsmål: \"{{sporsmal}}\". " +
                    "Oppfølgingsspørsmål: \"{{oppfolgingssporsmal}}\". " +
                    "Brukarens svar: \"{{svar}}\". " +
                    "{{laeringsmaal}}" +
                    "Gi ei kort, konkret og konstruktiv vurdering. " +
                    "Sei kva som er bra og kva som kan forbedrast. " +
                    "Avslutt med: RATING: X (1-5), der 5 er svært bra.";

    private static final String FALLBACK_FASIT =
            "Du er ein faglærar. Pass på at du brukar avsnitt slik at der er meir lesbart. " +
                    "Brukaren stilte dette originale spørsmålet: \"{{sporsmal}}\". " +
                    "{{laeringsmaal}}" +
                    "Gi eit klart, korrekt og pedagogisk svar på dette spørsmålet.";

    private String hentPrompt(String nokkel, String fallback) {
        return adminPromptService.getAktivPrompt(nokkel)
                .map(AdminPrompt::getInnhald)
                .orElse(fallback);
    }

    private String lagLæringsmålTekst(String laeringsmaal) {
        if (laeringsmaal == null || laeringsmaal.isBlank()) return "";
        return "Relevante læringsmål for emnet: " + laeringsmaal + ". ";
    }

    public Map<String, Object> startSession(int brukarId, int temaId, String sporsmal) {
        Brukar brukar = brukarRepository.findById(brukarId).orElseThrow();
        Tema tema = temaRepository.findById(temaId).orElseThrow();

        SporsmalSession session = new SporsmalSession();
        session.setBrukar(brukar);
        session.setTema(tema);
        session.setStartSporsmal(sporsmal);
        session.setOpprettaTid(LocalDateTime.now());
        session.setState(SporsmalSession.SessionState.INITIAL);
        sporsmalRepository.save(session);

        Emne emne = tema.getEmne();
        String emneNamn = emne != null ? emne.getNamn() : "";
        String laeringsmaal = emne != null ? emne.getLaeringsmaal() : null;

        String promptMal = hentPrompt("oppfolgingssporsmal", FALLBACK_OPPFOLGINGSSPORSMAL);
        String promptTekst = promptMal
                .replace("{{sporsmal}}", sporsmal)
                .replace("{{emne}}", emneNamn)
                .replace("{{tema}}", tema.getNamn())
                .replace("{{laeringsmaal}}", lagLæringsmålTekst(laeringsmaal));

        AIRequest request = new AIRequest();
        request.setSubject(emneNamn);
        request.setTopic(tema.getNamn());
        request.setQuestion(promptTekst);

        AIResponse aiResponse = aiModelService.askAI(request);

        session.setOppfolgingsSporsmal(aiResponse.getFollow_up_question());
        session.setState(SporsmalSession.SessionState.FOLLOW_UP);
        sporsmalRepository.save(session);

        return Map.of(
                "sessionId", session.getSessionId(),
                "oppfolgingsSporsmal", aiResponse.getFollow_up_question() != null ? aiResponse.getFollow_up_question() : ""
        );
    }

    public Map<String, Object> handleRefleksjon(Long sessionId, String svar) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();

        Tema tema = session.getTema();
        Emne emne = tema.getEmne();
        String emneNamn = emne != null ? emne.getNamn() : "";
        String laeringsmaal = emne != null ? emne.getLaeringsmaal() : null;

        String vurderingMal = hentPrompt("vurdering", FALLBACK_VURDERING);
        String vurderingTekst = vurderingMal
                .replace("{{sporsmal}}", session.getStartSporsmal())
                .replace("{{oppfolgingssporsmal}}", session.getOppfolgingsSporsmal())
                .replace("{{svar}}", svar)
                .replace("{{emne}}", emneNamn)
                .replace("{{tema}}", tema.getNamn())
                .replace("{{laeringsmaal}}", lagLæringsmålTekst(laeringsmaal));

        AIRequest vurderingRequest = new AIRequest();
        vurderingRequest.setSubject(emneNamn);
        vurderingRequest.setTopic(tema.getNamn());
        vurderingRequest.setQuestion(vurderingTekst);

        AIResponse vurderingResponse = aiModelService.askAI(vurderingRequest);
        String vurdering = vurderingResponse.getExplanation();
        Integer rating = extractRating(vurdering);
        String cleanText = vurdering.replaceAll("RATING:\\s*\\d", "").trim();

        session.setBrukarRefleksjon(svar);
        session.setVurdering(cleanText);
        session.setRating(rating);

        // Rating < 4 → nytt oppfølgingsspørsmål
        if (rating != null && rating < 4) {
            String nyOppfolgingMal = hentPrompt("oppfolgingssporsmal", FALLBACK_OPPFOLGINGSSPORSMAL);
            String nyOppfolgingTekst = nyOppfolgingMal
                    .replace("{{sporsmal}}", session.getStartSporsmal())
                    .replace("{{emne}}", emneNamn)
                    .replace("{{tema}}", tema.getNamn())
                    .replace("{{laeringsmaal}}", lagLæringsmålTekst(laeringsmaal));

            AIRequest nyOppfolgingRequest = new AIRequest();
            nyOppfolgingRequest.setSubject(emneNamn);
            nyOppfolgingRequest.setTopic(tema.getNamn());
            nyOppfolgingRequest.setQuestion(nyOppfolgingTekst);

            AIResponse nyOppfolgingResponse = aiModelService.askAI(nyOppfolgingRequest);
            session.setOppfolgingsSporsmal(nyOppfolgingResponse.getFollow_up_question());
            session.setState(SporsmalSession.SessionState.NEEDS_FOLLOWUP);
            sporsmalRepository.save(session);

            return Map.of(
                    "vurdering", cleanText,
                    "rating", rating,
                    "needsFollowup", true,
                    "oppfolgingsSporsmal", nyOppfolgingResponse.getFollow_up_question() != null
                            ? nyOppfolgingResponse.getFollow_up_question() : ""
            );
        }

        // Rating >= 4 → fasit
        String fasitMal = hentPrompt("fasit", FALLBACK_FASIT);
        String fasitTekst = fasitMal
                .replace("{{sporsmal}}", session.getStartSporsmal())
                .replace("{{emne}}", emneNamn)
                .replace("{{tema}}", tema.getNamn())
                .replace("{{laeringsmaal}}", lagLæringsmålTekst(laeringsmaal));

        AIRequest fasitRequest = new AIRequest();
        fasitRequest.setSubject(emneNamn);
        fasitRequest.setTopic(tema.getNamn());
        fasitRequest.setQuestion(fasitTekst);

        AIResponse fasitResponse = aiModelService.askAI(fasitRequest);
        String fasit = fasitResponse.getExplanation();

        session.setFasitSvar(fasit);
        session.setState(SporsmalSession.SessionState.FINAL_ANSWER);
        sporsmalRepository.save(session);

        return Map.of(
                "vurdering", cleanText,
                "rating", rating != null ? rating : 0,
                "needsFollowup", false,
                "fasit", fasit != null ? fasit : ""
        );
    }

    public void handleTilbakemelding(Long sessionId, String tekst) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();
        session.setState(SporsmalSession.SessionState.COMPLETED);
        sporsmalRepository.save(session);
    }

    public Map<String, Object> startNyRunde(int brukarId, Long gammalSessionId, String nyttSporsmal) {
        SporsmalSession gammal = sporsmalRepository.findById(gammalSessionId).orElseThrow();
        return startSession(brukarId, gammal.getTema().getTemaId(), nyttSporsmal);
    }

    private Integer extractRating(String text) {
        if (text == null) return null;
        Matcher match = Pattern.compile("RATING:\\s*(\\d)").matcher(text);
        if (match.find()) {
            int rating = Integer.parseInt(match.group(1));
            if (rating >= 1 && rating <= 5) return rating;
        }
        return null;
    }

    public Map<String, Object> hentFasitDirekte(Long sessionId) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();

        Tema tema = session.getTema();
        Emne emne = tema.getEmne();
        String emneNamn = emne != null ? emne.getNamn() : "";
        String laeringsmaal = emne != null ? emne.getLaeringsmaal() : null;

        String fasitMal = hentPrompt("fasit", FALLBACK_FASIT);
        String fasitTekst = fasitMal
                .replace("{{sporsmal}}", session.getStartSporsmal())
                .replace("{{emne}}", emneNamn)
                .replace("{{tema}}", tema.getNamn())
                .replace("{{laeringsmaal}}", lagLæringsmålTekst(laeringsmaal));

        AIRequest fasitRequest = new AIRequest();
        fasitRequest.setSubject(emneNamn);
        fasitRequest.setTopic(tema.getNamn());
        fasitRequest.setQuestion(fasitTekst);

        AIResponse fasitResponse = aiModelService.askAI(fasitRequest);
        String fasit = fasitResponse.getExplanation();

        session.setFasitSvar(fasit);
        session.setState(SporsmalSession.SessionState.FINAL_ANSWER);
        sporsmalRepository.save(session);

        return Map.of("fasit", fasit != null ? fasit : "");
    }

    public long getSessionCount(int brukarId) {
        return sporsmalRepository.countByBrukarId(brukarId);
    }
}