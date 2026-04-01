package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Implémentation JDBC du DAO pour les membres de la communauté
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    // Construit un objet CommunityMember à partir d'une ligne du ResultSet
    private CommunityMember lireMembre(ResultSet rs) throws SQLException {
        CommunityMember membre = new CommunityMember();
        membre.setName(rs.getString("name"));
        membre.setEmail(rs.getString("email"));
        membre.setBirthYear(rs.getObject("birth_year") != null ? rs.getInt("birth_year") : null);
        membre.setPhone(rs.getString("phone"));
        membre.setCity(rs.getString("city"));
        membre.setMembershipType(rs.getString("membership_type"));
        return membre;
    }

    // Charge les disciplines favorites d'un membre depuis la base
    private List<Discipline> chargerDisciplinesFavorites(Connection conn, int membreId)
            throws SQLException {
        List<Discipline> disciplines = new ArrayList<Discipline>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT d.name FROM disciplines d "
                       + "JOIN member_disciplines md ON d.id = md.discipline_id "
                       + "WHERE md.member_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, membreId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                disciplines.add(new Discipline(rs.getString("name")));
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }

        return disciplines;
    }

    @Override
    public Optional<CommunityMember> findById(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id, name, email, birth_year, phone, city, membership_type "
                       + "FROM community_members WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            // Retourne le membre s'il existe
            if (rs.next()) {
                CommunityMember membre = lireMembre(rs);
                int membreId = rs.getInt("id");
                // Chargement des disciplines favorites
                List<Discipline> disciplines = chargerDisciplinesFavorites(conn, membreId);
                membre.setFavoriteDisciplines(disciplines);
                return Optional.of(membre);
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("Erreur findById membre : " + e.getMessage());
            return Optional.empty();
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> membres = new ArrayList<CommunityMember>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id, name, email, birth_year, phone, city, membership_type "
                       + "FROM community_members";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                CommunityMember membre = lireMembre(rs);
                int membreId = rs.getInt("id");
                // Chargement des disciplines favorites de ce membre
                List<Discipline> disciplines = chargerDisciplinesFavorites(conn, membreId);
                membre.setFavoriteDisciplines(disciplines);
                membres.add(membre);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll membres : " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }

        return membres;
    }
}
