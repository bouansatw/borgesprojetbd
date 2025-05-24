package com.gestionecole.controller;

import com.gestionecole.model.*;
import com.gestionecole.repository.AnneeSectionRepository;
import com.gestionecole.repository.CoursRepository;
import com.gestionecole.repository.HoraireRepository;
import com.gestionecole.repository.SectionRepository;
import com.gestionecole.service.CoursService;
import com.gestionecole.service.HoraireService;
import com.gestionecole.service.NoteService;
import com.gestionecole.service.ProfesseurService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "professeur@ecole.be", roles = "PROFESSEUR")
class ProfesseurControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private AnneeSectionRepository anneeSectionRepository;
    @Autowired
    private CoursRepository coursRepository;
    @Autowired
    private HoraireRepository horaireRepository;

    @MockBean
    private ProfesseurService professeurService;
    @MockBean
    private CoursService coursService;
    @MockBean
    private HoraireService horaireService;
    @MockBean
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        Section section = new Section();
        section.setNom("Informatique");
        section.setNbPlaces(30); // required if non-null in schema

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setSection(section);
        anneeSection.setAnneeAcademique("2024-2025");

        Cours cours = new Cours();
        cours.setIntitule("Programmation Java");
        cours.setAnneeSection(anneeSection);

        Horaire h1 = new Horaire();
        h1.setJour("LUNDI");
        h1.setHeureDebut("08:00");
        h1.setHeureFin("10:00");
        h1.setCours(cours);

        Horaire h2 = new Horaire();
        h2.setJour("MARDI");
        h2.setHeureDebut("14:00");
        h2.setHeureFin("16:00");
        h2.setCours(cours);

    }


    @Test
    void shouldRejectDeuxiemeSessionIfPremiereSessionMissing() throws Exception {
        mockMvc.perform(post("/professeur/notes/modifier")
                        .with(csrf())
                        .param("inscription.id", "1")
                        .param("cours.id", "42")
                        .param("deuxiemeSession", "12.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/professeur/notes/42"))
                .andExpect(flash().attribute("errorMessage",
                        "Impossible d'entrer une note de deuxième session si la note de première session est absente."));

        verifyNoInteractions(noteService);
    }

    @Test
    void shouldAcceptValidNoteAndPersist() throws Exception {
        mockMvc.perform(post("/professeur/notes/modifier")
                        .with(csrf())
                        .param("inscription.id", "1")
                        .param("cours.id", "42")
                        .param("premiereSession", "14.0")
                        .param("deuxiemeSession", "15.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/professeur/notes/42"));

        verify(noteService, times(1)).createOrUpdateNote(any(Note.class));
    }

    @Test
    void shouldHandleMissingNoteFieldsGracefully() throws Exception {
        mockMvc.perform(post("/professeur/notes/modifier").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/professeur/notes/"))
                .andExpect(flash().attribute("errorMessage",
                        "Note incomplète : inscription ou cours manquant."));

        verifyNoInteractions(noteService);
    }

    @Test
    void shouldShowCoursListForLoggedInProfesseur() throws Exception {
        Professeur prof = new Professeur();
        prof.setEmail("professeur@ecole.be");
        prof.setNom("Jean");

        Cours cours1 = new Cours();
        cours1.setIntitule("Java Avancé");
        cours1.setCode("JAVA202");
        cours1.setCredits(6);

        Cours cours2 = new Cours();
        cours2.setIntitule("Sécurité Réseau");
        cours2.setCode("SEC301");
        cours2.setCredits(5);

        List<Cours> coursList = List.of(cours1, cours2);

        when(professeurService.getProfesseurByEmail("professeur@ecole.be")).thenReturn(Optional.of(prof));
        when(coursService.getCoursByProfesseur(prof)).thenReturn(coursList);

        mockMvc.perform(get("/professeur/cours"))
                .andExpect(status().isOk())
                .andExpect(view().name("professeur/cours/liste"))
                .andExpect(model().attributeExists("cours"));

        verify(professeurService).getProfesseurByEmail("professeur@ecole.be");
        verify(coursService).getCoursByProfesseur(prof);
    }


    @Test
    void shouldShowHorairesForLoggedInProfesseur() throws Exception {
        Professeur prof = new Professeur();
        prof.setEmail("professeur@ecole.be");

        // Set up Section and AnneeSection
        Section section = new Section();
        section.setNom("Informatique");
        section.setNbPlaces(30);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setSection(section);
        anneeSection.setAnneeAcademique("2024-2025");

        // Set up Cours
        Cours cours = new Cours();
        cours.setIntitule("Programmation Java");
        cours.setAnneeSection(anneeSection); // ✅ This is the critical fix

        // Set up Horaires
        Horaire h1 = new Horaire();
        h1.setJour("LUNDI");
        h1.setHeureDebut("08:00");
        h1.setHeureFin("10:00");
        h1.setCours(cours);

        Horaire h2 = new Horaire();
        h2.setJour("MARDI");
        h2.setHeureDebut("14:00");
        h2.setHeureFin("16:00");
        h2.setCours(cours);

        List<Horaire> horaires = List.of(h1, h2);

        when(professeurService.getProfesseurByEmail("professeur@ecole.be")).thenReturn(Optional.of(prof));
        when(horaireService.getHorairesByProfesseur(prof)).thenReturn(horaires);

        mockMvc.perform(get("/professeur/horaires"))
                .andExpect(status().isOk())
                .andExpect(view().name("professeur/horaires/liste"))
                .andExpect(model().attributeExists("horaires"));

        verify(professeurService).getProfesseurByEmail("professeur@ecole.be");
        verify(horaireService).getHorairesByProfesseur(prof);
    }

    @Test
    @WithMockUser(username = "professeur@ecole.be", roles = "PROFESSEUR")
    void shouldReturnListeCoursWhenProfesseurExists() throws Exception {
        Professeur prof = new Professeur();
        prof.setEmail("professeur@ecole.be");

        Cours cours = new Cours();
        cours.setIntitule("Algorithmique");

        when(professeurService.getProfesseurByEmail("professeur@ecole.be"))
                .thenReturn(Optional.of(prof));
        when(coursService.getCoursByProfesseur(prof))
                .thenReturn(List.of(cours));

        mockMvc.perform(get("/professeur/notes"))
                .andExpect(status().isOk())
                .andExpect(view().name("professeur/notes/liste_cours"))
                .andExpect(model().attributeExists("cours"));

        verify(professeurService).getProfesseurByEmail("professeur@ecole.be");
        verify(coursService).getCoursByProfesseur(prof);
    }

    @Test
    void shouldReturnEtudiantsNotesForCours() throws Exception {
        Long coursId = 42L;

        Cours cours = new Cours();
        cours.setId(coursId);
        cours.setIntitule("Architecture Logicielle");

        Etudiant etudiant = new Etudiant();
        etudiant.setId(1L);
        etudiant.setNom("Alice");

        Inscription inscription = new Inscription();
        inscription.setId(100L);
        inscription.setEtudiant(etudiant);

        Note note = new Note();
        note.setId(999L);
        note.setPremiereSession(15.5);
        note.setCours(cours);
        note.setInscription(inscription);

        when(coursService.getCoursById(coursId)).thenReturn(Optional.of(cours));
        when(noteService.getInscriptionsByCours(cours)).thenReturn(List.of(inscription));
        when(noteService.getNoteByInscriptionAndCours(inscription.getId(), cours.getId()))
                .thenReturn(Optional.of(note));

        mockMvc.perform(get("/professeur/notes/{coursId}", coursId))
                .andExpect(status().isOk())
                .andExpect(view().name("professeur/notes/liste_etudiants"))
                .andExpect(model().attributeExists("cours"))
                .andExpect(model().attributeExists("inscriptions"))
                .andExpect(model().attributeExists("etudiants"))
                .andExpect(model().attributeExists("notes"));

        verify(coursService).getCoursById(coursId);
        verify(noteService).getInscriptionsByCours(cours);
        verify(noteService).getNoteByInscriptionAndCours(inscription.getId(), cours.getId());
    }

    @Test
    void shouldReturnNoteFormWithExistingNote() throws Exception {
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Jean");
        etudiant.setPrenom("Dupont");

        Inscription inscription = new Inscription();
        inscription.setId(1L);
        inscription.setEtudiant(etudiant);

        Cours cours = new Cours();
        cours.setId(42L);

        Note note = new Note();
        note.setInscription(inscription);
        note.setCours(cours);

        when(noteService.getNoteByInscriptionAndCours(1L, 42L)).thenReturn(Optional.of(note));

        mockMvc.perform(get("/professeur/notes/modifier/1/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("professeur/notes/modifier"))
                .andExpect(model().attributeExists("note"))
                .andExpect(model().attribute("coursId", 42L));
    }

    @Test
    void shouldReturnNoteFormWithNewNoteWhenNoteDoesNotExist() throws Exception {
        // Arrange
        // Create Etudiant
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Dupont");
        etudiant.setPrenom("Jean");

        // Create Inscription and associate Etudiant
        Inscription inscription = new Inscription();
        inscription.setId(1L); // ensure ID is set if accessed
        inscription.setEtudiant(etudiant);

        // Create Cours (needed by the controller/view logic)
        Cours cours = new Cours();
        cours.setId(42L);
        cours.setIntitule("Mathématiques");

        // Expect: no existing note
        when(noteService.getNoteByInscriptionAndCours(1L, 42L)).thenReturn(Optional.empty());
        // Return mock Inscription and Cours
        when(noteService.getInscriptionById(1L)).thenReturn(Optional.of(inscription));
        when(coursService.getCoursById(42L)).thenReturn(Optional.of(cours));

        // Act & Assert
        mockMvc.perform(get("/professeur/notes/modifier/1/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("professeur/notes/modifier"))
                .andExpect(model().attributeExists("note"))
                .andExpect(model().attribute("coursId", 42L));

        // Verify calls
        verify(noteService).getNoteByInscriptionAndCours(1L, 42L);
        verify(noteService).getInscriptionById(1L);
        verify(coursService).getCoursById(42L);
    }

    @Test
    void shouldRedirectIfInscriptionOrCoursIsMissing() throws Exception {
        when(noteService.getNoteByInscriptionAndCours(1L, 42L)).thenReturn(Optional.empty());
        when(noteService.getInscriptionById(1L)).thenReturn(Optional.empty());
        when(coursService.getCoursById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/professeur/notes/modifier/1/42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/professeur/notes"))
                .andExpect(flash().attribute("errorMessage", "Inscription ou cours introuvable."));

        verify(noteService).getNoteByInscriptionAndCours(1L, 42L);
        verify(noteService).getInscriptionById(1L);
        verify(coursService).getCoursById(42L);
    }


    @Test
    void shouldRegisterNoteSuccessfully() throws Exception {
        // Arrange
        Long coursId = 42L;
        Long inscriptionId = 1L;

        Cours cours = new Cours();
        cours.setId(coursId);

        Inscription inscription = new Inscription();
        inscription.setId(inscriptionId);

        Note note = new Note();
        note.setId(10L);
        note.setPremiereSession(14.5);
        note.setDeuxiemeSession(null);
        note.setCours(cours);
        note.setInscription(inscription);

        // Perform POST request with form-encoded parameters
        mockMvc.perform(post("/professeur/notes/modifier")
                        .with(csrf())
                        .param("id", String.valueOf(note.getId()))
                        .param("premiereSession", String.valueOf(note.getPremiereSession()))
                        .param("cours.id", String.valueOf(coursId))
                        .param("inscription.id", String.valueOf(inscriptionId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/professeur/notes/" + coursId));

        // Verify that the note service was called
        verify(noteService).createOrUpdateNote(any(Note.class));
    }


}
