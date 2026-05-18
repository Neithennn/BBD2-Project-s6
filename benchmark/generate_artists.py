"""
generate_artists.py
Génère N artistes fictifs dans la table artists pour le benchmark index.
Les noms et emails sont numérotés pour garantir l'unicité.
"""

import mysql.connector
import random

# ── Config ───────────────────────────────────────────────────────────────────
DB_CONFIG = dict(host="localhost", user="root", password="",
                 database="ArtConnect")

VILLES = [
    "Paris", "Lyon", "Marseille", "Bordeaux", "Nantes", "Lille", "Toulouse",
    "Strasbourg", "Rennes", "Nice", "Grenoble", "Montpellier", "Brest",
    "Dijon", "Angers", "Nimes", "Clermont-Ferrand", "Reims", "Le Havre",
    "Metz", "Perpignan", "Tours", "Amiens", "Limoges", "Caen", "Orléans",
    "Mulhouse", "Rouen", "Nancy", "Avignon", "Poitiers", "La Rochelle",
    "Besancon", "Valenciennes", "Pau", "Bayonne", "Troyes", "Annecy",
    "Lorient", "La Seyne-sur-Mer", "Quimper", "Bourges", "Villeurbanne",
    "Boulogne-sur-Mer", "Saint-Nazaire", "Cannes", "Antibes", "Saint-Etienne",
    "Toulon", "Dunkerque", "Chambery", "Agen", "Laval", "Le Mans",
    "Calais", "Ajaccio", "Bastia", "Blois", "Chartres", "Colmar",
    "Compiegne", "Draguignan", "Epinal", "Frejus", "Gap", "Hyeres",
    "Istres", "Laon", "Lons-le-Saunier", "Macon", "Meaux", "Melun",
    "Montauban", "Montbeliard", "Moulins", "Niort", "Perigueux", "Roanne",
    "Rodez", "Saint-Brieuc", "Saint-Malo", "Saint-Omer", "Saintes",
    "Sens", "Soissons", "Tarbes", "Thionville", "Valence", "Vannes",
    "Verdun", "Versailles", "Vichy", "Vienne", "Vitry-le-Francois",
    "Auxerre", "Belfort", "Chateauroux", "Cherbourg", "Evreux",
    "Fontainebleau", "Gueret", "Lure", "Mende", "Montlucon",
]

N = 50_000   # nombre d'artistes à insérer
PREFIX = "bench_"   # préfixe pour retrouver / supprimer facilement ces lignes

# ── Insertion ─────────────────────────────────────────────────────────────────
def main():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # Supprimer les anciens artistes de benchmark si on relance le script
    cursor.execute("DELETE FROM artists WHERE name LIKE %s", (PREFIX + "%",))
    conn.commit()
    print("[OK] Anciens artistes benchmark supprimes.")

    rows = []
    for i in range(1, N + 1):
        rows.append((
            f"{PREFIX}{i}",                   # name  (unique)
            f"Bio artiste {i}.",              # bio
            random.randint(1950, 2000),        # birth_year
            f"bench{i}@artconnect.test",       # contact_email (unique)
            f"+33600{i:06d}",                  # phone
            random.choice(VILLES),             # city  <- colonne indexee
            f"https://bench{i}.art",           # website
            None,                              # social_media
            1,                                 # is_active
        ))

    sql = """
        INSERT INTO artists
            (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    BATCH = 500
    for i in range(0, len(rows), BATCH):
        cursor.executemany(sql, rows[i:i+BATCH])
        conn.commit()
        print(f"  {min(i+BATCH, N)}/{N} inseres...")
    print(f"[OK] {N} artistes inseres.")

    cursor.close()
    conn.close()


if __name__ == "__main__":
    main()
