package no.hvl.studyassist.controller;

import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.service.OpenAIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final OpenAIService openAIService;

    public AIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/ask")
    public AIResponse askQuestion(@RequestBody AIRequest request) {
        return openAIService.askAI(request);
    }
}