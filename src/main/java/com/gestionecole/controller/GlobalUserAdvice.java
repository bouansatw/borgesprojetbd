package com.gestionecole.controller;


import com.gestionecole.model.Utilisateur;
import com.gestionecole.repository.UtilisateurRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
public class GlobalUserAdvice {

    private final UtilisateurRepository utilisateurRepository;

    public GlobalUserAdvice(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @ModelAttribute("currentUser")
    public Utilisateur getCurrentUser(Principal principal) {
        if (principal != null) {
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(principal.getName());
            return utilisateurOpt.orElse(null);
        }
        return null;
    }
}

