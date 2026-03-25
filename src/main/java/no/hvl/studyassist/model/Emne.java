package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
@Table(name = "emne")
@Data
public class Emne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int emneId;

    private String namn;
    private String beskrivelse;

    @ManyToOne
    @JoinColumn(name = "brukar_id")
    private Brukar brukar;

    @JsonManagedReference
    @OneToMany(mappedBy = "emne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tema> tema;
}