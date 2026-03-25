package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.EmneService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emne")
@CrossOrigin
public class EmneController {

    private final EmneService emneService;
    private final BrukarService brukarService;

    public EmneController(EmneService emneService, BrukarService brukarService) {
        this.emneService = emneService;
        this.brukarService = brukarService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Emne emne, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        emne.setBrukar(brukar);
        return ResponseEntity.ok(emneService.save(emne));
    }

    @GetMapping("/brukar/{brukarId}")
    public ResponseEntity<?> getByBrukar(@PathVariable int brukarId, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        return ResponseEntity.ok(emneService.findByBrukarId(brukar.getId()));
    }

    @DeleteMapping("/{emneId}")
    public ResponseEntity<?> delete(@PathVariable int emneId, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        if (!emneService.eigesAvBrukar(emneId, brukar.getId())) {
            return ResponseEntity.status(403).body("Du har ikkje tilgang til dette emnet.");
        }

        emneService.deleteById(emneId);
        return ResponseEntity.noContent().build();
    }
}
