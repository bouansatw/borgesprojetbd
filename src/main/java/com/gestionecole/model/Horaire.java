package com.gestionecole.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Horaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jour;
    private String heureDebut;
    private String heureFin;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;
}
