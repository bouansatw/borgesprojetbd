package com.gestionecole.service;

import com.gestionecole.model.*;
import com.gestionecole.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NoteServiceTest {

    @Autowired private CoursRepository coursRepository;
    @Autowired
    private EtudiantRepository etudiantRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AnneeSectionRepository anneeSectionRepository;
    @Autowired
    private InscriptionRepository inscriptionRepository;
    @Autowired
    private NoteService noteService;
    @Autowired private NoteRepository noteRepository;


    private Etudiant etudiant;
    private Cours cours;

    @BeforeEach
    void setup() {
        etudiant = new Etudiant();
        etudiant.setNom("Doe");
        etudiant.setPrenom("John");
        etudiant.setEmail("john.doe@ecole.be");
        etudiant.setPassword("secret");
        etudiant.setRole("ROLE_ETUDIANT");
        etudiantRepository.save(etudiant);

        cours = new Cours();
        cours.setIntitule("Programmation");
        coursRepository.save(cours);
    }

    @Test
    void testCreateNoteWithPremiereSession() {
        // Setup Section and AnneeSection
        Section section = new Section("Informatique", 20);
        sectionRepository.save(section);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection);

        // Setup Cours
        Cours cours = new Cours();
        cours.setCode("INFO101");
        cours.setIntitule("Intro à l'Informatique");
        cours.setCredits(3);
        cours.setAnneeSection(anneeSection);
        coursRepository.save(cours);

        // Setup Etudiant
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Test");
        etudiant.setPrenom("Etudiant");
        etudiant.setEmail("test.etudiant@ecole.be");
        etudiant.setPassword(passwordEncoder.encode("test123"));
        etudiant.setRole("ROLE_ETUDIANT");
        etudiant.setMatricule("E-10001");
        etudiantRepository.save(etudiant);

        // Setup Inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setAnneeSection(anneeSection);
        inscriptionRepository.save(inscription);

        // Act
        Note note = new Note();
        note.setInscription(inscription);
        note.setCours(cours);
        note.setPremiereSession(14.0);
        noteService.createOrUpdateNote(note);

        // Assert
        Note saved = noteRepository.findByInscription_IdAndCours_Id(inscription.getId(), cours.getId()).orElseThrow();
        assertThat(saved.getPremiereSession()).isEqualTo(14.0);
        assertThat(saved.getDeuxiemeSession()).isNull();
    }


    @Test
    void testCreateNoteWithDeuxiemeSessionOnlyShouldFail() {
        // Setup Section and AnneeSection
        Section section = new Section("Cybersécurité", 20);
        sectionRepository.save(section);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection);

        // Setup Cours
        Cours cours = new Cours();
        cours.setCode("SEC101");
        cours.setIntitule("Sécurité Informatique");
        cours.setCredits(4);
        cours.setAnneeSection(anneeSection);
        coursRepository.save(cours);

        // Setup Etudiant
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Fail");
        etudiant.setPrenom("Test");
        etudiant.setEmail("fail.test@ecole.be");
        etudiant.setPassword(passwordEncoder.encode("fail123"));
        etudiant.setRole("ROLE_ETUDIANT");
        etudiant.setMatricule("E-99999");
        etudiantRepository.save(etudiant);

        // Setup Inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setAnneeSection(anneeSection);
        inscriptionRepository.save(inscription);

        // Prepare a note with only deuxième session filled
        Note note = new Note();
        note.setInscription(inscription);
        note.setCours(cours);
        note.setPremiereSession(null);
        note.setDeuxiemeSession(12.5);

        // Expect failure due to missing première session
        assertThatThrownBy(() -> noteService.createOrUpdateNote(note))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deuxième session");
    }


    @Test
    void testAddDeuxiemeSessionAfterPremiereSessionExists() {
        // Setup Section and AnneeSection
        Section section = new Section("Cybersécurité", 20);
        sectionRepository.save(section);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection);

        // Setup Cours
        Cours cours = new Cours();
        cours.setCode("SEC201");
        cours.setIntitule("Sécurité Avancée");
        cours.setCredits(5);
        cours.setAnneeSection(anneeSection);
        coursRepository.save(cours);

        // Setup Etudiant
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Valjean");
        etudiant.setPrenom("Jean");
        etudiant.setEmail("jean.valjean@ecole.be");
        etudiant.setPassword(passwordEncoder.encode("password"));
        etudiant.setMatricule("E-10010");
        etudiant.setRole("ROLE_ETUDIANT");
        etudiantRepository.save(etudiant);

        // Setup Inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setAnneeSection(anneeSection);
        inscriptionRepository.save(inscription);

        // Create note with première session
        Note note = new Note();
        note.setInscription(inscription);
        note.setCours(cours);
        note.setPremiereSession(11.0);
        noteService.createOrUpdateNote(note);

        // Update with deuxième session
        Note update = new Note();
        update.setInscription(inscription);
        update.setCours(cours);
        update.setDeuxiemeSession(13.0);
        noteService.createOrUpdateNote(update);

        // Verify
        Note saved = noteRepository.findByInscription_IdAndCours_Id(inscription.getId(), cours.getId()).orElseThrow();
        assertThat(saved.getPremiereSession()).isEqualTo(11.0);
        assertThat(saved.getDeuxiemeSession()).isEqualTo(13.0);
    }

    @Test
    void testUpdatePremiereSessionWithoutAffectingDeuxieme() {
        // Setup Section and AnneeSection
        Section section = new Section("Cybersécurité", 20);
        sectionRepository.save(section);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection);

        // Setup Cours
        Cours cours = new Cours();
        cours.setCode("SEC203");
        cours.setIntitule("Sécurité Offensive");
        cours.setCredits(5);
        cours.setAnneeSection(anneeSection);
        coursRepository.save(cours);

        // Setup Etudiant
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Martin");
        etudiant.setPrenom("Bob");
        etudiant.setEmail("bob.martin@ecole.be");
        etudiant.setPassword(passwordEncoder.encode("Pass1234"));
        etudiant.setMatricule("E-10011");
        etudiant.setRole("ROLE_ETUDIANT");
        etudiantRepository.save(etudiant);

        // Setup Inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setAnneeSection(anneeSection);
        inscriptionRepository.save(inscription);

        // First save: both sessions
        Note note = new Note();
        note.setInscription(inscription);
        note.setCours(cours);
        note.setPremiereSession(12.0);
        note.setDeuxiemeSession(14.0);
        noteService.createOrUpdateNote(note);

        // Second save: update only première session
        Note update = new Note();
        update.setInscription(inscription);
        update.setCours(cours);
        update.setPremiereSession(15.0); // Only update this
        noteService.createOrUpdateNote(update);

        // Assert state in DB
        Note saved = noteRepository.findByInscription_IdAndCours_Id(inscription.getId(), cours.getId()).orElseThrow();
        assertThat(saved.getPremiereSession()).isEqualTo(15.0);
        assertThat(saved.getDeuxiemeSession()).isEqualTo(14.0); // Should remain unchanged
    }

}
