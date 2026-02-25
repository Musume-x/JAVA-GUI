package dao;

import config.config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MasterlistDAO {
    
    public java.util.List<java.util.Map<String, Object>> getAllSubjects() {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT id, subject_code, subject_name, units, created_at FROM subjects_masterlist ORDER BY id DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("subject_code", rs.getString("subject_code"));
                row.put("subject_name", rs.getString("subject_name"));
                row.put("units", rs.getInt("units"));
                row.put("created_at", rs.getString("created_at"));
                rows.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error loading subjects: " + e.getMessage());
        }
        return rows;
    }
    
    public boolean addSubject(String code, String name, int units) {
        String sql = "INSERT INTO subjects_masterlist (subject_code, subject_name, units) VALUES (?, ?, ?)";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setInt(3, units);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding subject: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateSubject(int id, String code, String name, int units) {
        String sql = "UPDATE subjects_masterlist SET subject_code = ?, subject_name = ?, units = ? WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setInt(3, units);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteSubject(int id) {
        String sql = "DELETE FROM subjects_masterlist WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
            return false;
        }
    }
    
    public java.util.List<java.util.Map<String, Object>> getAllCourses() {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT id, course_code, course_name, level, created_at FROM courses_masterlist ORDER BY id DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("course_code", rs.getString("course_code"));
                row.put("course_name", rs.getString("course_name"));
                row.put("level", rs.getString("level"));
                row.put("created_at", rs.getString("created_at"));
                rows.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error loading courses: " + e.getMessage());
        }
        return rows;
    }
    
    public boolean addCourse(String code, String name, String level) {
        String sql = "INSERT INTO courses_masterlist (course_code, course_name, level) VALUES (?, ?, ?)";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setString(3, level);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding course: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateCourse(int id, String code, String name, String level) {
        String sql = "UPDATE courses_masterlist SET course_code = ?, course_name = ?, level = ? WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setString(3, level);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteCourse(int id) {
        String sql = "DELETE FROM courses_masterlist WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return false;
        }
    }
    
    public java.util.List<java.util.Map<String, Object>> getAllSections() {
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        String sql = "SELECT id, section_name, adviser, room, created_at FROM sections_masterlist ORDER BY id DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("section_name", rs.getString("section_name"));
                row.put("adviser", rs.getString("adviser"));
                row.put("room", rs.getString("room"));
                row.put("created_at", rs.getString("created_at"));
                rows.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error loading sections: " + e.getMessage());
        }
        return rows;
    }
    
    public boolean addSection(String sectionName, String adviser, String room) {
        String sql = "INSERT INTO sections_masterlist (section_name, adviser, room) VALUES (?, ?, ?)";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sectionName);
            pstmt.setString(2, adviser);
            pstmt.setString(3, room);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding section: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateSection(int id, String sectionName, String adviser, String room) {
        String sql = "UPDATE sections_masterlist SET section_name = ?, adviser = ?, room = ? WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sectionName);
            pstmt.setString(2, adviser);
            pstmt.setString(3, room);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating section: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteSection(int id) {
        String sql = "DELETE FROM sections_masterlist WHERE id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting section: " + e.getMessage());
            return false;
        }
    }
}

