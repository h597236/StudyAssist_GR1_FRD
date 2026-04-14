package no.hvl.studyassist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import no.hvl.studyassist.service.ai.AiModelService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AIController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiModelService aiModelService;

    @MockBean
    private BrukarService brukarService;

    @Test
    void ask_ai_ok() throws Exception {

        // Mock brukar (session)
        Brukar brukar = new Brukar();
        brukar.setId(1);

        when(brukarService.findById(1)).thenReturn(brukar);

        // Mock AI response (Riktig felt!)
        AIResponse response = new AIResponse();
        response.setExplanation("Test forklaring");
        response.setFollow_up_question("Test spørsmål");

        when(aiModelService.askAI(any()))
                .thenReturn(response);

        // Request (Riktig felt!)
        AIRequest request = new AIRequest();
        request.setQuestion("Test spørsmål");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("brukarId", 1);

        mockMvc.perform(post("/api/ai/ask")
                        .session(session)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void ask_ai_not_logged_in() throws Exception {

        AIRequest request = new AIRequest();
        request.setQuestion("Test");

        mockMvc.perform(post("/api/ai/ask")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}