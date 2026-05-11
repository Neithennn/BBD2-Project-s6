-- ============================================================
-- ArtConnect - Schéma de la base de données
-- Projet BBD2 T1603
-- Conception respectant la 3e Forme Normale (3FN)
-- ============================================================

CREATE DATABASE IF NOT EXISTS ArtConnect;
USE ArtConnect;

-- ============================================================
-- Table des disciplines artistiques
-- (référentiel indépendant, évite la répétition dans artists)
-- ============================================================
CREATE TABLE IF NOT EXISTS disciplines (
    id   INT          NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_discipline_name (name)
);

-- ============================================================
-- Table des artistes
-- ============================================================
CREATE TABLE IF NOT EXISTS artists (
    id            INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150) NOT NULL,
    bio           TEXT,
    birth_year    INT,
    contact_email VARCHAR(200),
    phone         VARCHAR(50),
    city          VARCHAR(100),
    website       VARCHAR(200),
    social_media  VARCHAR(200),
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_artist_name (name),
    UNIQUE KEY uq_artist_email (contact_email),
    CONSTRAINT chk_artist_birth_year CHECK (birth_year IS NULL OR birth_year BETWEEN 1000 AND 2100)
);

-- ============================================================
-- Table de jonction : artiste <-> discipline (N:N)
-- ============================================================
CREATE TABLE IF NOT EXISTS artist_disciplines (
    artist_id    INT NOT NULL,
    discipline_id INT NOT NULL,
    PRIMARY KEY (artist_id, discipline_id),
    FOREIGN KEY (artist_id)    REFERENCES artists(id)    ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES disciplines(id) ON DELETE CASCADE
);

-- ============================================================
-- Table des tags d'oeuvres
-- (référentiel indépendant pour éviter la redondance)
-- ============================================================
CREATE TABLE IF NOT EXISTS artwork_tags (
    id   INT         NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tag_name (name)
);

-- ============================================================
-- Table des oeuvres
-- artist_id : clé étrangère vers artists
-- ============================================================
CREATE TABLE IF NOT EXISTS artworks (
    id            INT             NOT NULL AUTO_INCREMENT,
    title         VARCHAR(200)    NOT NULL,
    creation_year INT,
    type          VARCHAR(100),
    medium        VARCHAR(100),
    dimensions    VARCHAR(100),
    description   TEXT,
    price         DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    status        ENUM('FOR_SALE', 'SOLD', 'EXHIBITED') NOT NULL DEFAULT 'FOR_SALE',
    artist_id     INT             NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_artwork_title (title),
    CONSTRAINT chk_artwork_creation_year CHECK (creation_year IS NULL OR creation_year BETWEEN 1000 AND 2100),
    CONSTRAINT chk_artwork_price CHECK (price >= 0),
    FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE
);

-- ============================================================
-- Table de jonction : oeuvre <-> tag (N:N)
-- ============================================================
CREATE TABLE IF NOT EXISTS artwork_artwork_tags (
    artwork_id INT NOT NULL,
    tag_id     INT NOT NULL,
    PRIMARY KEY (artwork_id, tag_id),
    FOREIGN KEY (artwork_id) REFERENCES artworks(id)     ON DELETE CASCADE,
    FOREIGN KEY (tag_id)     REFERENCES artwork_tags(id) ON DELETE CASCADE
);

-- ============================================================
-- Table des galeries
-- ============================================================
CREATE TABLE IF NOT EXISTS galleries (
    id            INT            NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150)   NOT NULL,
    address       VARCHAR(300),
    owner_name    VARCHAR(150),
    opening_hours VARCHAR(200),
    contact_phone VARCHAR(50),
    rating        DECIMAL(3, 2)  NOT NULL DEFAULT 0.00,
    website       VARCHAR(200),
    PRIMARY KEY (id),
    UNIQUE KEY uq_gallery_name (name),
    CONSTRAINT chk_gallery_rating CHECK (rating BETWEEN 0 AND 5)
);

-- ============================================================
-- Table des expositions
-- gallery_id : clé étrangère vers galleries
-- ============================================================
CREATE TABLE IF NOT EXISTS exhibitions (
    id           INT          NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200) NOT NULL,
    start_date   DATE         NOT NULL,
    end_date     DATE         NOT NULL,
    description  TEXT,
    gallery_id   INT          NOT NULL,
    curator_name VARCHAR(150),
    theme        VARCHAR(150),
    PRIMARY KEY (id),
    UNIQUE KEY uq_exhibition_title (title),
    CONSTRAINT chk_exhibition_dates CHECK (end_date > start_date),
    FOREIGN KEY (gallery_id) REFERENCES galleries(id) ON DELETE CASCADE
);

-- ============================================================
-- Table de jonction : exposition <-> oeuvre (N:N)
-- ============================================================
CREATE TABLE IF NOT EXISTS exhibition_artworks (
    exhibition_id INT NOT NULL,
    artwork_id    INT NOT NULL,
    PRIMARY KEY (exhibition_id, artwork_id),
    FOREIGN KEY (exhibition_id) REFERENCES exhibitions(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_id)    REFERENCES artworks(id)    ON DELETE CASCADE
);

-- ============================================================
-- Table des ateliers
-- instructor_id : clé étrangère vers artists (l'animateur)
-- ============================================================
CREATE TABLE IF NOT EXISTS workshops (
    id               INT            NOT NULL AUTO_INCREMENT,
    title            VARCHAR(200)   NOT NULL,
    date             DATETIME       NOT NULL,
    duration_minutes INT            NOT NULL DEFAULT 60,
    max_participants INT            NOT NULL DEFAULT 10,
    price            DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    instructor_id    INT            NOT NULL,
    location         VARCHAR(200),
    description      TEXT,
    level            VARCHAR(50),
    PRIMARY KEY (id),
    UNIQUE KEY uq_workshop_title (title),
    CONSTRAINT chk_workshop_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_workshop_capacity CHECK (max_participants > 0),
    CONSTRAINT chk_workshop_price CHECK (price >= 0),
    FOREIGN KEY (instructor_id) REFERENCES artists(id) ON DELETE CASCADE
);

-- ============================================================
-- Table des membres de la communauté
-- ============================================================
CREATE TABLE IF NOT EXISTS community_members (
    id              INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(200) NOT NULL,
    birth_year      INT,
    phone           VARCHAR(50),
    city            VARCHAR(100),
    membership_type VARCHAR(50)  NOT NULL DEFAULT 'free',
    PRIMARY KEY (id),
    UNIQUE KEY uq_member_email (email),
    CONSTRAINT chk_membership_type CHECK (membership_type IN ('free', 'premium'))
);

-- ============================================================
-- Table de jonction : membre <-> discipline favorite (N:N)
-- ============================================================
CREATE TABLE IF NOT EXISTS member_disciplines (
    member_id    INT NOT NULL,
    discipline_id INT NOT NULL,
    PRIMARY KEY (member_id, discipline_id),
    FOREIGN KEY (member_id)    REFERENCES community_members(id) ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES disciplines(id)       ON DELETE CASCADE
);

-- ============================================================
-- Table des réservations d'ateliers
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
    id             INT          NOT NULL AUTO_INCREMENT,
    workshop_id    INT          NOT NULL,
    member_id      INT          NOT NULL,
    booking_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (id),
    UNIQUE KEY uq_booking_member_workshop (workshop_id, member_id),
    CONSTRAINT chk_booking_payment_status CHECK (payment_status IN ('PENDING', 'PAID', 'CANCELLED')),
    FOREIGN KEY (workshop_id) REFERENCES workshops(id)         ON DELETE CASCADE,
    FOREIGN KEY (member_id)   REFERENCES community_members(id) ON DELETE CASCADE
);

-- ============================================================
-- Table des avis sur les oeuvres
-- ============================================================
CREATE TABLE IF NOT EXISTS reviews (
    id          INT  NOT NULL AUTO_INCREMENT,
    reviewer_id INT  NOT NULL,
    artwork_id  INT  NOT NULL,
    rating      INT  NOT NULL,
    comment     TEXT,
    review_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    PRIMARY KEY (id),
    -- Un membre ne peut laisser qu'un seul avis par oeuvre
    UNIQUE KEY uq_review (reviewer_id, artwork_id),
    -- La note doit être entre 1 et 5
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
    FOREIGN KEY (reviewer_id) REFERENCES community_members(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_id)  REFERENCES artworks(id)          ON DELETE CASCADE
);

-- ============================================================
-- Table d'audit pour les changements de statut des oeuvres
-- (utilisée par le trigger trg_audit_statut_oeuvre)
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_artworks (
    id          INT          NOT NULL AUTO_INCREMENT,
    artwork_id  INT          NOT NULL,
    ancien_statut  VARCHAR(50),
    nouveau_statut VARCHAR(50),
    date_changement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
