package com.gestionecole.service;

import com.gestionecole.model.Cours;
import com.gestionecole.model.Etudiant;
import com.gestionecole.model.Inscription;
import com.gestionecole.model.Note;
import com.gestionecole.repository.CoursRepository;
import com.gestionecole.repository.EtudiantRepository;
import com.gestionecole.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public List<Note> getNotesByEtudiant(Etudiant etudiant) {
        return noteRepository.findByEtudiant(etudiant);
    }

    public Optional<Note> getNoteByEtudiantAndCours(Long etudiantId, Long coursId) {
        return noteRepository.findByEtudiant_IdAndCours_Id(etudiantId, coursId);
    }

    public List<Note> getNotesByCours(Cours cours) {
        return noteRepository.findByCours(cours);
    }

    @Transactional
    public void saveNote(Note note) {
        noteRepository.saveAndFlush(note);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }

    public List<Etudiant> getEtudiantsInscritsAuCours(Cours cours) {
        return inscriptionService.getInscriptionsByCours(cours)
                .stream()
                .map(Inscription::getEtudiant)
                .collect(Collectors.toList());
    }

    public Optional<Etudiant> findEtudiantById(Long id) {
        return etudiantRepository.findById(id);
    }

    public Optional<Cours> findCoursById(Long id) {
        return coursRepository.findById(id);
    }

    @Transactional
    public void createOrUpdateNote(Note note) {
        Long inscriptionId = note.getInscription().getId();
        Long coursId = note.getCours().getId();

        // Try to find existing note for this inscription + cours
        Optional<Note> existingNoteOpt = noteRepository.findByInscription_IdAndCours_Id(inscriptionId, coursId);

        Note noteToSave = existingNoteOpt.orElseGet(() -> {
            Note newNote = new Note();
            newNote.setInscription(note.getInscription());
            newNote.setCours(note.getCours());
            return newNote;
        });

        // 🔐 Business rule: Deuxième session requires première session
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


}