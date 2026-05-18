"""
benchmark_index.py
Compare le temps de requête SELECT sur artists.city
avec et sans l'index idx_artists_city.
"""

import mysql.connector
import time

# ── Config ───────────────────────────────────────────────────────────────────
DB_CONFIG = dict(host="localhost", user="root", password="",
                 database="ArtConnect")

VILLE_CIBLE = "Paris"
NB_ITERATIONS = 50   # moyenne sur N requêtes pour être précis


# ── Helpers ──────────────────────────────────────────────────────────────────
def drop_index(cursor):
    try:
        cursor.execute("DROP INDEX idx_artists_city ON artists")
        print("  [OK] Index supprime")
    except mysql.connector.Error:
        print("  [i] Index n'existait pas (rien a supprimer)")


def create_index(cursor):
    cursor.execute("CREATE INDEX idx_artists_city ON artists(city)")
    print("  [OK] Index cree")


def flush(cursor):
    """Vide le cache InnoDB pour forcer la lecture depuis le disque."""
    cursor.execute("FLUSH TABLES")

def mesurer(cursor, conn2, label, n=NB_ITERATIONS):
    """Retourne le temps moyen en secondes sur n requetes (cache flushe avant chaque mesure)."""
    durees = []
    c2 = conn2.cursor()
    for _ in range(n):
        flush(c2)
        conn2.commit()
        start = time.perf_counter()
        cursor.execute("SELECT SQL_NO_CACHE * FROM artists WHERE city = %s", (VILLE_CIBLE,))
        cursor.fetchall()
        durees.append(time.perf_counter() - start)
    c2.close()
    moyenne = sum(durees) / len(durees)
    print(f"  [{label}] moyenne sur {n} requetes : {moyenne*1000:.3f} ms")
    return moyenne


# ── Main ─────────────────────────────────────────────────────────────────────
def main():
    conn  = mysql.connector.connect(**DB_CONFIG)
    conn2 = mysql.connector.connect(**DB_CONFIG)  # connexion dédiée au flush
    cursor = conn.cursor()

    # Vérif du nombre de lignes
    cursor.execute("SELECT COUNT(*) FROM artists")
    total = cursor.fetchone()[0]
    print(f"\n[i] Table artists : {total} lignes - ville cible : '{VILLE_CIBLE}'\n")

    # ── 1. SANS index ─────────────────────────────────────────────────────────
    print("-- SANS index --")
    drop_index(cursor)
    conn.commit()

    cursor.execute("EXPLAIN SELECT * FROM artists WHERE city = %s", (VILLE_CIBLE,))
    row = cursor.fetchone()
    print(f"  EXPLAIN -> type={row[3]}, rows_estimes={row[8]}, Extra={row[9]}")

    t_sans = mesurer(cursor, conn2, "sans index")

    print("\n-- AVEC index --")
    create_index(cursor)
    conn.commit()

    cursor.execute("EXPLAIN SELECT * FROM artists WHERE city = %s", (VILLE_CIBLE,))
    row = cursor.fetchone()
    print(f"  EXPLAIN -> type={row[3]}, rows_estimes={row[8]}, Extra={row[9]}")

    t_avec = mesurer(cursor, conn2, "avec index")

    ratio = t_sans / t_avec if t_avec > 0 else float("inf")
    gain_pct = (1 - t_avec / t_sans) * 100 if t_sans > 0 else 0

    print("\n================================================")
    print(f"  Sans index : {t_sans*1000:.3f} ms  (type: ALL = full scan)")
    print(f"  Avec index : {t_avec*1000:.3f} ms  (type: ref = index lookup)")
    print(f"  Gain       : {ratio:.1f}x plus rapide ({gain_pct:.0f}% de reduction)")
    print("================================================\n")

    cursor.close()
    conn.close()
    conn2.close()


if __name__ == "__main__":
    main()
