package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sporsmal_session")
@Data
public class SporsmalSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "brukar_id")
    private Brukar brukar;

    @ManyToOne
    @JoinColumn(name = "tema_id")
    private Tema tema;

    @Column(columnDefinition = "TEXT")
    private String startSporsmal;

    @Column(columnDefinition = "TEXT")
    private String oppfolgingsSporsmal;

    @Column(columnDefinition = "TEXT")
    private String brukarRefleksjon;

    @Column(columnDefinition = "TEXT")
    private String vurdering;

    @Column(columnDefinition = "TEXT")
    private String fasitSvar;

    @Column
    private Integer rating;

    @Enumerated(EnumType.STRING)
    private SessionState state;

    private LocalDateTime opprettaTid;

    public enum SessionState {
        INITIAL,
        FOLLOW_UP,
        NEEDS_FOLLOWUP,
        FINAL_ANSWER,
        COMPLETED
    }
}