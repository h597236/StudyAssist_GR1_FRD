package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.TemaService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tema")
@CrossOrigin
public class TemaController {

    private final TemaService temaService;
    private final BrukarService brukarService;

    public TemaController(TemaService temaService, BrukarService brukarService) {
        this.temaService = temaService;
        this.brukarService = brukarService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Tema tema, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        if (tema.getEmne() == null || tema.getEmne().getEmneId() == 0) {
            return ResponseEntity.badRequest().body("Tema må ha eit gyldig emne.");
        }

        int emneId = tema.getEmne().getEmneId();
        if (!temaService.eigesAvBrukar(emneId, brukar.getId())) {
            return ResponseEntity.status(403).body("Du har ikkje tilgang til dette emnet.");
        }

        Emne emne = temaService.findEmneById(emneId);
        if (emne == null) {
            return ResponseEntity.badRequest().body("Emne finst ikkje.");
        }

        tema.setEmne(emne);
        return ResponseEntity.ok(temaService.save(tema));
    }

    @GetMapping("/emne/{emneId}")
    public ResponseEntity<?> getByEmne(@PathVariable int emneId, HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        if (!temaService.eigesAvBrukar(emneId, brukar.getId())) {
            return ResponseEntity.status(403).body("Du har ikkje tilgang til dette emnet.");
        }

        return ResponseEntity.ok(temaService.findByEmneId(emneId));
    }

    @DeleteMapping("/{temaId}")
        public ResponseEntity<?> delete(@PathVariable int temaId, HttpSession session) {
            Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
            if (brukar == null) {
                return ResponseEntity.status(401).body("Ikkje logga inn.");
            }

            Tema tema = temaService.findById(temaId);
            if (tema == null) {
                return ResponseEntity.notFound().build();
            }

            if (!temaService.eigesAvBrukar(tema.getEmne().getEmneId(), brukar.getId())) {
                return ResponseEntity.status(403).body("Du har ikkje tilgang til dette temaet.");
            }

            temaService.deleteById(temaId);
            return ResponseEntity.noContent().build();
        }
}
