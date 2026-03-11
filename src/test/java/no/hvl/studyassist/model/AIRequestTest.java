package no.hvl.studyassist.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AIRequestTest {

    @Test
    void shouldStoreValuesCorrectly() {

        AIRequest request = new AIRequest();

        request.setSubject("Programmering");
        request.setTopic("Streams");
        request.setQuestion("Hva er en stream?");

        assertEquals("Programmering", request.getSubject());
        assertEquals("Streams", request.getTopic());
        assertEquals("Hva er en stream?", request.getQuestion());
    }
}