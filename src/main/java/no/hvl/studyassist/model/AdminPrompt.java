package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_prompt")
@Data
public class AdminPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nokkel;

    @Column(columnDefinition = "TEXT")
    private String innhald;

    private int versjon;

    private boolean erAktiv;

    @ManyToOne
    @JoinColumn(name = "endra_av")
    private Brukar endraAv;

    private LocalDateTime endraTid;
}