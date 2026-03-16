package no.hvl.studyassist.controller;

import no.hvl.studyassist.service.BrukarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/brukar")
public class BrukarController {

    @Autowired
    private BrukarService brukarService;

    @PostMapping("/registrer")
    public ResponseEntity<String> registrer(@RequestBody Map<String, String> body) {
        String brukarnavn = body.get("brukarnavn");
        String passord = body.get("passord");

        if (brukarService.finnes(brukarnavn)) {
            return ResponseEntity.badRequest().body("Brukarnamn er allereie tatt.");
        }

        brukarService.registrer(brukarnavn, passord);
        return ResponseEntity.ok("Brukar oppretta!");
    }

    @PostMapping("/logginn")
    public ResponseEntity<String> loggInn(@RequestBody Map<String, String> body) {
        String brukarnavn = body.get("brukarnavn");
        String passord = body.get("passord");

        if (brukarService.loggInn(brukarnavn, passord)) {
            return ResponseEntity.ok(brukarnavn);
        } else {
            return ResponseEntity.status(401).body("Feil brukarnamn eller passord.");
        }
    }
}