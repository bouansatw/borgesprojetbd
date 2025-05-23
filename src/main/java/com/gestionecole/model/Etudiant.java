package com.gestionecole.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Etudiant extends Utilisateur {

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private List<Inscription> inscriptions;

    @Transient
    private Note noteForCours;

    private String info;
    private String photo;

    public boolean isInscrit() {
        return inscriptions != null && !inscriptions.isEmpty();
    }

    @Override
    public String toString() {
        return "Etudiant(nom=" + getNom() +
                ", prenom=" + getPrenom() +
                ", email=" + getEmail() + ")";
    }
}
