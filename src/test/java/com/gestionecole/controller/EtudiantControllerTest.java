package com.gestionecole.controller;

import com.gestionecole.model.*;
import com.gestionecole.service.EtudiantService;
import com.gestionecole.service.HoraireService;
import com.gestionecole.service.InscriptionService;
import com.gestionecole.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "student@example.com", roles = "ETUDIANT")
public class EtudiantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EtudiantService etudiantService;

    @MockBean
    private InscriptionService inscriptionService;

    @MockBean
    private HoraireService horaireService;

    @MockBean
    private NoteService noteService;

    private Etudiant etudiant;
    private Inscription inscription;
    private AnneeSection anneeSection;
    private Section section;
    private Cours cours;

    @BeforeEach
    void setup() {
        etudiant = new Etudiant();
        etudiant.setId(1L);

        section = new Section();
        section.setNom("Informatique");

        anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);

        cours = new Cours();
        cours.setId(42L);
        anneeSection.setCours(List.of(cours));

        inscription = new Inscription();
        inscription.setAnneeSection(anneeSection);
    }

    @Test
    void shouldDisplayCoursesWhenEtudiantAndInscriptionsExist() throws Exception {
        when(etudiantService.getEtudiantByEmail("student@example.com")).thenReturn(Optional.of(etudiant));
        when(inscriptionService.getInscriptionsByEtudiant(etudiant)).thenReturn(List.of(inscription));

        mockMvc.perform(get("/etudiant/cours"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/cours"))
                .andExpect(model().attributeExists("inscriptions"))
                .andExpect(model().attributeExists("cours"));
    }

    @Test
    void shouldDisplayEmptyCoursesWhenEtudiantNotFound() throws Exception {
        when(etudiantService.getEtudiantByEmail("student@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/etudiant/cours"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/cours"))
                .andExpect(model().attributeDoesNotExist("inscriptions"))
                .andExpect(model().attributeDoesNotExist("cours"));
    }

    @Test
    void shouldDisplayHoraireWhenInscriptionsExist() throws Exception {
        when(etudiantService.getEtudiantByEmail("student@example.com")).thenReturn(Optional.of(etudiant));
        when(inscriptionService.getInscriptionsByEtudiant(etudiant)).thenReturn(List.of(inscription));

        Section section = new Section();
        section.setNom("Informatique");

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);

        Cours cours = new Cours();
        cours.setIntitule("Mathématiques");
        cours.setAnneeSection(anneeSection); //  this is what was missing!

        Horaire horaire = new Horaire();
        horaire.setJour("Lundi");
        horaire.setHeureDebut("08:00");
        horaire.setHeureFin("10:00");
        horaire.setCours(cours); //  required to not break template rendering

        when(horaireService.getHoraireBySectionAndAnnee("Informatique", "2024-2025"))
                .thenReturn(List.of(horaire));

        mockMvc.perform(get("/etudiant/horaire"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/horaire"))
                .andExpect(model().attributeExists("horaires"));
    }


    @Test
    void shouldDisplayEmptyHoraireWhenNoInscriptions() throws Exception {
        when(etudiantService.getEtudiantByEmail("student@example.com")).thenReturn(Optional.of(etudiant));
        when(inscriptionService.getInscriptionsByEtudiant(etudiant)).thenReturn(List.of());

        mockMvc.perform(get("/etudiant/horaire"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/horaire"))
                .andExpect(model().attribute("horaires", List.of()));
    }

    @Test
    void shouldDisplayEmptyHoraireWhenEtudiantNotFound() throws Exception {
        when(etudiantService.getEtudiantByEmail("student@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/etudiant/horaire"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/horaire"))
                .andExpect(model().attributeDoesNotExist("horaires"));
    }

    @Test
    void shouldDisplayNotesWhenEtudiantAndInscriptionsExist() throws Exception {
        // Arrange
        when(etudiantService.getEtudiantByEmail("student@example.com"))
                .thenReturn(Optional.of(etudiant));

        when(inscriptionService.getInscriptionsByEtudiant(etudiant))
                .thenReturn(List.of(inscription));

        Cours cours = new Cours();
        cours.setIntitule("Mathématiques");

        Note note = new Note();
        note.setPremiereSession(14.5);
        note.setDeuxiemeSession(16.0);
        note.setCours(cours); //  Required to avoid NullPointerException in template

        when(noteService.getNotesByInscription(inscription))
                .thenReturn(List.of(note));

        // Act & Assert
        mockMvc.perform(get("/etudiant/note"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/note"))
                .andExpect(model().attributeExists("notes"));
    }

    @Test
    void shouldDisplayEmptyNotesWhenEtudiantNotFound() throws Exception {
        when(etudiantService.getEtudiantByEmail("student@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/etudiant/note"))
                .andExpect(status().isOk())
                .andExpect(view().name("etudiant/note"))
                .andExpect(model().attributeDoesNotExist("notes"));
    }
}
