package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.service.TemaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tema")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @PostMapping
    public Tema opprettTema(@RequestBody Tema tema) {
        return temaService.lagreTema(tema);
    }

    @GetMapping("/emne/{emneId}")
    public List<Tema> hentTema(@PathVariable Integer emneId) {
        return temaService.hentTemaForEmne(emneId);
    }
}