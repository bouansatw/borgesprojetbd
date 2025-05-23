package com.gestionecole.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnneeSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String anneeAcademique;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "anneeSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cours> cours = new ArrayList<>();


    public AnneeSection(String anneeAcademique, Section section) {
        this.anneeAcademique = anneeAcademique;
        this.section = section;
    }


    @Override
    public String toString() {
        return "AnneeSection(anneeAcademique=" + getAnneeAcademique() + ")";
    }
}