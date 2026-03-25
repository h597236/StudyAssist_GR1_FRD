package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sporsmal_session")
@Data
public class SporsmalSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brukar_id")
    private Brukar brukar;

    @ManyToOne
    @JoinColumn(name = "tema_id")
    private Tema tema;

    private String originalSporsmal;
    private String followUpSporsmal;
    private String svar;
    private String vurdering;
    private String fasit;

    @Enumerated(EnumType.STRING)
    private SessionState state;

    public enum SessionState {
        FOLLOW_UP,
        ANSWERED,
        FINAL_ANSWER,
        COMPLETED
    }
}