package com.gestionecole.service;

import com.gestionecole.model.*;
import com.gestionecole.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EtudiantServiceTest {

    @Autowired private EtudiantService etudiantService;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private AnneeSectionRepository anneeSectionRepository;
    @Autowired private HoraireRepository horaireRepository;
    @Autowired private CoursRepository coursRepository;
    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private InscriptionRepository inscriptionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void testRegisterStudentCreatesEtudiantMatriculeAnneeAndInscriptions() {
        // Arrange
        Section section = new Section("Informatique", 25);
        sectionRepository.save(section);

        String academicYear = "2024-2025";

        AnneeSection anneeSection = new AnneeSection(academicYear, section);
        anneeSectionRepository.save(anneeSection);

        Cours cours = new Cours();
        cours.setIntitule("Java avancé");
        coursRepository.save(cours);

        Horaire horaire = new Horaire();
        horaire.setJour("LUNDI");
        horaire.setHeureDebut("08:00");
        horaire.setHeureFin("10:00");
        horaire.setCours(cours);
        horaire.setAnneeSection(anneeSection);
        horaireRepository.save(horaire);

        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Alice");
        etudiant.setPrenom("Durand");
        etudiant.setEmail("alice.durand@ecole.be");
        etudiant.setPassword("pass123");

        // Act
        etudiantService.registerStudent(etudiant, section.getId());

        // Assert
        Etudiant saved = etudiantRepository.findByEmail("alice.durand@ecole.be").orElseThrow();

        assertThat(saved.getMatricule()).matches("E-\\d{5}");
        assertThat(saved.getSection()).isEqualTo(section);
        assertThat(saved.getAnneeSection()).isEqualTo(anneeSection);
        assertThat(passwordEncoder.matches("pass123", saved.getPassword())).isTrue();

        List<Inscription> inscriptions = inscriptionRepository.findByEtudiant(saved);
        assertThat(inscriptions).hasSize(1);
        assertThat(inscriptions.getFirst().getCours().getIntitule()).isEqualTo("Java avancé");
    }
}
