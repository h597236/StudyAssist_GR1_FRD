package no.hvl.studyassist.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AIResponseTest {

    @Test
    void shouldStoreExplanationAndFollowUpQuestion() {

        AIResponse response = new AIResponse();

        response.setExplanation("Dette er en forklaring");
        response.setFollow_up_question("Kan du forklare mer?");

        assertEquals("Dette er en forklaring", response.getExplanation());
        assertEquals("Kan du forklare mer?", response.getFollow_up_question());
    }
}