package com.gestionecole.service;

import com.gestionecole.model.Cours;
import com.gestionecole.model.Inscription;
import com.gestionecole.model.Note;
import com.gestionecole.repository.CoursRepository;
import com.gestionecole.repository.EtudiantRepository;
import com.gestionecole.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final InscriptionService inscriptionService;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;

    public NoteService(NoteRepository noteRepository, InscriptionService inscriptionService, EtudiantRepository etudiantRepository, CoursRepository coursRepository) {
        this.noteRepository = noteRepository;
        this.inscriptionService = inscriptionService;
        this.etudiantRepository = etudiantRepository;
        this.coursRepository = coursRepository;
    }

    public List<Note> getNotesByCours(Cours cours) {
        return noteRepository.findByCours(cours);
    }

    public Optional<Note> getNoteByInscriptionAndCours(Long inscriptionId, Long coursId) {
        return noteRepository.findByInscription_IdAndCours_Id(inscriptionId, coursId);
    }

    public List<Inscription> getInscriptionsByCours(Cours cours) {
        return inscriptionService.getInscriptionsByCours(cours);
    }

    @Transactional
    public void createOrUpdateNote(Note note) {
        Long inscriptionId = note.getInscription().getId();
        Long coursId = note.getCours().getId();

        if (inscriptionId == null || coursId == null) {
            throw new IllegalArgumentException("Inscription ou cours manquant.");
        }

        Inscription inscription = inscriptionService.getInscriptionById(inscriptionId)
                .orElseThrow(() -> new IllegalStateException("Inscription introuvable."));

        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new IllegalStateException("Cours introuvable."));

        Optional<Note> existingNoteOpt = noteRepository.findByInscription_IdAndCours_Id(inscriptionId, coursId);

        Note noteToSave = existingNoteOpt.orElseGet(() -> {
            Note newNote = new Note();
            newNote.setInscription(inscription);
            newNote.setCours(cours);
            return newNote;
        });

        if (note.getDeuxiemeSession() != null &&
                (noteToSave.getPremiereSession() == null && note.getPremiereSession() == null)) {
            throw new IllegalStateException("Impossible d’ajouter une note en deuxième session sans note en première session.");
        }

        if (note.getPremiereSession() != null) {
            noteToSave.setPremiereSession(note.getPremiereSession());
        }

        if (note.getDeuxiemeSession() != null) {
            noteToSave.setDeuxiemeSession(note.getDeuxiemeSession());
        }

        noteRepository.save(noteToSave);
    }


    public List<Note> getNotesByInscription(Inscription inscription) {
        return noteRepository.findByInscription(inscription);
    }



}