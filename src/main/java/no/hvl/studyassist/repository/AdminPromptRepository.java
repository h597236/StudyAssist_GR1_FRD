package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.AdminPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminPromptRepository extends JpaRepository<AdminPrompt, Long> {

    List<AdminPrompt> findByNokkelOrderByVersjonAsc(String nokkel);

    Optional<AdminPrompt> findByNokkelAndErAktivTrue(String nokkel);
}