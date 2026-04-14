package no.hvl.studyassist.service;

import no.hvl.studyassist.model.Brukar;
import no.hvl.studyassist.model.Emne;
import no.hvl.studyassist.model.Tema;
import no.hvl.studyassist.repository.BrukarRepository;
import no.hvl.studyassist.repository.EmneRepository;
import no.hvl.studyassist.repository.SporsmalRepository;
import no.hvl.studyassist.repository.TemaRepository;
import no.hvl.studyassist.service.ai.AiModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmneService {

    private final EmneRepository emneRepository;
    private final BrukarRepository brukarRepository;
    private final TemaRepository temaRepository;
    private final SporsmalRepository sporsmalRepository;
    private final AiModelService aiModelService;

    public EmneService(EmneRepository emneRepository,
                       BrukarRepository brukarRepository,
                       TemaRepository temaRepository,
                       SporsmalRepository sporsmalRepository,
                       AiModelService aiModelService) {
        this.emneRepository = emneRepository;
        this.brukarRepository = brukarRepository;
        this.temaRepository = temaRepository;
        this.sporsmalRepository = sporsmalRepository;
        this.aiModelService = aiModelService;
    }

    public Emne save(Emne emne) {
        if (emne.getBrukar() != null && emne.getBrukar().getId() != 0) {
            Brukar brukar = brukarRepository.findById(emne.getBrukar().getId())
                    .orElseThrow(() -> new RuntimeException("Brukar finst ikkje"));
            emne.setBrukar(brukar);
        }

        // Finn URL i beskrivelse
        String url = finnUrl(emne.getBeskrivelse());
        emne.setLaeringsurl(url);

        if (url != null) {
            // Hent rå tekst frå URL
            String raaTekst = hentTekstFraUrl(url);
            if (raaTekst != null) {
                // Send til AI for å trekke ut læringsmål
                String laeringsmaal = aiModelService.askRaw(
                        "Du er ein assistent som trekk ut læringsmål frå nettsider. " +
                                "Returner BERRE ein kortfatta punktliste med læringsmåla. " +
                                "Ikkje inkluder noko anna tekst. " +
                                "Viss du ikkje finn læringsmål, returner ingenting.",
                        raaTekst
                );
                emne.setLaeringsmaal(laeringsmaal);
            }
        }

        return emneRepository.save(emne);
    }

    public List<Emne> findByBrukarId(int brukarId) {
        return emneRepository.findByBrukar_Id(brukarId);
    }

    public boolean eigesAvBrukar(int emneId, int brukarId) {
        return emneRepository.findById(emneId)
                .map(emne -> emne.getBrukar() != null && emne.getBrukar().getId() == brukarId)
                .orElse(false);
    }

    @Transactional
    public void deleteById(int emneId) {
        List<Tema> temaListe = temaRepository.findByEmneEmneId(emneId);
        for (Tema tema : temaListe) {
            sporsmalRepository.deleteByTemaTemaId(tema.getTemaId());
        }
        temaRepository.deleteAll(temaListe);
        emneRepository.deleteById(emneId);
    }

    private String finnUrl(String tekst) {
        if (tekst == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("https?://\\S+")
                .matcher(tekst);
        return m.find() ? m.group() : null;
    }

    private String hentTekstFraUrl(String urlString) {
        try {
            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "StudyAssist/1.0");

            if (conn.getResponseCode() != 200) return null;

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream())
            );
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }
            reader.close();

            String tekst = html.toString()
                    .replaceAll("(?is)<script[^>]*>.*?</script>", "")
                    .replaceAll("(?is)<style[^>]*>.*?</style>", "")
                    .replaceAll("<[^>]+>", " ")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (tekst.length() > 3000) tekst = tekst.substring(0, 3000);
            return tekst.isBlank() ? null : tekst;

        } catch (Exception e) {
            return null;
        }
    }
}