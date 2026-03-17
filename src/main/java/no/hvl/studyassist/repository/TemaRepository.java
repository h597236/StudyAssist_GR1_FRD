package no.hvl.studyassist.repository;


import no.hvl.studyassist.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {
    List<Tema> findByEmneEmneId(Integer emneId);
}