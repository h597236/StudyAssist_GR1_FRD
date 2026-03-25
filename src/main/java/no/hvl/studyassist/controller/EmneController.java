package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.service.EmneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emne")
@CrossOrigin
public class EmneController {

    private final EmneService emneService;

    public EmneController(EmneService emneService) {
        this.emneService = emneService;
    }

    @PostMapping
    public Emne create(@RequestBody Emne emne) {
        return emneService.save(emne);
    }

    @GetMapping("/brukar/{brukarId}")
    public List<Emne> getByBrukar(@PathVariable int brukarId) {
        return emneService.findByBrukarId(brukarId);
    }

    @DeleteMapping("/{emneId}")
    public void delete(@PathVariable int emneId) {
        emneService.deleteById(emneId);
    }
}