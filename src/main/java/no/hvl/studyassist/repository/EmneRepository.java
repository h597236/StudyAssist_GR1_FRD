package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.Emne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmneRepository extends JpaRepository<Emne, Integer> {

    List<Emne> findByBrukar_Id(int brukarId);
}