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

    public Brukar registrer(String email, String passord) {
        Brukar brukar = new Brukar();
        brukar.setEmail(email);
        brukar.setPassord(passwordEncoder.encode(passord));
        return brukarRepository.save(brukar);
    }

    public Brukar loggInn(String email, String passord) {
        Brukar brukar = brukarRepository.findByEmail(email);

        if (brukar == null) return null;

        if (passwordEncoder.matches(passord, brukar.getPassord())) {
            return brukar;
        }

        return null;
    }

    public boolean finnes(String email) {
        return brukarRepository.findByEmail(email) != null;
    }

    public Brukar findById(int id) {
        return brukarRepository.findById(id).orElse(null);
    }
}