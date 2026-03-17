package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemaRepository extends JpaRepository<Tema, Integer> {

    List<Tema> findByEmneEmneId(int emneId);
}