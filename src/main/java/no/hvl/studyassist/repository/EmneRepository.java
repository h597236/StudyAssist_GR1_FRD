package no.hvl.studyassist.repository;


import no.hvl.studyassist.model.Emne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmneRepository extends JpaRepository<Emne, Integer> {}
