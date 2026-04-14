package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.ai.AiModelService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AiModelService aiModelService;
    private final BrukarService brukarService;

    public AIController(AiModelService aiModelService, BrukarService brukarService) {
        this.aiModelService = aiModelService;
        this.brukarService = brukarService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody AIRequest request, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);

        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        AIResponse response = aiModelService.askAI(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models")
    public ResponseEntity<?> getModels() {
        return ResponseEntity.ok(aiModelService.getAvailableModels());
    }
}
