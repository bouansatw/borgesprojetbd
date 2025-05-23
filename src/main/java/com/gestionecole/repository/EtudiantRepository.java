package com.gestionecole.repository;

import com.gestionecole.model.Etudiant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    Optional<Etudiant> findByEmail(String email);
    @EntityGraph(attributePaths = "section")
    Optional<Etudiant> findWithSectionByEmail(@Param("email") String email);
}
