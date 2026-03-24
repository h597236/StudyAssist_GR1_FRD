package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/brukar")
@CrossOrigin
public class BrukarController {

    @Autowired
    private BrukarService brukarService;

    @PostMapping("/registrer")
    public ResponseEntity<?> registrer(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String passord = body.get("passord");

        if (email == null || email.isBlank() ||
                passord == null || passord.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email og passord må fyllast ut.");
        }

        if (brukarService.finnes(email)) {
            return ResponseEntity.badRequest().body("Email er allereie brukt.");
        }

        Brukar brukar = brukarService.registrer(email, passord);
        return ResponseEntity.ok(brukar);
    }

    @PostMapping("/logginn")
    public ResponseEntity<?> loggInn(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String passord = body.get("passord");

        if (email == null || email.isBlank() ||
                passord == null || passord.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email og passord må fyllast ut.");
        }

        Brukar brukar = brukarService.loggInn(email, passord);

        if (brukar != null) {
            return ResponseEntity.ok(brukar);
        } else {
            return ResponseEntity.status(401).body("Feil email eller passord.");
        }
    }
}