package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.repository.BrukarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BrukarService {

    @Autowired
    private BrukarRepository brukarRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public void registrer(String brukarnavn, String passord) {
        Brukar brukar = new Brukar();
        brukar.setBrukarnavn(brukarnavn);
        brukar.setPassord(passwordEncoder.encode(passord));
        brukarRepository.save(brukar);
    }

    public boolean loggInn(String brukarnavn, String passord) {
        Brukar brukar = brukarRepository.findById(brukarnavn).orElse(null);
        if (brukar == null) return false;
        return passwordEncoder.matches(passord, brukar.getPassord());
    }

    public boolean finnes(String brukarnavn) {
        return brukarRepository.existsById(brukarnavn);
    }
}