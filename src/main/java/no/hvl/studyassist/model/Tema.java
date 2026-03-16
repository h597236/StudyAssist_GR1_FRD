package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tema")
@Data
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int temaId;

    private String namn;

    @ManyToOne
    @JoinColumn(name = "emneId")
    private Emne emne;
}