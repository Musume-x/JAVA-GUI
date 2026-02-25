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
    
    public java.util.List<java.util.Map<String, Object>> getAllEnrollments() {
        java.util.List<java.util.Map<String, Object>> enrollments = new java.util.ArrayList<>();
        String sql = "SELECT ea.id, ea.user_id, ea.program, ea.level, ea.status, ea.created_at, " +
                     "pe.full_name, pe.birthdate, pe.contact_no, pe.kinder_level " +
                     "FROM enrollment_applications ea " +
                     "LEFT JOIN preschool_enrollments pe ON pe.user_id = ea.user_id " +
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
        String sql = "SELECT ea.id, ea.user_id, ea.program, ea.level, ea.status, ea.created_at " +
                     "FROM enrollment_applications ea WHERE ea.status = 'pending' ORDER BY ea.created_at DESC";
        
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                java.util.Map<String, Object> enrollment = new java.util.HashMap<>();
                enrollment.put("id", rs.getInt("id"));
                enrollment.put("user_id", rs.getInt("user_id"));
                enrollment.put("status", rs.getString("status"));
                enrollment.put("created_at", rs.getString("created_at"));
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
            
            if (existingId == null) {
                String insertApp = "INSERT INTO enrollment_applications (user_id, program, level, status) VALUES (?, 'preschool', ?, 'pending')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertApp)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, kinderLevel);
                    pstmt.executeUpdate();
                }
            } else {
                String updateApp = "UPDATE enrollment_applications SET level = ?, status = 'pending' WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateApp)) {
                    pstmt.setString(1, kinderLevel);
                    pstmt.setInt(2, existingId);
                    pstmt.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error submitting preschool enrollment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
