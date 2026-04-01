-- ============================================================
-- ArtConnect - Données de test réalistes
-- Projet BBD2 T1603
-- ============================================================

USE ArtConnect;

-- Désactiver les vérifications de clés étrangères pendant l'insertion
SET FOREIGN_KEY_CHECKS = 0;

-- Vider les tables dans l'ordre inverse des dépendances
TRUNCATE TABLE audit_artworks;
TRUNCATE TABLE reviews;
TRUNCATE TABLE bookings;
TRUNCATE TABLE member_disciplines;
TRUNCATE TABLE exhibition_artworks;
TRUNCATE TABLE artist_disciplines;
TRUNCATE TABLE artwork_artwork_tags;
TRUNCATE TABLE artwork_tags;
TRUNCATE TABLE artworks;
TRUNCATE TABLE exhibitions;
TRUNCATE TABLE workshops;
TRUNCATE TABLE community_members;
TRUNCATE TABLE galleries;
TRUNCATE TABLE artist_disciplines;
TRUNCATE TABLE artists;
TRUNCATE TABLE disciplines;

-- Réactiver les vérifications
SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================
-- Disciplines artistiques
-- ============================================================
INSERT INTO disciplines (id, name) VALUES
    (1, 'Peinture'),
    (2, 'Sculpture'),
    (3, 'Photographie'),
    (4, 'Art Numérique'),
    (5, 'Musique');


-- ============================================================
-- Artistes
-- ============================================================
INSERT INTO artists (id, name, bio, birth_year, contact_email, phone, city, website, is_active) VALUES
    (1, 'Leonardo Vinci',  'Peintre et sculpteur de la Renaissance, maître du sfumato.',                1452, 'leonardo@artconnect.com',  '0600000001', 'Paris',     'www.leonardovinci.art', 1),
    (2, 'Claude Monet',    'Fondateur du mouvement impressionniste, célèbre pour ses Nymphéas.',        1840, 'monet@artconnect.com',      '0600000002', 'Giverny',   'www.claudemonet.art',   1),
    (3, 'Ansel Adams',     'Photographe américain connu pour ses paysages en noir et blanc.',           1902, 'ansel@artconnect.com',      '0600000003', 'Lyon',      'www.anseladams.art',    1),
    (4, 'Frida Kahlo',     'Peintre mexicaine reconnue pour ses autoportraits et son style unique.',    1907, 'frida@artconnect.com',      '0600000004', 'Marseille', 'www.fridakahlo.art',    1),
    (5, 'Auguste Rodin',   'Sculpteur français, considéré comme le père de la sculpture moderne.',     1840, 'rodin@artconnect.com',      '0600000005', 'Paris',     'www.augusterodin.art',  1);


-- ============================================================
-- Disciplines des artistes (N:N)
-- ============================================================
INSERT INTO artist_disciplines (artist_id, discipline_id) VALUES
    (1, 1), -- Leonardo : Peinture
    (1, 2), -- Leonardo : Sculpture
    (2, 1), -- Monet : Peinture
    (3, 3), -- Ansel : Photographie
    (4, 1), -- Frida : Peinture
    (5, 2); -- Rodin : Sculpture


-- ============================================================
-- Tags d'oeuvres
-- ============================================================
INSERT INTO artwork_tags (id, name) VALUES
    (1, 'Renaissance'),
    (2, 'Impressionnisme'),
    (3, 'Portrait'),
    (4, 'Paysage'),
    (5, 'Abstrait'),
    (6, 'Noir et blanc');


-- ============================================================
-- Oeuvres
-- ============================================================
INSERT INTO artworks (id, title, creation_year, type, medium, dimensions, description, price, status, artist_id) VALUES
    (1, 'La Joconde',           1503, 'Peinture',     'Huile sur bois',    '77x53 cm',   'Le sourire énigmatique le plus célèbre au monde.',           150000.00, 'EXHIBITED', 1),
    (2, 'La Cène',              1495, 'Peinture',     'Tempera sur plâtre','460x880 cm', 'La dernière cène de Jésus avec ses apôtres.',                200000.00, 'EXHIBITED', 1),
    (3, 'Nymphéas',             1906, 'Peinture',     'Huile sur toile',   '200x200 cm', 'Les célèbres nénuphars du jardin de Giverny.',               180000.00, 'FOR_SALE',  2),
    (4, 'Le Penseur',           1902, 'Sculpture',    'Bronze',            '180x98 cm',  'Figure humaine en méditation profonde.',                      90000.00, 'EXHIBITED', 5),
    (5, 'Les Deux Fridas',      1939, 'Peinture',     'Huile sur toile',   '174x173 cm', 'Double autoportrait symbolisant l identité mexicaine.',      120000.00, 'FOR_SALE',  4),
    (6, 'Le Monolithe',         1941, 'Photographie', 'Tirage argentique', '50x60 cm',   'Photographie en noir et blanc de la Sierra Nevada.',          15000.00, 'FOR_SALE',  3);


-- ============================================================
-- Tags associés aux oeuvres (N:N)
-- ============================================================
INSERT INTO artwork_artwork_tags (artwork_id, tag_id) VALUES
    (1, 1), -- La Joconde : Renaissance
    (1, 3), -- La Joconde : Portrait
    (2, 1), -- La Cène : Renaissance
    (3, 2), -- Nymphéas : Impressionnisme
    (3, 4), -- Nymphéas : Paysage
    (4, 3), -- Le Penseur : Portrait
    (5, 3), -- Les Deux Fridas : Portrait
    (5, 5), -- Les Deux Fridas : Abstrait
    (6, 4), -- Le Monolithe : Paysage
    (6, 6); -- Le Monolithe : Noir et blanc


-- ============================================================
-- Galeries
-- ============================================================
INSERT INTO galleries (id, name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
    (1, 'Louvre Art House',     '75001 Paris, Rue de Rivoli',    'Marie Dupont', 'Lu-Di 9h-18h', '0145678901', 4.80, 'www.louvrearthouse.fr'),
    (2, 'The British Gallery',  '10 Baker Street, Londres',      'John Smith',   'Ma-Sa 10h-17h','0207654321', 4.50, 'www.britishgallery.co.uk'),
    (3, 'Metropolitan Hub',     '1000 5th Ave, New York',        'Amy Johnson',  'Ma-Di 10h-20h','2125357710', 4.90, 'www.metrohub.com');


-- ============================================================
-- Expositions
-- ============================================================
INSERT INTO exhibitions (id, title, start_date, end_date, description, gallery_id, curator_name, theme) VALUES
    (1, 'Renaissance Revival',    '2025-03-01', '2025-06-30', 'Une plongée dans les chefs-d oeuvre de la Renaissance.',           1, 'Sophie Martin',   'Renaissance'),
    (2, 'Sculpting the Soul',     '2025-04-15', '2025-08-15', 'Les grandes sculptures de la modernité réunies.',                   2, 'James Brown',     'Sculpture Moderne'),
    (3, 'Impressionist Dreams',   '2025-05-01', '2025-09-01', 'L impressionnisme français à travers ses plus grandes toiles.',     3, 'Claire Fontaine', 'Impressionnisme');


-- ============================================================
-- Oeuvres exposées (N:N)
-- ============================================================
INSERT INTO exhibition_artworks (exhibition_id, artwork_id) VALUES
    (1, 1), -- Renaissance Revival : La Joconde
    (1, 2), -- Renaissance Revival : La Cène
    (2, 4), -- Sculpting the Soul : Le Penseur
    (3, 3); -- Impressionist Dreams : Nymphéas


-- ============================================================
-- Ateliers
-- ============================================================
INSERT INTO workshops (id, title, date, duration_minutes, max_participants, price, instructor_id, location, description, level) VALUES
    (1, 'Maîtriser la peinture à l huile', '2025-07-10 10:00:00', 180, 3, 80.00,  2, 'Paris, Atelier Monet',    'Apprendre les bases de la peinture à l huile.',         'Débutant'),
    (2, 'Paysages impressionnistes',       '2025-07-25 14:00:00', 120, 5, 60.00,  2, 'Giverny, Studio Nature', 'Peindre des paysages avec la technique impressionniste.', 'Intermédiaire'),
    (3, 'Sculpture en argile',             '2025-08-05 09:00:00', 240, 4, 120.00, 5, 'Paris, Atelier Rodin',    'Introduction à la sculpture sur argile.',                'Débutant');


-- ============================================================
-- Membres de la communauté
-- ============================================================
INSERT INTO community_members (id, name, email, birth_year, phone, city, membership_type) VALUES
    (1, 'Alice Wonderland', 'alice@mail.com',   1990, '0611111111', 'Paris',    'premium'),
    (2, 'Bob Ross',         'bob@mail.com',     1985, '0622222222', 'Lyon',     'free'),
    (3, 'Charlie Brown',    'charlie@mail.com', 1995, '0633333333', 'Bordeaux', 'free'),
    (4, 'Diana Prince',     'diana@mail.com',   1988, '0644444444', 'Paris',    'premium'),
    (5, 'Ethan Hunt',       'ethan@mail.com',   1992, '0655555555', 'Marseille','free');


-- ============================================================
-- Disciplines favorites des membres (N:N)
-- ============================================================
INSERT INTO member_disciplines (member_id, discipline_id) VALUES
    (1, 1), -- Alice : Peinture
    (1, 3), -- Alice : Photographie
    (2, 1), -- Bob : Peinture
    (3, 2), -- Charlie : Sculpture
    (4, 3), -- Diana : Photographie
    (5, 4); -- Ethan : Art Numérique


-- ============================================================
-- Réservations d'ateliers
-- ============================================================
INSERT INTO bookings (id, workshop_id, member_id, booking_date, payment_status) VALUES
    (1, 1, 1, '2025-06-01 10:00:00', 'PAID'),     -- Alice s inscrit atelier peinture
    (2, 1, 2, '2025-06-02 11:00:00', 'PAID'),     -- Bob s inscrit atelier peinture
    (3, 2, 1, '2025-06-05 09:00:00', 'PENDING'),  -- Alice s inscrit atelier paysages
    (4, 3, 3, '2025-06-10 14:00:00', 'PAID'),     -- Charlie s inscrit atelier sculpture
    (5, 3, 4, '2025-06-11 15:00:00', 'PENDING');  -- Diana s inscrit atelier sculpture


-- ============================================================
-- Avis sur les oeuvres
-- ============================================================
INSERT INTO reviews (id, reviewer_id, artwork_id, rating, comment, review_date) VALUES
    (1, 1, 1, 5, 'Un chef d oeuvre absolu, le regard de la Joconde est hypnotisant.',   '2025-05-15'),
    (2, 2, 3, 4, 'Les Nymphéas sont magnifiques, la lumière est parfaitement rendue.',  '2025-05-20'),
    (3, 3, 4, 5, 'Le Penseur dégage une puissance incroyable, la sculpture est parfaite.', '2025-05-22'),
    (4, 4, 5, 4, 'Les Deux Fridas est une oeuvre très émouvante et symbolique.',        '2025-05-25'),
    (5, 5, 6, 5, 'La photographie du Monolithe est saisissante, le noir et blanc est magnifique.', '2025-05-28');
