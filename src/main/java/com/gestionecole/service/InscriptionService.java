package com.gestionecole.service;

import com.gestionecole.model.Cours;
import com.gestionecole.model.Etudiant;
import com.gestionecole.model.Inscription;
import com.gestionecole.repository.InscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;

    public InscriptionService(InscriptionRepository inscriptionRepository) {
        this.inscriptionRepository = inscriptionRepository;
    }

    public Optional<Inscription> getInscriptionById(Long id) {
        return inscriptionRepository.findById(id);
    }

    public List<Inscription> getInscriptionsByEtudiant(Etudiant etudiant) {
        return inscriptionRepository.findByEtudiant(etudiant);
    }
    public List<Inscription> getInscriptionsByCours(Cours cours) {
        return inscriptionRepository.findByCours(cours);
    }

}