-- ============================================================
-- ArtConnect - Fonctionnalités SQL Avancées
-- Projet BBD2 T1603
-- Triggers, Procédures stockées, Vues, Index
-- ============================================================

USE ArtConnect;

-- ============================================================
-- INDEX
-- Les index accélèrent les recherches fréquentes
-- ============================================================

-- Index sur artist_id dans artworks (recherche par artiste)
CREATE INDEX idx_artworks_artist_id ON artworks(artist_id);

-- Index sur member_id dans bookings (réservations d'un membre)
CREATE INDEX idx_bookings_member_id ON bookings(member_id);

-- Index sur city dans artists (recherche par ville)
CREATE INDEX idx_artists_city ON artists(city);


-- ============================================================
-- VUES
-- Simplifient les requêtes en pré-joignant les tables
-- ============================================================

-- Vue : oeuvres avec le nom de leur artiste
CREATE OR REPLACE VIEW v_oeuvres_avec_artistes AS
    SELECT
        a.id          AS artwork_id,
        a.title       AS titre,
        a.type        AS type_oeuvre,
        a.status      AS statut,
        a.price       AS prix,
        ar.id         AS artist_id,
        ar.name       AS nom_artiste,
        ar.city       AS ville_artiste
    FROM artworks a
    JOIN artists ar ON a.artist_id = ar.id;

-- Vue : expositions avec le nom de leur galerie
CREATE OR REPLACE VIEW v_expositions_avec_galerie AS
    SELECT
        e.id           AS exposition_id,
        e.title        AS titre_expo,
        e.start_date   AS date_debut,
        e.end_date     AS date_fin,
        e.theme        AS theme,
        g.id           AS gallery_id,
        g.name         AS nom_galerie,
        g.address      AS adresse_galerie
    FROM exhibitions e
    JOIN galleries g ON e.gallery_id = g.id;

-- Vue : réservations avec les détails du membre et de l'atelier
CREATE OR REPLACE VIEW v_reservations_membres AS
    SELECT
        b.id             AS booking_id,
        b.booking_date   AS date_reservation,
        b.payment_status AS statut_paiement,
        m.id             AS member_id,
        m.name           AS nom_membre,
        m.email          AS email_membre,
        w.id             AS workshop_id,
        w.title          AS titre_atelier,
        w.date           AS date_atelier,
        w.max_participants AS places_max
    FROM bookings b
    JOIN community_members m ON b.member_id = m.id
    JOIN workshops w         ON b.workshop_id = w.id;


-- ============================================================
-- TRIGGERS
-- S'exécutent automatiquement avant/après certaines opérations
-- ============================================================

-- Suppression des triggers existants (pour ré-exécution du script)
DROP TRIGGER IF EXISTS trg_verif_dates_exposition;
DROP TRIGGER IF EXISTS trg_verif_capacite_workshop;
DROP TRIGGER IF EXISTS trg_audit_statut_oeuvre;

DELIMITER $$

-- Trigger 1 : vérifier que la date de fin est après la date de début
-- S'exécute BEFORE INSERT et BEFORE UPDATE sur exhibitions
CREATE TRIGGER trg_verif_dates_exposition
BEFORE INSERT ON exhibitions
FOR EACH ROW
BEGIN
    -- Si la date de fin est avant ou égale à la date de début, on bloque
    IF NEW.end_date <= NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Erreur : la date de fin doit être après la date de début.';
    END IF;
END$$

-- Trigger 2 : vérifier la capacité avant une réservation
-- S'exécute BEFORE INSERT sur bookings
CREATE TRIGGER trg_verif_capacite_workshop
BEFORE INSERT ON bookings
FOR EACH ROW
BEGIN
    DECLARE nb_places_prises INT;
    DECLARE capacite_max     INT;

    -- Compter les réservations existantes pour cet atelier
    SELECT COUNT(*) INTO nb_places_prises
    FROM bookings
    WHERE workshop_id = NEW.workshop_id;

    -- Récupérer la capacité maximale de l'atelier
    SELECT max_participants INTO capacite_max
    FROM workshops
    WHERE id = NEW.workshop_id;

    -- Si l'atelier est complet, on bloque l'insertion
    IF nb_places_prises >= capacite_max THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Erreur : cet atelier est complet.';
    END IF;
END$$

-- Trigger 3 : enregistrer les changements de statut des oeuvres (audit)
-- S'exécute AFTER UPDATE sur artworks
CREATE TRIGGER trg_audit_statut_oeuvre
AFTER UPDATE ON artworks
FOR EACH ROW
BEGIN
    -- On ne logue que si le statut a vraiment changé
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_artworks (artwork_id, ancien_statut, nouveau_statut)
        VALUES (OLD.id, OLD.status, NEW.status);
    END IF;
END$$

DELIMITER ;


-- ============================================================
-- PROCÉDURES STOCKÉES
-- ============================================================

-- Suppression des procédures existantes (pour ré-exécution du script)
DROP PROCEDURE IF EXISTS sp_inscrire_membre;
DROP PROCEDURE IF EXISTS sp_nb_participants;
DROP PROCEDURE IF EXISTS sp_stats_artiste;

DELIMITER $$

-- Procédure 1 : inscrire un membre à un atelier
-- Vérifie que le membre n'est pas déjà inscrit, puis crée la réservation
CREATE PROCEDURE sp_inscrire_membre(
    IN p_member_id   INT,
    IN p_workshop_id INT
)
BEGIN
    DECLARE nb_inscriptions_existantes INT;

    -- Vérifier si le membre est déjà inscrit à cet atelier
    SELECT COUNT(*) INTO nb_inscriptions_existantes
    FROM bookings
    WHERE member_id = p_member_id AND workshop_id = p_workshop_id;

    IF nb_inscriptions_existantes > 0 THEN
        -- Le membre est déjà inscrit, on lève une erreur
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Erreur : ce membre est déjà inscrit à cet atelier.';
    ELSE
        -- Créer la réservation (le trigger de capacité s'exécutera aussi)
        INSERT INTO bookings (workshop_id, member_id, payment_status)
        VALUES (p_workshop_id, p_member_id, 'PENDING');
    END IF;
END$$

-- Procédure 2 : compter le nombre de participants à un atelier
CREATE PROCEDURE sp_nb_participants(
    IN  p_workshop_id  INT,
    OUT p_nb_participants INT
)
BEGIN
    -- Compter uniquement les réservations non annulées
    SELECT COUNT(*) INTO p_nb_participants
    FROM bookings
    WHERE workshop_id = p_workshop_id
      AND payment_status != 'CANCELLED';
END$$

-- Procédure 3 : statistiques d'un artiste
-- Retourne le nombre d'oeuvres, la valeur totale et le nombre vendues
CREATE PROCEDURE sp_stats_artiste(
    IN p_artist_id INT
)
BEGIN
    SELECT
        COUNT(*)                                        AS nb_oeuvres_total,
        SUM(price)                                      AS valeur_totale,
        SUM(CASE WHEN status = 'SOLD' THEN 1 ELSE 0 END) AS nb_oeuvres_vendues,
        SUM(CASE WHEN status = 'FOR_SALE' THEN 1 ELSE 0 END) AS nb_oeuvres_en_vente
    FROM artworks
    WHERE artist_id = p_artist_id;
END$$

DELIMITER ;


-- ============================================================
-- TRANSACTION COMPLEXE
-- Inscription atomique d'un membre à plusieurs ateliers
-- Si une inscription échoue, toutes sont annulées
-- ============================================================

-- Exemple d'utilisation (décommenté pour illustration) :
/*
START TRANSACTION;

    -- Inscrire le membre 1 à l'atelier 1
    CALL sp_inscrire_membre(1, 1);

    -- Inscrire le membre 1 à l'atelier 2
    CALL sp_inscrire_membre(1, 2);

-- Si tout s'est bien passé, on valide
COMMIT;

-- En cas d'erreur, on annule tout
-- ROLLBACK;
*/
