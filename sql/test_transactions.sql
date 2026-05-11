-- ============================================================
-- ArtConnect - Tests des transactions
-- Projet BBD2 T1603
--
-- Ce script montre 3 scenarios transactionnels :
--   1) Inscription reussie d'un membre a plusieurs ateliers (COMMIT)
--   2) Inscription qui echoue (capacite atteinte) -> ROLLBACK automatique
--   3) Transfert manuel (COMMIT ou ROLLBACK explicite)
--
-- Pour executer : source sql/test_transactions.sql
-- ============================================================

USE ArtConnect;

-- Affiche l'etat de depart
SELECT '--- Etat initial ---' AS Info;
SELECT w.id, w.title, w.max_participants,
       (SELECT COUNT(*) FROM bookings b WHERE b.workshop_id = w.id) AS nb_reservations
FROM workshops w
ORDER BY w.id;


-- ============================================================
-- SCENARIO 1 : inscription atomique reussie
-- Un membre s'inscrit a plusieurs ateliers en une seule unite.
-- Toutes les inscriptions sont valides, on fait COMMIT.
-- ============================================================
SELECT '--- Scenario 1 : inscriptions multiples reussies ---' AS Info;

START TRANSACTION;

-- Inscription du membre 5 (Ethan) a l'atelier 2 (capacite 5, 1 reservation existante)
CALL sp_inscrire_membre(5, 2);

-- Inscription du membre 5 a l'atelier 3 (capacite 4, 2 reservations existantes)
CALL sp_inscrire_membre(5, 3);

-- Les 2 inscriptions ont reussi, on valide
COMMIT;

SELECT 'Scenario 1 : OK, 2 inscriptions commitees' AS Resultat;
SELECT * FROM bookings WHERE member_id = 5;


-- ============================================================
-- SCENARIO 2 : inscription qui doit echouer
-- On tente d'inscrire un membre deja inscrit -> SIGNAL leve par la procedure
-- Le bloc d'erreur (HANDLER) fait un ROLLBACK explicite
-- ============================================================
SELECT '--- Scenario 2 : inscription en doublon ---' AS Info;

-- On utilise une procedure de test pour capturer l'erreur dans MySQL client
DROP PROCEDURE IF EXISTS test_scenario_2;

DELIMITER $$
CREATE PROCEDURE test_scenario_2()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'Scenario 2 : erreur captee, ROLLBACK effectue' AS Resultat;
    END;

    START TRANSACTION;

    -- Cette inscription reussit
    CALL sp_inscrire_membre(3, 2);

    -- Celle-ci doit echouer : le membre 1 est deja inscrit a l'atelier 1
    CALL sp_inscrire_membre(1, 1);

    -- Si jamais on arrive ici, on valide (mais on ne devrait pas)
    COMMIT;
    SELECT 'Scenario 2 : COMMIT (non attendu)' AS Resultat;
END$$
DELIMITER ;



CALL test_scenario_2();

-- La premiere inscription (membre 3 / atelier 2) ne doit PAS etre presente
-- car le ROLLBACK a tout annule
SELECT 'Verification : pas d inscription du membre 3 a l atelier 2' AS Info;
SELECT COUNT(*) AS nb_lignes
FROM bookings
WHERE member_id = 3 AND workshop_id = 2;

DROP PROCEDURE IF EXISTS test_scenario_2;


-- ============================================================
-- SCENARIO 3 : atelier plein (trigger trg_verif_capacite_workshop)
-- On remplit l'atelier 1 (capacite 3) puis on tente une 4e inscription
-- Le trigger doit bloquer -> ROLLBACK
-- ============================================================
SELECT '--- Scenario 3 : capacite atteinte ---' AS Info;

DROP PROCEDURE IF EXISTS test_scenario_3;

DELIMITER $$
CREATE PROCEDURE test_scenario_3()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'Scenario 3 : capacite atteinte, ROLLBACK effectue' AS Resultat;
    END;

    START TRANSACTION;

    -- L atelier 1 a deja 2 inscrits (Alice et Bob), capacite 3
    -- On inscrit le membre 4 (Diana) : ca passe, on est a 3/3
    CALL sp_inscrire_membre(4, 1);

    -- On tente le membre 5 (Ethan) : trigger leve -> exception
    CALL sp_inscrire_membre(5, 1);

    COMMIT;
    SELECT 'Scenario 3 : COMMIT (non attendu)' AS Resultat;
END$$
DELIMITER ;

CALL test_scenario_3();

-- Verification : Diana non plus ne doit pas etre inscrite (rollback)
SELECT 'Verification : pas d inscription du membre 4 a l atelier 1' AS Info;
SELECT COUNT(*) AS nb_lignes
FROM bookings
WHERE member_id = 4 AND workshop_id = 1;

DROP PROCEDURE IF EXISTS test_scenario_3;


-- ============================================================
-- Etat final
-- ============================================================
SELECT '--- Etat final ---' AS Info;
SELECT w.id, w.title, w.max_participants,
       (SELECT COUNT(*) FROM bookings b WHERE b.workshop_id = w.id) AS nb_reservations
FROM workshops w
ORDER BY w.id;
