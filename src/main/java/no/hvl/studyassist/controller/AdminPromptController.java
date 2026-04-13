package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.AdminPrompt;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.AdminPromptService;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/prompt")
@CrossOrigin
public class AdminPromptController {

    private final AdminPromptService adminPromptService;
    private final BrukarService brukarService;

    public AdminPromptController(AdminPromptService adminPromptService, BrukarService brukarService) {
        this.adminPromptService = adminPromptService;
        this.brukarService = brukarService;
    }

    @GetMapping("/{nokkel}")
    public ResponseEntity<?> getPrompt(@PathVariable String nokkel, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Ikkje tilgang.");

        List<AdminPrompt> versjonar = adminPromptService.getVersjonar(nokkel);
        AdminPrompt aktiv = adminPromptService.getAktivPrompt(nokkel).orElse(null);

        return ResponseEntity.ok(Map.of(
                "aktiv", aktiv != null ? aktiv : "",
                "versjonar", versjonar
        ));
    }

    @PostMapping("/{nokkel}")
    public ResponseEntity<?> lagrePrompt(
            @PathVariable String nokkel,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        if (!isAdmin(session)) return ResponseEntity.status(403).body("Ikkje tilgang.");

        String innhald = body.get("innhald");
        if (innhald == null || innhald.isBlank()) {
            return ResponseEntity.badRequest().body("Innhald kan ikkje vere tomt.");
        }

        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        AdminPrompt lagra = adminPromptService.lagreNyVersjon(nokkel, innhald, brukar);
        return ResponseEntity.ok(lagra);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getVersjon(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Ikkje tilgang.");

        return adminPromptService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/settaktiv")
    public ResponseEntity<?> settAktiv(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body("Ikkje tilgang.");

        AdminPrompt lagra = adminPromptService.settAktiv(id);
        return ResponseEntity.ok(lagra);
    }

    private boolean isAdmin(HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        return brukar != null && "ADMIN".equalsIgnoreCase(brukar.getRolle());
    }
}