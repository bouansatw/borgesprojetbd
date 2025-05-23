package com.gestionecole.repository;

import com.gestionecole.model.Cours;
import com.gestionecole.model.Inscription;
import com.gestionecole.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByCours(Cours cours);
    Optional<Note> findByInscription_IdAndCours_Id(Long inscriptionId, Long coursId);

    List<Note> findByInscription(Inscription inscription);

}
