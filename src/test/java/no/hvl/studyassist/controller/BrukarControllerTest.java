package no.hvl.studyassist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrukarController.class)
@AutoConfigureMockMvc(addFilters = false)
class BrukarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrukarService brukarService;

    @Autowired
    private ObjectMapper objectMapper;

    private Brukar mockBrukar() {
        Brukar b = new Brukar();
        b.setId(1);
        b.setEmail("test@test.no");
        return b;
    }

    @Test
    void login_endpoint_exists() throws Exception {

        Brukar brukar = mockBrukar();

        when(brukarService.loggInn(anyString(), anyString()))
                .thenReturn(brukar);

        mockMvc.perform(post("/api/brukar/logginn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@test.no",
                                "passord", "1234"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void register_endpoint_exists() throws Exception {

        Brukar brukar = mockBrukar();

        when(brukarService.finnes(anyString()))
                .thenReturn(false);

        when(brukarService.registrer(anyString(), anyString()))
                .thenReturn(brukar);

        mockMvc.perform(post("/api/brukar/registrer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "test@test.no",
                                "passord", "1234"
                        ))))
                .andExpect(status().isOk());
    }
}