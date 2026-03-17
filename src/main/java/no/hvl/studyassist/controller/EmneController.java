package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.service.EmneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emne")
public class EmneController {

    private final EmneService emneService;

    public EmneController(EmneService emneService) {
        this.emneService = emneService;
    }

    @PostMapping
    public Emne opprettEmne(@RequestBody Emne emne) {
        return emneService.lagreEmne(emne);
    }

    @GetMapping
    public List<Emne> hentAlle() {
        return emneService.hentAlleEmner();
    }
}