package no.hvl.studyassist.repository;


import no.hvl.studyassist.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Integer> {}