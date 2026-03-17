package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tema")
@Data
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int temaId;

    private String navn;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "emne_id")
    private Emne emne;
}