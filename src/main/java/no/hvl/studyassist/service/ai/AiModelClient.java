package no.hvl.studyassist.service.ai;

import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;

public interface AiModelClient {

    String getModelName();

    AIResponse askAI(AIRequest request);

    String askRaw(String instructions, String input);
}
