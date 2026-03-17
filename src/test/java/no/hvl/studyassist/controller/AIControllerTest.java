package no.hvl.studyassist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.service.OpenAIService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AIController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAIService openAIService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAIResponse() throws Exception {

        AIResponse mockResponse = new AIResponse();
        mockResponse.setExplanation("Forklaring");
        mockResponse.setFollow_up_question("Oppfølgingsspørsmål");

        Mockito.when(openAIService.askAI(Mockito.any()))
                .thenReturn(mockResponse);

        AIRequest request = new AIRequest();
        request.setSubject("Programmering");
        request.setTopic("Løkker");
        request.setQuestion("Hva er en for-løkke?");

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explanation").value("Forklaring"))
                .andExpect(jsonPath("$.follow_up_question").value("Oppfølgingsspørsmål"));
    }
}