package no.hvl.studyassist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }

    @GetMapping("/home")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/sporsmal")
    public String sporsmal() {
        return "forward:/sporsmal.html";
    }

    @GetMapping("/historikk")
    public String historikk() {
        return "forward:/historikk.html";
    }
}