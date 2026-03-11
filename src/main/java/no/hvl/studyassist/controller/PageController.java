package no.hvl.studyassist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/sporsmal")
    public String sporsmal() {
        return "redirect:/sporsmal.html";
    }
}