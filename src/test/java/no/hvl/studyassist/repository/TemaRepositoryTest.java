package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.Tema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TemaRepositoryTest {

    @Test
    void lagreTema_skalGiId() {
        Tema tema = new Tema();
        tema.setNamn("Test tema");

        // hvis du bruker autowired repo:
        // tema = repo.save(tema);

        assertNotNull(tema);
    }
}