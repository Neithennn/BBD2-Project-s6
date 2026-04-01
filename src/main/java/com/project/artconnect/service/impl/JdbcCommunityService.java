package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service communauté branché sur la base MySQL via JDBC
public class JdbcCommunityService implements CommunityService {

    // DAO utilisé pour accéder à la table community_members
    private final JdbcCommunityMemberDao memberDao = new JdbcCommunityMemberDao();

    @Override
    public List<CommunityMember> getAllMembers() {
        // Délégation directe au DAO
        return memberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        // Parcours de la liste pour trouver le membre par son nom
        List<CommunityMember> tous = memberDao.findAll();

        for (CommunityMember m : tous) {
            if (m.getName().equals(name)) {
                return Optional.of(m);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        List<Review> avis = new ArrayList<Review>();

        if (member == null) {
            return avis;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();

            // Récupération des avis avec le titre de l'oeuvre concernée
            String sql = "SELECT r.rating, r.comment, r.review_date, a.title AS artwork_title "
                       + "FROM reviews r "
                       + "JOIN artworks a ON r.artwork_id = a.id "
                       + "JOIN community_members m ON r.reviewer_id = m.id "
                       + "WHERE m.name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, member.getName());
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                // Création d'une oeuvre minimale avec son titre
                Artwork artwork = new Artwork();
                artwork.setTitle(rs.getString("artwork_title"));

                // Création de l'avis
                Review review = new Review(member, artwork, rs.getInt("rating"), rs.getString("comment"));

                Date dateAvis = rs.getDate("review_date");
                if (dateAvis != null) {
                    review.setReviewDate(dateAvis.toLocalDate());
                }

                avis.add(review);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getReviewsByMember : " + e.getMessage());
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

        return avis;
    }
}
