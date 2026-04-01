package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service ateliers branché sur la base MySQL via JDBC
public class JdbcWorkshopService implements WorkshopService {

    // DAO utilisé pour accéder à la table workshops
    private final JdbcWorkshopDao workshopDao = new JdbcWorkshopDao();

    @Override
    public List<Workshop> getAllWorkshops() {
        // Délégation directe au DAO
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        // Parcours de la liste pour trouver l'atelier par son titre
        List<Workshop> tous = workshopDao.findAll();

        for (Workshop w : tous) {
            if (w.getTitle().equals(title)) {
                return Optional.of(w);
            }
        }

        return Optional.empty();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();

            // Insertion via sous-requête pour éviter de chercher les IDs manuellement
            String sql = "INSERT INTO bookings (workshop_id, member_id, payment_status) "
                       + "SELECT w.id, m.id, 'PENDING' "
                       + "FROM workshops w, community_members m "
                       + "WHERE w.title = ? AND m.name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, workshop.getTitle());
            stmt.setString(2, member.getName());

            // Exécution de la réservation
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur bookWorkshop : " + e.getMessage());
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        List<Booking> reservations = new ArrayList<Booking>();

        if (member == null) {
            return reservations;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();

            // Récupération des réservations avec les détails de l'atelier
            String sql = "SELECT w.title, w.date, w.duration_minutes, w.max_participants, "
                       + "w.price, w.location, w.description, w.level, "
                       + "ar.name AS instructor_name, "
                       + "b.booking_date, b.payment_status "
                       + "FROM bookings b "
                       + "JOIN workshops w ON b.workshop_id = w.id "
                       + "JOIN artists ar ON w.instructor_id = ar.id "
                       + "JOIN community_members m ON b.member_id = m.id "
                       + "WHERE m.name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, member.getName());
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                // Reconstruction de l'objet Workshop
                Workshop workshop = new Workshop();
                workshop.setTitle(rs.getString("title"));
                workshop.setDurationMinutes(rs.getInt("duration_minutes"));
                workshop.setMaxParticipants(rs.getInt("max_participants"));
                workshop.setPrice(rs.getDouble("price"));
                workshop.setLocation(rs.getString("location"));
                workshop.setDescription(rs.getString("description"));
                workshop.setLevel(rs.getString("level"));

                Timestamp date = rs.getTimestamp("date");
                if (date != null) {
                    workshop.setDate(date.toLocalDateTime());
                }

                Artist instructeur = new Artist();
                instructeur.setName(rs.getString("instructor_name"));
                workshop.setInstructor(instructeur);

                // Création de la réservation
                Booking booking = new Booking(workshop, member);

                Timestamp dateResa = rs.getTimestamp("booking_date");
                if (dateResa != null) {
                    booking.setBookingDate(dateResa.toLocalDateTime());
                }

                booking.setPaymentStatus(rs.getString("payment_status"));
                reservations.add(booking);
            }

        } catch (SQLException e) {
            System.err.println("Erreur getBookingsByMember : " + e.getMessage());
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

        return reservations;
    }
}
