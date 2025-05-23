package com.gestionecole.repository;

import com.gestionecole.model.Cours;
import com.gestionecole.model.Etudiant;
import com.gestionecole.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByEtudiant(Etudiant etudiant);
    Optional<Note> findByEtudiant_IdAndCours_Id(Long etudiantId, Long coursId);
    List<Note> findByCours(Cours cours); // ✅ ligne à ajouter
    Optional<Note> findByInscription_IdAndCours_Id(Long inscriptionId, Long coursId);
}
