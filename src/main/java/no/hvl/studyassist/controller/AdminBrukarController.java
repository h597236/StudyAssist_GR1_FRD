package no.hvl.studyassist.controller;

import jakarta.servlet.http.HttpSession;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.AdminBrukarService;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/brukarar")
@CrossOrigin
public class AdminBrukarController {

    @Autowired
    private AdminBrukarService adminBrukarService;

    @Autowired
    private BrukarService brukarService;

    // GET /api/admin/brukarar?email=...&rolle=...
    @GetMapping
    public ResponseEntity<?> getAllBrukarar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String rolle,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body("Ikkje tilgang");
        }

        Brukar innlogga = SessionUtil.getLoggedInBrukar(session, brukarService);
        int innloggaId = innlogga != null ? innlogga.getId() : -1;

        List<Map<String, Object>> brukarar = adminBrukarService.getAllBrukarar(email, rolle)
                .stream()
                .filter(b -> !b.get("id").equals(innloggaId))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(brukarar);
    }

    // PUT /api/admin/brukarar/{id}/rolle
    @PutMapping("/{id}/rolle")
    public ResponseEntity<?> endreRolle(
            @PathVariable int id,
            @RequestBody Map<String, String> body,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body("Ikkje tilgang");
        }

        String nyRolle = body.get("rolle");
        if (nyRolle == null || nyRolle.isBlank()) {
            return ResponseEntity.badRequest().body("Rolle manglar");
        }

        adminBrukarService.endreRolle(id, nyRolle);
        return ResponseEntity.ok(Map.of("melding", "Rolle oppdatert"));
    }

    // DELETE /api/admin/brukarar/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> slettBrukar(
            @PathVariable int id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body("Ikkje tilgang");
        }

        Brukar innlogga = SessionUtil.getLoggedInBrukar(session, brukarService);
        if (innlogga != null && innlogga.getId() == id) {
            return ResponseEntity.badRequest().body("Du kan ikkje slette deg sjølv.");
        }

        adminBrukarService.slettBrukar(id);
        return ResponseEntity.ok(Map.of("melding", "Brukar sletta"));
    }

    private boolean isAdmin(HttpSession session) {
        Brukar brukar = SessionUtil.getLoggedInBrukar(session, brukarService);
        return brukar != null && "ADMIN".equalsIgnoreCase(brukar.getRolle());
    }
}