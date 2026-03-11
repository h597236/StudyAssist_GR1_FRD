package no.hvl.studyassist.service;

import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIServiceTest {

    @Test
    void shouldReturnErrorResponseIfSomethingFails() {

        OpenAIService service = new OpenAIService();

        AIRequest request = new AIRequest();
        request.setSubject("Test");
        request.setTopic("Test");
        request.setQuestion("Test");

        AIResponse response = service.askAI(request);

        assertNotNull(response);
    }
}