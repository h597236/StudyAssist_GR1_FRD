package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.repository.BrukarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminBrukarService {

    @Autowired
    private BrukarRepository brukarRepository;

    public List<Map<String, Object>> getAllBrukarar(String email, String rolle) {
        List<Brukar> alle = brukarRepository.findAll();

        if (email != null && !email.isBlank()) {
            String lower = email.toLowerCase();
            alle = alle.stream()
                    .filter(b -> b.getEmail() != null && b.getEmail().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }

        if (rolle != null && !rolle.isBlank()) {
            alle = alle.stream()
                    .filter(b -> rolle.equalsIgnoreCase(b.getRolle()))
                    .collect(Collectors.toList());
        }

        return alle.stream().map(b -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", b.getId());
            dto.put("email", b.getEmail());
            dto.put("rolle", b.getRolle() != null ? b.getRolle() : "VANLIG");
            return dto;
        }).collect(Collectors.toList());
    }

    public void slettBrukar(int id) {
        brukarRepository.deleteById(id);
    }

    public void endreRolle(int id, String nyRolle) {
        Brukar brukar = brukarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brukar ikkje funnen: " + id));
        brukar.setRolle(nyRolle);
        brukarRepository.save(brukar);
    }
}