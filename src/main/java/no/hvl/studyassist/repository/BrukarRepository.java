package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.Brukar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrukarRepository extends JpaRepository<Brukar, String> {}
