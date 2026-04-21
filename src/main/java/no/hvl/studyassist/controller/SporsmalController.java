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

    @PostMapping("/start")
    public ResponseEntity<?> startSporsmal(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        int temaId = (int) body.get("temaId");
        String sporsmal = (String) body.get("sporsmal");

        return ResponseEntity.ok(sporsmalService.startSession(brukar.getId(), temaId, sporsmal));
    }

    @PostMapping("/refleksjon")
    public ResponseEntity<?> sendRefleksjon(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String svar = (String) body.get("svar");

        return ResponseEntity.ok(sporsmalService.handleRefleksjon(sessionId, svar));
    }

    @PostMapping("/tilbakemelding")
    public ResponseEntity<?> sendTilbakemelding(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String tekst = (String) body.get("tekst");

        sporsmalService.handleTilbakemelding(sessionId, tekst);
        return ResponseEntity.ok("Tilbakemelding mottatt.");
    }

    @GetMapping("/count")
    public ResponseEntity<?> getSessionCount(HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        long count = sporsmalService.getSessionCount(brukar.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/ny-runde")
    public ResponseEntity<?> nyRunde(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Long gammalSessionId = Long.valueOf(body.get("sessionId").toString());
        String nyttSporsmal = (String) body.get("sporsmal");

        return ResponseEntity.ok(sporsmalService.startNyRunde(brukar.getId(), gammalSessionId, nyttSporsmal));
    }

    @PostMapping("/fasit-direkte")
    public ResponseEntity<?> fasitDirekte(@RequestBody Map<String, Object> body, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        return ResponseEntity.ok(sporsmalService.hentFasitDirekte(sessionId));
    }
}