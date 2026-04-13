package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.util.SessionUtil;
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
            return ResponseEntity.badRequest().body("Email og passord må fyllast ut.");
        }

        if (brukarService.finnes(email)) {
            return ResponseEntity.badRequest().body("Email er allereie brukt.");
        }

        Brukar brukar = brukarService.registrer(email, passord);
        return ResponseEntity.ok(Map.of(
                "id", brukar.getId(),
                "email", brukar.getEmail()
        ));
    }

    @PostMapping("/logginn")
    public ResponseEntity<?> loggInn(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email");
        String passord = body.get("passord");

        if (email == null || email.isBlank() ||
                passord == null || passord.isBlank()) {
            return ResponseEntity.badRequest().body("Email og passord må fyllast ut.");
        }

        Brukar brukar = brukarService.loggInn(email, passord);

        if (brukar != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("brukarId", brukar.getId());

            return ResponseEntity.ok(Map.of(
                    "id", brukar.getId(),
                    "email", brukar.getEmail(),
                    "rolle", brukar.getRolle() != null ? brukar.getRolle() : "STUDENT"
            ));
        } else {
            return ResponseEntity.status(401).body("Feil email eller passord.");
        }
    }

    // Now also returns rolle — needed for admin check in frontend
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);

        if (brukar == null) {
            return ResponseEntity.status(401).body("Ikkje logga inn.");
        }

        return ResponseEntity.ok(Map.of(
                "id", brukar.getId(),
                "email", brukar.getEmail(),
                "rolle", brukar.getRolle() != null ? brukar.getRolle() : "STUDENT"
        ));
    }

    @PostMapping("/loggut")
    public ResponseEntity<?> loggUt(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }
}