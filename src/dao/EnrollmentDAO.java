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
        String sql = "SELECT id, user_id, status, created_at FROM enrollment_applications ORDER BY created_at DESC";
        
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
            System.err.println("Error getting all enrollments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return enrollments;
    }
    
    public java.util.List<java.util.Map<String, Object>> getPendingEnrollments() {
        java.util.List<java.util.Map<String, Object>> enrollments = new java.util.ArrayList<>();
        String sql = "SELECT id, user_id, status, created_at FROM enrollment_applications WHERE status = 'pending' ORDER BY created_at DESC";
        
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
}
