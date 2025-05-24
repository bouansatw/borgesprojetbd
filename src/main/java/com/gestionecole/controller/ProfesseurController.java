package com.gestionecole.controller;

import com.gestionecole.model.Cours;
import com.gestionecole.model.Etudiant;
import com.gestionecole.model.Inscription;
import com.gestionecole.model.Note;
import com.gestionecole.service.CoursService;
import com.gestionecole.service.HoraireService;
import com.gestionecole.service.NoteService;
import com.gestionecole.service.ProfesseurService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/professeur")
public class ProfesseurController {

    private final ProfesseurService professeurService;
    private final CoursService coursService;
    private final HoraireService horaireService;
    private final NoteService noteService;

    public ProfesseurController(ProfesseurService professeurService, CoursService coursService, HoraireService horaireService, NoteService noteService) {
        this.professeurService = professeurService;
        this.coursService = coursService;
        this.horaireService = horaireService;
        this.noteService = noteService;
    }

    @GetMapping("/cours")
    public String voirCours(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        professeurService.getProfesseurByEmail(email)
                .ifPresent(professeur -> model.addAttribute("cours", coursService.getCoursByProfesseur(professeur)));
        return "professeur/cours/liste";
    }

    @GetMapping("/horaires")
    public String voirHoraires(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        professeurService.getProfesseurByEmail(email)
                .ifPresent(professeur -> model.addAttribute("horaires", horaireService.getHorairesByProfesseur(professeur)));
        return "professeur/horaires/liste";
    }

    @GetMapping("/notes")
    public String listeCoursNotes(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        professeurService.getProfesseurByEmail(email)
                .ifPresent(professeur -> model.addAttribute("cours", coursService.getCoursByProfesseur(professeur)));
        return "professeur/notes/liste_cours";
    }

    @GetMapping("/notes/{coursId}")
    public String listeEtudiantsPourCours(@PathVariable Long coursId, Model model) {
        Cours cours = coursService.getCoursById(coursId).orElse(null);
        if (cours != null) {
            List<Inscription> inscriptions = noteService.getInscriptionsByCours(cours);
            Map<Long, Note> notes = new HashMap<>();

            for (Inscription inscription : inscriptions) {
                noteService.getNoteByInscriptionAndCours(inscription.getId(), cours.getId())
                        .ifPresent(note -> notes.put(inscription.getEtudiant().getId(), note));
            }

            List<Etudiant> etudiants = inscriptions.stream()
                    .map(Inscription::getEtudiant)
                    .toList();

            model.addAttribute("cours", cours);
            model.addAttribute("inscriptions", inscriptions);
            model.addAttribute("etudiants", etudiants); // ✅ ADD THIS
            model.addAttribute("notes", notes);
        }
        return "professeur/notes/liste_etudiants";
    }


    @GetMapping("/notes/modifier/{inscriptionId}/{coursId}")
    public String modifierNoteForm(@PathVariable Long inscriptionId, @PathVariable Long coursId, Model model) {
        Optional<Note> noteOpt = noteService.getNoteByInscriptionAndCours(inscriptionId, coursId);

        Note note = noteOpt.orElseGet(() -> {
            Note newNote = new Note();
            Inscription inscription = new Inscription();
            inscription.setId(inscriptionId);
            Etudiant etudiant = new Etudiant();
            etudiant.setNom("Inconnu");
            inscription.setEtudiant(etudiant); // placeholder
            newNote.setInscription(inscription);

            Cours cours = new Cours();
            cours.setId(coursId);
            cours.setIntitule("Inconnu");
            newNote.setCours(cours);
            return newNote;
        });

        model.addAttribute("note", note);
        model.addAttribute("coursId", coursId);
        return "professeur/notes/modifier";
    }

    @PostMapping("/notes/modifier")
    public String enregistrerNote(@ModelAttribute("note") Note note,
                                  RedirectAttributes redirectAttributes) {
        if (note.getInscription() == null || note.getInscription().getId() == null
                || note.getCours() == null || note.getCours().getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Note incomplète : inscription ou cours manquant.");
            return "redirect:/professeur/notes/" + (note.getCours() != null ? note.getCours().getId() : "");
        }

        // ✅ Business rule: no deuxième session without première session
        if (note.getPremiereSession() == null && note.getDeuxiemeSession() != null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Impossible d'entrer une note de deuxième session si la note de première session est absente.");
            return "redirect:/professeur/notes/" + note.getCours().getId();
        }

        try {
            noteService.createOrUpdateNote(note);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/professeur/notes/" + note.getCours().getId();
        }

        return "redirect:/professeur/notes/" + note.getCours().getId();
    }
}
