package no.hvl.studyassist.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;


@Entity
@Table(name = "emne")
@Data
public class Emne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int emneId;

    private String namn;
    private String laeringspunkt;

    @ManyToOne
    @JoinColumn(name = "brukarnamn")
    private Brukar brukar;

    @OneToMany(mappedBy = "emne")
    private List<Tema> tema;
}