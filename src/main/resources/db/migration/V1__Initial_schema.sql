-- ================================
-- V1__Initial_schema.sql
-- PostgreSQL schema for Gestion Ecole
-- ================================

-- Table des utilisateurs (étudiants et professeurs)
CREATE TABLE utilisateur (
                             id BIGSERIAL PRIMARY KEY,
                             nom VARCHAR(255) NOT NULL,
                             prenom VARCHAR(255) NOT NULL,
                             email VARCHAR(255) UNIQUE NOT NULL,
                             password VARCHAR(255) NOT NULL,
                             role VARCHAR(255) NOT NULL,
                             matricule VARCHAR(255) -- Identifiant interne (étudiant/professeur)
);

-- Table des sections (ex: Télécom, Cyber...)
CREATE TABLE section (
                         id BIGSERIAL PRIMARY KEY,
                         nom VARCHAR(255) NOT NULL,
                         nb_places INT NOT NULL
);

-- Table des années académiques liées à une section
CREATE TABLE annee_section (
                               id BIGSERIAL PRIMARY KEY,
                               annee_academique VARCHAR(255) NOT NULL,
                               section_id BIGINT,
                               CONSTRAINT fk_annee_section_section FOREIGN KEY (section_id) REFERENCES section(id)
);

-- Table des étudiants
CREATE TABLE etudiant (
                          id BIGINT PRIMARY KEY, -- FK vers utilisateur
                          info TEXT,
                          photo TEXT,
                          CONSTRAINT fk_etudiant_utilisateur FOREIGN KEY (id) REFERENCES utilisateur (id)
);

-- Table des professeurs
CREATE TABLE professeur (
                            id BIGINT PRIMARY KEY, -- FK vers utilisateur
                            matricule VARCHAR(100) NOT NULL,
                            CONSTRAINT fk_professeur_utilisateur FOREIGN KEY (id) REFERENCES utilisateur(id)
);

-- Table des cours, liés à un professeur et à une année/section
CREATE TABLE cours (
                       id BIGSERIAL PRIMARY KEY,
                       code VARCHAR(20) NOT NULL,
                       intitule VARCHAR(255) NOT NULL,
                       description TEXT,
                       credits INT NOT NULL,
                       professeur_id BIGINT,
                       annee_section_id BIGINT,
                       CONSTRAINT fk_cours_professeur FOREIGN KEY (professeur_id) REFERENCES professeur (id),
                       CONSTRAINT fk_cours_annee_section FOREIGN KEY (annee_section_id) REFERENCES annee_section (id)
);

-- Table des horaires hebdomadaires d’un cours
CREATE TABLE horaire (
                         id BIGSERIAL PRIMARY KEY,
                         cours_id BIGINT NOT NULL,
                         heure_debut TIME NOT NULL,
                         heure_fin TIME NOT NULL,
                         jour VARCHAR(50) NOT NULL,
                         CONSTRAINT fk_horaire_cours FOREIGN KEY (cours_id) REFERENCES cours(id)
);

-- Table des inscriptions (liens entre étudiants et cours via année)
CREATE TABLE inscription (
                             id BIGSERIAL PRIMARY KEY,
                             etudiant_id BIGINT,
                             annee_section_id BIGINT,
                             date_inscription TIMESTAMP,
                             CONSTRAINT fk_inscription_etudiant FOREIGN KEY (etudiant_id) REFERENCES etudiant(id),
                             CONSTRAINT fk_inscription_annee_section FOREIGN KEY (annee_section_id) REFERENCES annee_section (id)
);

-- Table des notes liées à une inscription et un cours
CREATE TABLE note (
                      id BIGSERIAL PRIMARY KEY,
                      inscription_id BIGINT NOT NULL,
                      cours_id       BIGINT NOT NULL,
                      premiere_session DOUBLE PRECISION,
                      deuxieme_session DOUBLE PRECISION,
                      CONSTRAINT uk_note_inscription_cours UNIQUE (inscription_id, cours_id),
                      CONSTRAINT fk_note_cours FOREIGN KEY (cours_id) REFERENCES cours (id),
                      CONSTRAINT fk_note_inscription FOREIGN KEY (inscription_id) REFERENCES inscription (id)
);
