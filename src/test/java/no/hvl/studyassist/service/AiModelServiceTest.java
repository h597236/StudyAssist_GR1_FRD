package no.hvl.studyassist.service;

import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.service.ai.AiModelClient;
import no.hvl.studyassist.service.ai.AiModelResolver;
import no.hvl.studyassist.service.ai.AiModelService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiModelServiceTest {

    @Test
    void shouldReturnErrorResponseIfSomethingFails() {

        AiModelClient mockClient = mock(AiModelClient.class);
        AiModelResolver mockResolver = mock(AiModelResolver.class);

        AIResponse fakeResponse = new AIResponse();
        fakeResponse.setExplanation("Test svar");
        fakeResponse.setFollow_up_question("Test spørsmål");

        when(mockResolver.resolveOrDefault(null)).thenReturn(mockClient);
        when(mockClient.askAI(any())).thenReturn(fakeResponse);

        AiModelService service = new AiModelService(mockResolver);

        AIRequest request = new AIRequest();
        request.setSubject("Test");
        request.setTopic("Test");
        request.setQuestion("Test");

        AIResponse response = service.askAI(request);

        assertNotNull(response);
        assertEquals("Test svar", response.getExplanation());
    }
}