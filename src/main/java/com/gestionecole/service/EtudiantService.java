package com.gestionecole.service;

import com.gestionecole.model.*;
import com.gestionecole.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final SectionRepository sectionRepository;
    private final AnneeSectionRepository anneeSectionRepository;
    private final HoraireRepository horaireRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    public EtudiantService(EtudiantRepository etudiantRepository,
                           SectionRepository sectionRepository,
                           AnneeSectionRepository anneeSectionRepository,
                           HoraireRepository horaireRepository,
                           InscriptionRepository inscriptionRepository,
                           PasswordEncoder passwordEncoder) {
        this.etudiantRepository = etudiantRepository;
        this.sectionRepository = sectionRepository;
        this.anneeSectionRepository = anneeSectionRepository;
        this.horaireRepository = horaireRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    public Optional<Etudiant> getEtudiantById(Long id) {
        return etudiantRepository.findById(id);
    }

    public Optional<Etudiant> getEtudiantByEmail(String email) {
        return etudiantRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Etudiant> getEtudiantWithSectionByEmail(String email) {
        return etudiantRepository.findWithSectionByEmail(email);
    }

    @Transactional
    public void registerStudent(Etudiant etudiant, Long sectionId) {
        // 1. Find section and check capacity
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalStateException("Section non trouvée"));

        int placesRestantes = section.getNbPlaces() - countEtudiantsInSection(sectionId);

        if (placesRestantes <= 0) {
            throw new IllegalStateException("Plus de places disponibles dans cette section.");
        }

        // 2. Encode password
        etudiant.setPassword(passwordEncoder.encode(etudiant.getPassword()));

        // 3. Assign role and generate matricule
        etudiant.setRole(Roles.ETUDIANT);
        etudiant.setMatricule(generateNextMatricule());

        // 4. Save Etudiant (before creating FK-dependent inscriptions)
        etudiantRepository.save(etudiant);

        // 5. Determine current academic year
        String academicYear = getCurrentAcademicYear();

        // 6. Fetch AnneeSection for the student’s section
        AnneeSection anneeSection = anneeSectionRepository
                .findByAnneeAcademiqueAndSection(academicYear, section)
                .orElseThrow(() -> new IllegalStateException("Année académique non trouvée pour la section " + section.getNom()));

        // 7. Create and save inscription
        Inscription inscription = new Inscription();
        inscription.setEtudiant(etudiant);
        inscription.setAnneeSection(anneeSection);
        inscription.setDateInscription(LocalDate.now());
        inscription = inscriptionRepository.save(inscription);

        // 8. Enroll student into all courses for that academic section
        List<Cours> coursList = anneeSection.getCours(); // make sure AnneeSection has getCours() mapped correctly
        for (Cours cours : coursList) {
            Note note = new Note();
            note.setInscription(inscription);
            note.setCours(cours);
            note.setPremiereSession(null);
            note.setDeuxiemeSession(null);
            // Save notes via cascade from Inscription or via a dedicated repository if needed
            // Assuming cascade = CascadeType.ALL is set
            // Otherwise: noteRepository.save(note);
        }
    }


    private String getCurrentAcademicYear() {
        LocalDate today = LocalDate.now();
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        return year + "-" + (year + 1);
    }

    private String generateNextMatricule() {
        long count = etudiantRepository.count() + 1;
        return String.format("E-%05d", count);
    }


    public int countEtudiantsInSection(Long sectionId) {
        return inscriptionRepository.countByAnneeSection_Section_Id(sectionId);
    }

}
