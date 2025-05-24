package com.gestionecole.repository;

import com.gestionecole.model.Horaire;
import com.gestionecole.model.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoraireRepository extends JpaRepository<Horaire, Long> {

    @Query("""
                SELECT h FROM Horaire h
                WHERE h.cours.anneeSection.section.nom = :sectionNom
                  AND h.cours.anneeSection.anneeAcademique = :anneeAcademique
            """)
    List<Horaire> findBySectionNomAndAnneeAcademique(@Param("sectionNom") String sectionNom,
                                                     @Param("anneeAcademique") String anneeAcademique);

    List<Horaire> findByCours_Professeur(Professeur professeur);
}
