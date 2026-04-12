package dao;

import config.config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author miwa
 */
public class EnrollmentDAO {

    /** Standard fee recorded when a student creates a new enrollment application (receipt available for download). */
    private static final double ENROLLMENT_FEE_PHP = 1000.0;

    /**
     * After a brand-new application row is created, records the default enrollment payment so the student can open/download a receipt.
     * Skipped when the student only updates an existing application for the same program.
     */
    private void recordDefaultEnrollmentPaymentIfNew(int userId, boolean newApplicationRow) {
        if (!newApplicationRow) {
            return;
        }
        PaymentTransactionDAO pay = new PaymentTransactionDAO();
        int id = pay.insert(userId, ENROLLMENT_FEE_PHP,
                "Enrollment / registration fee",
                "Online application",
                "Automatic charge when enrollment was submitted.",
                null);
        if (id < 0) {
            System.err.println("Enrollment saved but payment row was not created for user_id=" + userId);
        }
    }
    
    public int getTotalApplicants() {
        // First try to get enrollment applications count
        String sql = "SELECT COUNT(*) FROM enrollment_applications";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                int count = rs.getInt(1);
                // If there are enrollment applications, return that count
                if (count > 0) {
                    return count;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total applicants: " + e.getMessage());
        }
        
        // Fallback: count registered students if enrollment_applications is empty
        return getTotalStudents();
    }
    
    public int getTotalStudents() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'student'";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total students: " + e.getMessage());
        }
        
        return 0;
    }
    
    public int getPendingApproval() {
        String sql = "SELECT COUNT(*) FROM enrollment_applications WHERE status = 'pending'";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending approval: " + e.getMessage());
        }
        
        return 0;
    }
    
    public int getConfirmedEnrollees() {
        String sql = "SELECT COUNT(*) FROM enrollment_applications WHERE status = 'confirmed'";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting confirmed enrollees: " + e.getMessage());
        }
        
        return 0;
    }
    
    private static final String ENROLLMENT_DETAIL_JOIN =
            "FROM enrollment_applications ea " +
            "LEFT JOIN enrollment_details ed ON ed.user_id = ea.user_id AND LOWER(ed.program) = LOWER(ea.program) " +
            "LEFT JOIN preschool_enrollments pe ON pe.user_id = ea.user_id AND LOWER(COALESCE(ea.program, '')) = 'preschool' ";

    public java.util.List<java.util.Map<String, Object>> getAllEnrollments() {
        java.util.List<java.util.Map<String, Object>> enrollments = new java.util.ArrayList<>();
        String sql = "SELECT ea.id, ea.user_id, ea.program, ea.level, ea.status, ea.created_at, " +
                     "COALESCE(ed.full_name, pe.full_name) AS full_name, pe.birthdate, pe.contact_no, pe.kinder_level " +
                     ENROLLMENT_DETAIL_JOIN +
                     "ORDER BY ea.created_at DESC";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                java.util.Map<String, Object> enrollment = new java.util.HashMap<>();
                enrollment.put("id", rs.getInt("id"));
                enrollment.put("user_id", rs.getInt("user_id"));
                enrollment.put("program", rs.getString("program"));
                enrollment.put("level", rs.getString("level"));
                enrollment.put("status", rs.getString("status"));
                enrollment.put("created_at", rs.getString("created_at"));
                enrollment.put("full_name", rs.getString("full_name"));
                enrollment.put("birthdate", rs.getString("birthdate"));
                enrollment.put("contact_no", rs.getString("contact_no"));
                enrollment.put("kinder_level", rs.getString("kinder_level"));
                enrollments.add(enrollment);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all enrollments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return enrollments;
    }
    
    public java.util.List<java.util.Map<String, Object>> getPendingEnrollments() {
        java.util.List<java.util.Map<String, Object>> enrollments = new java.util.ArrayList<>();
        String sql = "SELECT ea.id, ea.user_id, ea.program, ea.level, ea.status, ea.created_at, " +
                     "COALESCE(ed.full_name, pe.full_name) AS full_name " +
                     ENROLLMENT_DETAIL_JOIN +
                     "WHERE ea.status = 'pending' ORDER BY ea.created_at DESC";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                java.util.Map<String, Object> enrollment = new java.util.HashMap<>();
                enrollment.put("id", rs.getInt("id"));
                enrollment.put("user_id", rs.getInt("user_id"));
                enrollment.put("program", rs.getString("program"));
                enrollment.put("level", rs.getString("level"));
                enrollment.put("status", rs.getString("status"));
                enrollment.put("created_at", rs.getString("created_at"));
                enrollment.put("full_name", rs.getString("full_name"));
                enrollments.add(enrollment);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending enrollments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return enrollments;
    }
    
    public boolean submitPreschoolEnrollment(int userId, String fullName, String birthdate, String contactNo, String kinderLevel) {
        try (Connection conn = config.connectDB()) {
            conn.setAutoCommit(false);
            
            // Upsert details
            String upsertDetails = "INSERT INTO preschool_enrollments (user_id, full_name, birthdate, contact_no, kinder_level) " +
                                   "VALUES (?, ?, ?, ?, ?) " +
                                   "ON CONFLICT(user_id) DO UPDATE SET " +
                                   "full_name = excluded.full_name, birthdate = excluded.birthdate, contact_no = excluded.contact_no, kinder_level = excluded.kinder_level";
            try (PreparedStatement pstmt = conn.prepareStatement(upsertDetails)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, fullName);
                pstmt.setString(3, birthdate);
                pstmt.setString(4, contactNo);
                pstmt.setString(5, kinderLevel);
                pstmt.executeUpdate();
            }
            
            // Ensure an application row exists (one per user/program)
            Integer existingId = null;
            String findApp = "SELECT id FROM enrollment_applications WHERE user_id = ? AND program = 'preschool' ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(findApp)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                    }
                }
            }
            
            boolean newApplicationRow = false;
            if (existingId == null) {
                String insertApp = "INSERT INTO enrollment_applications (user_id, program, level, status) VALUES (?, 'preschool', ?, 'pending')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertApp)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, kinderLevel);
                    pstmt.executeUpdate();
                }
                newApplicationRow = true;
            } else {
                String updateApp = "UPDATE enrollment_applications SET level = ?, status = 'pending' WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateApp)) {
                    pstmt.setString(1, kinderLevel);
                    pstmt.setInt(2, existingId);
                    pstmt.executeUpdate();
                }
            }

            upsertEnrollmentDetails(conn, userId, "preschool", fullName, birthdate, contactNo, kinderLevel);
            
            conn.commit();
            recordDefaultEnrollmentPaymentIfNew(userId, newApplicationRow);
            return true;
        } catch (SQLException e) {
            System.err.println("Error submitting preschool enrollment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean submitEnrollment(int userId, String program, String fullName, String birthdate, String contactNo, String level) {
        try (Connection conn = config.connectDB()) {
            conn.setAutoCommit(false);
            String normalizedProgram = program.toLowerCase();

            Integer existingId = null;
            String findApp = "SELECT id FROM enrollment_applications WHERE user_id = ? AND program = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(findApp)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, normalizedProgram);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) existingId = rs.getInt("id");
                }
            }

            boolean newApplicationRow = false;
            if (existingId == null) {
                String insertApp = "INSERT INTO enrollment_applications (user_id, program, level, status) VALUES (?, ?, ?, 'pending')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertApp)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, normalizedProgram);
                    pstmt.setString(3, level);
                    pstmt.executeUpdate();
                }
                newApplicationRow = true;
            } else {
                String updateApp = "UPDATE enrollment_applications SET level = ?, status = 'pending' WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateApp)) {
                    pstmt.setString(1, level);
                    pstmt.setInt(2, existingId);
                    pstmt.executeUpdate();
                }
            }

            upsertEnrollmentDetails(conn, userId, normalizedProgram, fullName, birthdate, contactNo, level);

            conn.commit();
            recordDefaultEnrollmentPaymentIfNew(userId, newApplicationRow);
            return true;
        } catch (SQLException e) {
            System.err.println("Error submitting enrollment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void upsertEnrollmentDetails(Connection conn, int userId, String program, String fullName, String birthdate, String contactNo, String level) throws SQLException {
        String upsertDetails = "INSERT INTO enrollment_details (user_id, program, full_name, birthdate, contact_no, track_level) " +
                               "VALUES (?, ?, ?, ?, ?, ?) " +
                               "ON CONFLICT(user_id, program) DO UPDATE SET " +
                               "full_name = excluded.full_name, birthdate = excluded.birthdate, contact_no = excluded.contact_no, track_level = excluded.track_level";
        try (PreparedStatement pstmt = conn.prepareStatement(upsertDetails)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, program);
            pstmt.setString(3, fullName);
            pstmt.setString(4, birthdate);
            pstmt.setString(5, contactNo);
            pstmt.setString(6, level);
            pstmt.executeUpdate();
        }
    }

    public java.util.List<java.util.Map<String, Object>> getEnrollmentStatusByUser(int userId) {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT ea.program, ea.level, ea.status, ea.created_at, ed.full_name, ed.birthdate, ed.contact_no " +
                     "FROM enrollment_applications ea " +
                     "LEFT JOIN enrollment_details ed ON ed.user_id = ea.user_id AND ed.program = ea.program " +
                     "WHERE ea.user_id = ? ORDER BY ea.created_at DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    row.put("program", rs.getString("program"));
                    row.put("level", rs.getString("level"));
                    row.put("status", rs.getString("status"));
                    row.put("created_at", rs.getString("created_at"));
                    row.put("full_name", rs.getString("full_name"));
                    row.put("birthdate", rs.getString("birthdate"));
                    row.put("contact_no", rs.getString("contact_no"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading user enrollment statuses: " + e.getMessage());
        }
        return rows;
    }

    /**
     * Pending and approved applications for admin masterlist (excludes rejected).
     */
    public java.util.List<java.util.Map<String, Object>> getPendingAndConfirmedEnrollments() {
        java.util.List<java.util.Map<String, Object>> enrollments = new java.util.ArrayList<>();
        String sql = "SELECT ea.id, ea.user_id, ea.program, ea.level, ea.status, ea.created_at, " +
                     "COALESCE(ed.full_name, pe.full_name) AS full_name " +
                     ENROLLMENT_DETAIL_JOIN +
                     "WHERE LOWER(COALESCE(ea.status, '')) IN ('pending', 'confirmed') " +
                     "ORDER BY ea.created_at DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> enrollment = new java.util.HashMap<>();
                enrollment.put("id", rs.getInt("id"));
                enrollment.put("user_id", rs.getInt("user_id"));
                enrollment.put("program", rs.getString("program"));
                enrollment.put("level", rs.getString("level"));
                enrollment.put("status", rs.getString("status"));
                enrollment.put("created_at", rs.getString("created_at"));
                enrollment.put("full_name", rs.getString("full_name"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            System.err.println("Error loading masterlist enrollments: " + e.getMessage());
            e.printStackTrace();
        }
        return enrollments;
    }

    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        if (newStatus == null) {
            return false;
        }
        String s = newStatus.trim().toLowerCase();
        if (!"pending".equals(s) && !"confirmed".equals(s) && !"rejected".equals(s)) {
            return false;
        }
        String sql = "UPDATE enrollment_applications SET status = ? WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s);
            pstmt.setInt(2, applicationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating application status: " + e.getMessage());
            return false;
        }
    }
}
