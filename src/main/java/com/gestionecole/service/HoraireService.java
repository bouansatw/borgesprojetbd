package com.gestionecole.service;

import com.gestionecole.model.Horaire;
import com.gestionecole.model.Professeur;
import com.gestionecole.repository.HoraireRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HoraireService {

    private final HoraireRepository horaireRepository;

    public HoraireService(HoraireRepository horaireRepository) {
        this.horaireRepository = horaireRepository;
    }

    public List<Horaire> getAllHoraires() {
        return horaireRepository.findAll();
    }

    public Optional<Horaire> getHoraireById(Long id) {
        return horaireRepository.findById(id);
    }

    public Horaire saveHoraire(Horaire horaire) {
        return horaireRepository.save(horaire);
    }

    public void deleteHoraire(Long id) {
        horaireRepository.deleteById(id);
    }

    public List<Horaire> getHorairesByProfesseur(Professeur professeur) {
        return horaireRepository.findByCours_Professeur(professeur);
    }

    public List<Horaire> getHoraireBySectionAndAnnee(String sectionNom, String anneeAcademique) {
        return horaireRepository.findBySectionNomAndAnneeAcademique(sectionNom, anneeAcademique);
    }
}
