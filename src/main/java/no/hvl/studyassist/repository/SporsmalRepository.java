package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.SporsmalSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SporsmalRepository extends JpaRepository<SporsmalSession, Long> {
    List<SporsmalSession> findByBrukarId(int brukarId);
    long countByBrukarId(int brukarId);
}