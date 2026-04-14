package no.hvl.studyassist.service.ai;

import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiModelService {

    private final AiModelResolver modelResolver;

    public AiModelService(AiModelResolver modelResolver) {
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