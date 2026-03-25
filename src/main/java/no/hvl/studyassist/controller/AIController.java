package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.OpenAIService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final OpenAIService openAIService;
    private final BrukarService brukarService;

    public AIController(OpenAIService openAIService, BrukarService brukarService) {
        this.openAIService = openAIService;
        this.brukarService = brukarService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody AIRequest request, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);

        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        AIResponse response = openAIService.askAI(request);
        return ResponseEntity.ok(response);
    }
}
