package com.gestionecole.service;

import com.gestionecole.model.Section;
import com.gestionecole.repository.InscriptionRepository;
import com.gestionecole.repository.SectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final InscriptionRepository inscriptionRepository;

    public SectionService(SectionRepository sectionRepository,
                          InscriptionRepository inscriptionRepository) {
        this.sectionRepository = sectionRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    public List<Section> findAllSectionsWithRemainingPlaces() {
        List<Section> sections = sectionRepository.findAll();
        for (Section section : sections) {
            int nbInscrits = inscriptionRepository.countByAnneeSection_Section_Id(section.getId());
            section.setPlacesRestantes(section.getNbPlaces() - nbInscrits);
        }
        return sections;
    }

}
