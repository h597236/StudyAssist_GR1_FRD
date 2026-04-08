package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Historikk;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.HistorikkService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historikk")
@CrossOrigin
public class HistorikkController {

    private final HistorikkService historikkService;
    private final BrukarService brukarService;

    public HistorikkController(HistorikkService historikkService, BrukarService brukarService) {
        this.historikkService = historikkService;
        this.brukarService = brukarService;
    }

    @GetMapping
    public ResponseEntity<?> getHistorikk(HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        List<Historikk> historikk = historikkService.getHistorikk(brukar.getId());
        return ResponseEntity.ok(historikk);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getById(@PathVariable Long sessionId, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        Historikk historikk = historikkService.getById(sessionId);
        if (historikk == null) return ResponseEntity.status(404).body("Ikkje funne.");
        return ResponseEntity.ok(historikk);
    }

    @GetMapping("/sok")
    public ResponseEntity<?> sokHistorikk(@RequestParam String q, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) return ResponseEntity.status(401).body("Ikkje logga inn.");

        List<Historikk> results = historikkService.sokHistorikk(brukar.getId(), q);
        return ResponseEntity.ok(results);
    }
}