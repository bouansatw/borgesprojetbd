package com.gestionecole.controller;

import com.gestionecole.model.*;
import com.gestionecole.repository.*;
import jakarta.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private CoursRepository coursRepository;
    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private ProfesseurRepository professeurRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private HoraireRepository horaireRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AnneeSectionRepository anneeSectionRepository;
    @Autowired private InscriptionRepository inscriptionRepository;


    @BeforeEach
    void cleanUpDatabase() {
        entityManager.createNativeQuery("DELETE FROM note").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM inscription").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM horaire").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM cours").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM etudiant").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM annee_section").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM section").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM professeur").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM utilisateur").executeUpdate();

    }




    @Test
    void testJpaContext() {
        assertNotNull(entityManager);
    }

    @Test
    void testPageLoginAccessible() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(Matchers.containsString("form")));
    }

    @Test
    void testListTables() {
        jdbcTemplate.query(
                "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = 'PUBLIC'",
                (rs) -> {
                    System.out.println("==== DATABASE TABLES ====");
                    while (rs.next()) {
                        System.out.println(rs.getString("table_name"));
                    }
                    System.out.println("=========================");
                }
        );
    }


    @Test
    void testRedirectionEtudiantInscrit() throws Exception {
        Section section = new Section("Cyber", 30);
        sectionRepository.save(section);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection); // 🔥 Save it before referencing

        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Etudiant");
        etudiant.setPrenom("Inscrit");
        etudiant.setEmail("etudiant.inscrit@ecole.be");
        etudiant.setPassword(passwordEncoder.encode("etudiant123"));
        etudiant.setRole("ROLE_ETUDIANT");
        etudiantRepository.save(etudiant);

        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setAnneeSection(anneeSection);
        inscriptionRepository.save(inscription);

        mockMvc.perform(formLogin("/auth/login")
                        .user("etudiant.inscrit@ecole.be")
                        .password("etudiant123"))
                .andExpect(authenticated().withUsername("etudiant.inscrit@ecole.be"))
                .andExpect(redirectedUrl("/etudiant/cours"));
    }



    @Test
    void testRedirectionProfesseurAfterLogin() throws Exception {
        Professeur prof = new Professeur();
        prof.setNom("Jean");
        prof.setPrenom("Professeur");
        prof.setEmail("professeur@ecole.be");
        prof.setPassword(passwordEncoder.encode("professeur123"));
        prof.setRole("ROLE_PROFESSEUR");
        prof.setMatricule("P-10001"); // 🔥 Matricule is set

        professeurRepository.save(prof);  // 🔥 Then save child (Professeur)

        mockMvc.perform(formLogin("/auth/login")
                        .user("professeur@ecole.be")
                        .password("professeur123"))
                .andExpect(authenticated().withUsername("professeur@ecole.be"))
                .andExpect(redirectedUrl("/professeur/cours"));
    }


    @Test
    void testLienInscriptionPresentSurLoginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("href=\"/auth/register\"")));
    }

    @Test
    void testLoginInvalideRedirigeVersErreur() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
                        .user("email-inexistant@ecole.be")
                        .password("motdepasseincorrect"))
                .andExpect(redirectedUrl("/auth/login?error=true"));
    }

    @Test
    void testPageRegisterAccessible() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(Matchers.containsString("Inscription Étudiant")));
    }


    @Test
    void testRegisterEtudiantViaFormCreatesMatriculeAndInscriptions() throws Exception {
        // Arrange
        Section section = new Section("Cybersécurité", 20);
        sectionRepository.save(section);

        String academicYear = "2024-2025";

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique(academicYear);
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection);

        Cours cours = new Cours();
        cours.setIntitule("Sécurité Réseau");
        cours.setCode("SEC401");
        cours.setCredits(5);
        cours.setDescription("Cours de sécurité avancée");
        cours.setAnneeSection(anneeSection);
        anneeSection.getCours().add(cours);
        coursRepository.save(cours);

        Horaire horaire = new Horaire();
        horaire.setJour("VENDREDI");
        horaire.setHeureDebut("10:00");
        horaire.setHeureFin("12:00");
        horaire.setCours(cours);
        horaireRepository.save(horaire);

        // Act: Submit registration form
        mockMvc.perform(post("/auth/register")
                        .param("nom", "Jean")
                        .param("prenom", "Valjean")
                        .param("email", "jean.valjean@ecole.be")
                        .param("password", "Valjean123")
                        .param("sectionId", section.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // Assert: Verify Etudiant saved
        Etudiant saved = etudiantRepository.findByEmail("jean.valjean@ecole.be").orElseThrow();

        assertThat(saved.getMatricule()).matches("E-\\d{5}");
        assertThat(passwordEncoder.matches("Valjean123", saved.getPassword())).isTrue();

        List<Inscription> inscriptions = inscriptionRepository.findByEtudiant(saved);
        assertThat(inscriptions).hasSize(1);
        assertThat(inscriptions.get(0).getAnneeSection().getSection()).isEqualTo(section);
        assertThat(inscriptions.get(0).getAnneeSection().getAnneeAcademique()).isEqualTo(academicYear);

        List<Cours> coursList = coursRepository.findAll();
        assertThat(coursList).anyMatch(c -> c.getIntitule().equals("Sécurité Réseau"));
    }



    @Test
    void testRegisterEtudiantValidationError() throws Exception {
        mockMvc.perform(
                        post("/auth/register")
                                .param("nom", "") // Empty name -> validation error
                                .param("prenom", "Bob")
                                .param("email", "bob.martin@ecole.be")
                                .param("password", "Pass1234")
                                .param("sectionId", "1") // even if sectionId is OK
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(Matchers.containsString("form"))); // The form should still be there
    }



    @Test
    void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Etudiant");
        etudiant.setPrenom("NonInscrit");
        etudiant.setEmail("bob.martin@ecole.be");
        etudiant.setPassword(encoder.encode("Pass1234"));
        etudiant.setMatricule("E-10009");
        etudiant.setRole("ROLE_ETUDIANT");
        // Section is no longer directly set on Etudiant, so we skip setSection()
        etudiantRepository.save(etudiant);

        String storedHash = etudiantRepository.findByEmail("bob.martin@ecole.be")
                .orElseThrow()
                .getPassword();

        assertTrue(encoder.matches("Pass1234", storedHash));
    }



    @Test
    void testRegisterEtudiantInvalidEmailFormat() throws Exception {
        Section section = new Section("Cyber", 30);
        sectionRepository.save(section);

        mockMvc.perform(
                        post("/auth/register")
                                .param("nom", "Bob")
                                .param("prenom", "Martin")
                                .param("email", "bobmartin@gmail.com") // Wrong format
                                .param("password", "Pass1234")
                                .param("sectionId", section.getId().toString())
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(Matchers.containsString("Le format de l&#39;email doit être prenom.nom@ecole.be"))); // 🔥 escaped apostrophe
    }




    @Test
    void testRegisterEtudiantNoPlacesAvailable() throws Exception {
        // Arrange
        Section section = new Section("Réseaux", 1);
        sectionRepository.save(section);

        AnneeSection anneeSection = new AnneeSection();
        anneeSection.setAnneeAcademique("2024-2025");
        anneeSection.setSection(section);
        anneeSectionRepository.save(anneeSection);

        Etudiant existingEtudiant = new Etudiant();
        existingEtudiant.setNom("John");
        existingEtudiant.setPrenom("Doe");
        existingEtudiant.setEmail("john.doe@ecole.be");
        existingEtudiant.setPassword(passwordEncoder.encode("password"));
        existingEtudiant.setRole("ROLE_ETUDIANT");
        existingEtudiant.setMatricule("E-10001");
        etudiantRepository.save(existingEtudiant);

        Inscription inscription = new Inscription();
        inscription.setEtudiant(existingEtudiant);
        inscription.setAnneeSection(anneeSection);
        inscriptionRepository.save(inscription);

        // Act
        mockMvc.perform(
                        post("/auth/register")
                                .param("nom", "Jane")
                                .param("prenom", "Doe")
                                .param("email", "jane.doe@ecole.be")
                                .param("password", "password")
                                .param("sectionId", section.getId().toString())
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(Matchers.containsString("Plus de places disponibles dans cette section")));
    }


    @Test
    void testPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Pass1234";
        String encodedPassword = encoder.encode(rawPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }



    @Test
    void showSchema() {
        jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PROFESSEUR'",
                (rs) -> {
                    System.out.println("==== PROFESSEUR COLUMNS ====");
                    while (rs.next()) {
                        System.out.println(rs.getString("COLUMN_NAME"));
                    }
                    System.out.println("============================");
                }
        );
    }

}

