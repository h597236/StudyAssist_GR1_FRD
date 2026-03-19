package no.hvl.studyassist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import no.hvl.studyassist.model.AIRequest;
import no.hvl.studyassist.model.AIResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    public AIResponse askAI(AIRequest request) {
        try {

            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();

            String instructions = """
                    Du er en studieassistent for IT-studenter.

                    Svar pedagogisk og kortfatta.

                    Returner alltid kun JSON i dette formatet:

                    {
                      "explanation": "...",
                      "follow_up_question": "..."
                    }
                    """;

            String input;

            if (request.getSubject() != null && request.getTopic() != null) {
                input = """
            Emne: %s
            Tema: %s
            Spørsmål: %s
            """.formatted(
                        request.getSubject(),
                        request.getTopic(),
                        request.getQuestion()
                );
            } else {
                input = """
            Spørsmål: %s
            """.formatted(
                        request.getQuestion()
                );
            }

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model("gpt-5.2")
                    .instructions(instructions)
                    .input(input)
                    .build();

            Response response = client.responses().create(params);

            String text = response.output().stream()
                    .filter(ResponseOutputItem::isMessage)
                    .flatMap(item -> item.asMessage().content().stream())
                    .filter(ResponseOutputMessage.Content::isOutputText)
                    .map(content -> content.asOutputText().text())
                    .collect(java.util.stream.Collectors.joining());

            if (text.isBlank()) {
                throw new RuntimeException("Fant ikkje tekst i OpenAI-responsen");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(text, AIResponse.class);

        } catch (Exception e) {
            e.printStackTrace();

            AIResponse error = new AIResponse();
            error.setExplanation("Feil ved AI-kall");
            error.setFollow_up_question("Prøv igjen seinare");
            return error;
        }
    }
}