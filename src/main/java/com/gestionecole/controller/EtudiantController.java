package com.gestionecole.controller;

import com.gestionecole.model.Inscription;
import com.gestionecole.service.EtudiantService;
import com.gestionecole.service.HoraireService;
import com.gestionecole.service.InscriptionService;
import com.gestionecole.service.NoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/etudiant")
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final InscriptionService inscriptionService;
    private final HoraireService horaireService;
    private final NoteService noteService;

    public EtudiantController(EtudiantService etudiantService,
                              InscriptionService inscriptionService,
                              HoraireService horaireService,
                              NoteService noteService) {
        this.etudiantService = etudiantService;
        this.inscriptionService = inscriptionService;
        this.horaireService = horaireService;
        this.noteService = noteService;
    }

    @GetMapping("/cours")
    public String voirCours(Model model) {
        etudiantService.getEtudiantByEmail(getCurrentUserEmail()).ifPresent(etudiant -> {
            List<Inscription> inscriptions = inscriptionService.getInscriptionsByEtudiant(etudiant);
            model.addAttribute("inscriptions", inscriptions);
        });
        return "etudiant/cours";
    }

    @GetMapping("/horaire")
    public String voirHoraire(Model model) {
        etudiantService.getEtudiantByEmail(getCurrentUserEmail()).ifPresent(etudiant -> {
            List<Inscription> inscriptions = inscriptionService.getInscriptionsByEtudiant(etudiant);
            if (!inscriptions.isEmpty()) {
                // Assume all inscriptions belong to the same AnneeSection
                String sectionNom = inscriptions.get(0).getAnneeSection().getSection().getNom();
                String annee = inscriptions.get(0).getAnneeSection().getAnneeAcademique();
                model.addAttribute("horaires", horaireService.getHoraireBySectionAndAnnee(sectionNom, annee));
            } else {
                model.addAttribute("horaires", List.of());
            }
        });
        return "etudiant/horaire";
    }

    @GetMapping("/note")
    public String voirNotes(Model model) {
        etudiantService.getEtudiantByEmail(getCurrentUserEmail()).ifPresent(etudiant -> {
            List<Inscription> inscriptions = inscriptionService.getInscriptionsByEtudiant(etudiant);
            model.addAttribute("notes", inscriptions.stream()
                    .flatMap(inscription -> noteService.getNotesByInscription(inscription).stream())
                    .toList());
        });
        return "etudiant/note";
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
