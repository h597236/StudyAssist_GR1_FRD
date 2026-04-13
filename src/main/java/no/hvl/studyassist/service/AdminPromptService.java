package no.hvl.studyassist.service;

import no.hvl.studyassist.model.AdminPrompt;
import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.repository.AdminPromptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminPromptService {

    private final AdminPromptRepository adminPromptRepository;

    public AdminPromptService(AdminPromptRepository adminPromptRepository) {
        this.adminPromptRepository = adminPromptRepository;
    }

    public Optional<AdminPrompt> getAktivPrompt(String nokkel) {
        return adminPromptRepository.findByNokkelAndErAktivTrue(nokkel);
    }

    public List<AdminPrompt> getVersjonar(String nokkel) {
        return adminPromptRepository.findByNokkelOrderByVersjonAsc(nokkel);
    }

    public Optional<AdminPrompt> getById(Long id) {
        return adminPromptRepository.findById(id);
    }

    @Transactional
    public AdminPrompt lagreNyVersjon(String nokkel, String innhald, Brukar admin) {
        List<AdminPrompt> versjonar = adminPromptRepository.findByNokkelOrderByVersjonAsc(nokkel);

        if (versjonar.size() >= 4) {
            adminPromptRepository.delete(versjonar.get(0));
            versjonar.remove(0);
        }

        versjonar.forEach(v -> v.setErAktiv(false));
        adminPromptRepository.saveAll(versjonar);

        AdminPrompt ny = new AdminPrompt();
        ny.setNokkel(nokkel);
        ny.setInnhald(innhald);
        ny.setVersjon(versjonar.size() + 1);
        ny.setErAktiv(true);
        ny.setEndraAv(admin);
        ny.setEndraTid(LocalDateTime.now());

        return adminPromptRepository.save(ny);
    }

    @Transactional
    public AdminPrompt gjenopprett(Long id, Brukar admin) {
        AdminPrompt gamal = adminPromptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prompt finst ikkje"));
        return lagreNyVersjon(gamal.getNokkel(), gamal.getInnhald(), admin);
    }
}