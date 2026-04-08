package no.hvl.studyassist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Historikk {
    private Long sessionId;
    private String startSporsmal;
    private String oppfolgingsSporsmal;
    private String brukarRefleksjon;
    private String vurdering;
    private String fasitSvar;
    private Integer rating;
    private String temaId;
    private String temaNamn;
    private String emneId;
    private String emneNamn;
    private LocalDateTime opprettaTid;
    private String state;
}