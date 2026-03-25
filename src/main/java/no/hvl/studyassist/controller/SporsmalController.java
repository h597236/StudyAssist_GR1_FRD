package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.SporsmalService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sporsmal")
@CrossOrigin
public class SporsmalController {

    private final SporsmalService sporsmalService;
    private final BrukarService brukarService;

    public SporsmalController(SporsmalService sporsmalService, BrukarService brukarService) {
        this.sporsmalService = sporsmalService;
        this.brukarService = brukarService;
    }

    // Diagram 1: Start session
    @PostMapping("/start")
    public ResponseEntity<?> startSporsmal(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        int temaId = (int) body.get("temaId");
        String sporsmal = (String) body.get("sporsmal");

        return ResponseEntity.ok(sporsmalService.startSession(brukar.getId(), temaId, sporsmal));
    }

    // Diagram 2: Send reflection answer
    @PostMapping("/refleksjon")
    public ResponseEntity<?> sendRefleksjon(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String svar = (String) body.get("svar");

        return ResponseEntity.ok(sporsmalService.handleRefleksjon(sessionId, svar));
    }

    // Diagram 3: Send tilbakemelding
    @PostMapping("/tilbakemelding")
    public ResponseEntity<?> sendTilbakemelding(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String tekst = (String) body.get("tekst");

        sporsmalService.handleTilbakemelding(sessionId, tekst);
        return ResponseEntity.ok("Tilbakemelding mottatt.");
    }
}