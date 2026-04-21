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

import java.util.List;
import java.util.Map;

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
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        AIResponse response = aiModelService.askAI(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models")
    public ResponseEntity<?> getModels() {
        return ResponseEntity.ok(aiModelService.getAvailableModels());
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        String melding = (String) body.get("melding");
        List<Map<String, String>> historikk = (List<Map<String, String>>) body.get("historikk");

        StringBuilder logg = new StringBuilder();
        if (historikk != null) {
            for (Map<String, String> entry : historikk) {
                logg.append(entry.get("rolle")).append(": ").append(entry.get("tekst")).append("\n");
            }
        }

        String input = logg.length() > 0
                ? "Tidlegare samtale:\n" + logg + "\nBrukar: " + melding
                : melding;

        String instructions =
                "Du er ein hjelpsam studieassistent. " +
                        "Svar naturleg og direkte på det brukaren spør om. " +
                        "Husk konteksten frå tidlegare i samtalen.";

        String svar = aiModelService.askRaw(instructions, input);

        return ResponseEntity.ok(Map.of(
                "svar", svar != null ? svar : "Feil ved AI-kall"
        ));
    }
}