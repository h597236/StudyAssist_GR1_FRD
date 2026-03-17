package no.hvl.studyassist.repository;

import no.hvl.studyassist.model.Brukar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrukarRepository extends JpaRepository<Brukar, Integer> {

    Brukar findByEmail(String email);
}