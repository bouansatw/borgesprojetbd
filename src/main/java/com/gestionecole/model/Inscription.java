package com.gestionecole.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateInscription;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "annee_section_id", nullable = false)
    private AnneeSection anneeSection;

    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note> notes;


    @Override
    public String toString() {
        return "Inscription(dateInscription=" + dateInscription + ")";
    }
}
