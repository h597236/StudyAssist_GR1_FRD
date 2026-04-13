package no.hvl.studyassist.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "brukar")
@Data
public class Brukar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String email;
    private String passord;
    private String rolle;
}