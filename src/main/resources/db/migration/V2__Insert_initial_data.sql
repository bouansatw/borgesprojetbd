-- ================================
-- V2__Insert_initial_data.sql
-- Insert initial data for Gestion Ecole
-- ================================

-- Insert Sections
INSERT INTO section (nom, nb_places) VALUES
                                         ('Télécom', 30),
                                         ('Cyber', 25),
                                         ('Électronique', 20);

-- Insert Academic Years (linked to sections)
INSERT INTO annee_section (annee_academique, section_id) VALUES
                                                             ('2024-2025', 1),
                                                             ('2024-2025', 2),
                                                             ('2024-2025', 3);

-- Insert Utilisateurs (Etudiants)
INSERT INTO utilisateur (id, nom, prenom, email, password, role, matricule)
VALUES (1, 'Dupont', 'Alice', 'alice.dupont@ecole.be', '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa',
        'ROLE_ETUDIANT', 'E-10001'),
       (2, 'Martin', 'Bob', 'bob.martin@ecole.be', '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa',
        'ROLE_ETUDIANT', 'E-10002'),
       (3, 'Petit', 'Charlie', 'charlie.petit@ecole.be', '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa',
        'ROLE_ETUDIANT', 'E-10003');

-- Insert Utilisateurs (Professeurs)
INSERT INTO utilisateur (id, nom, prenom, email, password, role, matricule)
VALUES (4, 'Bernair', 'Michel', 'michel.bernair@ecole.be',
        '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa', 'ROLE_PROFESSEUR', 'P-20001'),
       (5, 'Hecquet', 'Jean-Paul', 'jean-paul.hecquet@ecole.be',
        '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa', 'ROLE_PROFESSEUR', 'P-20002'),
       (6, 'Jaghou', 'Ali', 'ali.jaghou@ecole.be', '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa',
        'ROLE_PROFESSEUR', 'P-20003'),
       (7, 'Lemaire', 'David', 'david.lemaire@ecole.be', '$2a$12$0aPcHqsZIIRMm0ZkuYe96e2gMLmW1V6zTqFUqKNLUiypSlAWxgMQa',
        'ROLE_PROFESSEUR', 'P-20004');

-- Insert Etudiants
INSERT INTO etudiant (id, info, photo)
VALUES (1, NULL, NULL),
       (2, NULL, NULL),
       (3, NULL, NULL);

-- Insert Professeurs
INSERT INTO professeur (id, matricule) VALUES
                                           (4, 'P-20001'),
                                           (5, 'P-20002'),
                                           (6, 'P-20003'),
                                           (7, 'P-20004');

-- Insert Cours (associated to Michel Bernair = id 4)
INSERT INTO cours (code, intitule, description, credits, professeur_id, annee_section_id)
VALUES ('NET101', 'Fundamentals of Networking', 'Introduction to computer networks, OSI model, TCP/IP basics.', 5, 4,
        1),
       ('NET201', 'Routing and Switching', 'Deep dive into routing protocols, VLANs, switching technologies.', 5, 4, 1),
       ('NET301', 'Network Security', 'Principles of securing networks, firewalls, VPNs, and intrusion detection.', 5,
        4, 1),
       ('NET401', 'Wireless Networking', '802.11 standards, Wi-Fi configuration, security and troubleshooting.', 5, 4,
        1),
       ('NET501', 'Cloud Networking', 'Networking in cloud environments: AWS, Azure, hybrid networking.', 5, 4, 1);

-- Insert Cours for Cyber section (annee_section_id = 2)
INSERT INTO cours (code, intitule, description, credits, professeur_id, annee_section_id)
VALUES ('CYB101', 'Intro to Cybersecurity', 'Cyber threat landscape, CIA triad, basic risk management.', 5, 5, 2),
       ('CYB201', 'Cryptography Basics', 'Symmetric/asymmetric encryption, hashing, PKI.', 5, 5, 2),
       ('CYB301', 'Ethical Hacking', 'Pentesting techniques, Kali Linux tools, vulnerabilities.', 5, 6, 2);

-- Insert Cours for Electronique section (annee_section_id = 3)
INSERT INTO cours (code, intitule, description, credits, professeur_id, annee_section_id)
VALUES ('ELEC101', 'Circuit Analysis', 'Ohm’s law, Kirchhoff’s laws, circuit theorems.', 5, 6, 3),
       ('ELEC201', 'Digital Electronics', 'Logic gates, flip-flops, microcontroller basics.', 5, 7, 3);



-- Insert Horaire (for cours 1 to 5)
INSERT INTO horaire (cours_id, heure_debut, heure_fin, jour)
VALUES (1, '18:00', '21:30', 'LUNDI'),
       (2, '18:00', '21:30', 'MARDI'),
       (3, '18:00', '21:30', 'MERCREDI'),
       (4, '18:00', '21:30', 'JEUDI'),
       (5, '18:00', '21:30', 'VENDREDI');

-- Insert Inscriptions
INSERT INTO inscription (etudiant_id, annee_section_id, date_inscription)
VALUES (1, 1, now()),
       (2, 1, now()),
       (3, 1, now());

-- Insert Notes
INSERT INTO note (inscription_id, cours_id, premiere_session, deuxieme_session)
VALUES (1, 1, NULL, NULL),
       (1, 2, NULL, NULL),
       (1, 3, NULL, NULL),
       (2, 1, NULL, NULL),
       (2, 2, NULL, NULL),
       (2, 3, NULL, NULL),
       (2, 4, NULL, NULL),
       (2, 5, NULL, NULL),
       (3, 4, NULL, NULL),
       (3, 5, NULL, NULL);

-- Reset only the utilisateur ID sequence
SELECT setval('utilisateur_id_seq', (SELECT MAX(id) FROM utilisateur));
