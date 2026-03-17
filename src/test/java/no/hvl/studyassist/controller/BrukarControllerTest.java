package no.hvl.studyassist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.service.BrukarService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrukarController.class)
@AutoConfigureMockMvc(addFilters = false)
class BrukarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrukarService brukarService;

    @Test
    void login_endpoint_exists() throws Exception {

        when(brukarService.loggInn(any(), any()))
                .thenReturn(new Brukar());

        mockMvc.perform(post("/api/brukar/logginn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "test@test.com",
                  "passord": "1234"
                }
            """))
                .andExpect(status().isOk());
    }

    @Test
    void register_endpoint_exists() throws Exception {

        when(brukarService.registrer(any(), any()))
                .thenReturn(new Brukar());

        mockMvc.perform(post("/api/brukar/registrer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "new@test.com",
                  "passord": "1234"
                }
            """))
                .andExpect(status().isOk());
    }
}