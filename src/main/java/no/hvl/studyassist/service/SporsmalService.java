package no.hvl.studyassist.service;

import no.hvl.studyassist.model.*;
import no.hvl.studyassist.repository.BrukarRepository;
import no.hvl.studyassist.repository.SporsmalRepository;
import no.hvl.studyassist.repository.TemaRepository;
import org.springframework.stereotype.Service;

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
        session.setOriginalSporsmal(sporsmal);
        session.setState(SporsmalSession.SessionState.FOLLOW_UP);
        sporsmalRepository.save(session);

        // Ask AI for follow-up question
        AIRequest request = new AIRequest();
        request.setSubject(tema.getEmne() != null ? tema.getEmne().getNamn() : "");
        request.setTopic(tema.getNamn());
        request.setQuestion("Brukaren stilte dette spørsmålet: \"" + sporsmal +
                "\". Gi ei kort forklaring og still eit oppfølgingsspørsmål.");

        AIResponse aiResponse = openAIService.askAI(request);

        session.setFollowUpSporsmal(aiResponse.getFollow_up_question());
        sporsmalRepository.save(session);

        return Map.of(
                "sessionId", session.getId(),
                "explanation", aiResponse.getExplanation() != null ? aiResponse.getExplanation() : "",
                "followUpSporsmal", aiResponse.getFollow_up_question() != null ? aiResponse.getFollow_up_question() : ""
        );
    }

    // Diagram 2: Handle reflection → get vurdering + fasit
    public Map<String, Object> handleRefleksjon(Long sessionId, String svar) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();

        session.setSvar(svar);
        session.setState(SporsmalSession.SessionState.ANSWERED);
        sporsmalRepository.save(session);

        Tema tema = session.getTema();
        String emneNamn = tema.getEmne() != null ? tema.getEmne().getNamn() : "";

        // Get vurdering
        AIRequest vurderingRequest = new AIRequest();
        vurderingRequest.setSubject(emneNamn);
        vurderingRequest.setTopic(tema.getNamn());
        vurderingRequest.setQuestion("Oppfølgingsspørsmål: \"" + session.getFollowUpSporsmal() +
                "\". Brukarens svar: \"" + svar + "\". Vurder svaret kort og konstruktivt.");
        AIResponse vurderingResponse = openAIService.askAI(vurderingRequest);
        String vurdering = vurderingResponse.getExplanation();

        // Get fasit
        AIRequest fasitRequest = new AIRequest();
        fasitRequest.setSubject(emneNamn);
        fasitRequest.setTopic(tema.getNamn());
        fasitRequest.setQuestion("Gi eit godt fasitsvar på dette spørsmålet: \"" + session.getFollowUpSporsmal() + "\"");
        AIResponse fasitResponse = openAIService.askAI(fasitRequest);
        String fasit = fasitResponse.getExplanation();

        session.setVurdering(vurdering);
        session.setFasit(fasit);
        session.setState(SporsmalSession.SessionState.FINAL_ANSWER);
        sporsmalRepository.save(session);

        return Map.of(
                "vurdering", vurdering != null ? vurdering : "",
                "fasit", fasit != null ? fasit : ""
        );
    }

    // Diagram 3: Handle tilbakemelding → mark session COMPLETED
    public void handleTilbakemelding(Long sessionId, String tekst) {
        SporsmalSession session = sporsmalRepository.findById(sessionId).orElseThrow();
        session.setState(SporsmalSession.SessionState.COMPLETED);
        sporsmalRepository.save(session);
    }
}