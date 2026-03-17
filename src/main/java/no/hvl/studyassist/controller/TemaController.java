package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.service.TemaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tema")
@CrossOrigin
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @PostMapping
    public Tema create(@RequestBody Tema tema) {
        return temaService.save(tema);
    }

    @GetMapping("/emne/{emneId}")
    public List<Tema> getByEmne(@PathVariable int emneId) {
        return temaService.findByEmneId(emneId);
    }
}