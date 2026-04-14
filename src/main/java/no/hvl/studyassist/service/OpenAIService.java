package no.hvl.studyassist.service;

import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.service.ai.AiModelClient;
import no.hvl.studyassist.service.ai.AiModelResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIService {

    private final AiModelResolver modelResolver;

    public OpenAIService(AiModelResolver modelResolver) {
        this.modelResolver = modelResolver;
    }

    public AIResponse askAI(AIRequest request) {
        return askAI(request, null);
    }

    public AIResponse askAI(AIRequest request, String modelName) {
        AiModelClient client = modelResolver.resolveOrDefault(modelName);
        return client.askAI(request);
    }

    public String askRaw(String instructions, String input) {
        return askRaw(instructions, input, null);
    }

    public String askRaw(String instructions, String input, String modelName) {
        AiModelClient client = modelResolver.resolveOrDefault(modelName);
        return client.askRaw(instructions, input);
    }

    public List<String> getAvailableModels() {
        return modelResolver.getAvailableModels();
    }
}