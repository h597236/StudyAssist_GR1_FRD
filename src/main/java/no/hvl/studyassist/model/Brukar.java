package no.hvl.studyassist.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "brukar")
@Data
public class Brukar {

    @Id
    private String brukarnavn;

    private String passord;

    @OneToMany(mappedBy = "brukar")
    private List<Emne> emner;
}