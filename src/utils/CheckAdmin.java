package utils;

import dao.UserDAO;
import config.config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Utility to check if admin user exists and verify password hashing
 */
public class CheckAdmin {
    
    public static void main(String[] args) {
        config dbConfig = new config();
        UserDAO userDAO = new UserDAO();
        
        String adminEmail = "admin@scc.edu";
        String adminPassword = "admin123";
        
        System.out.println("=== Checking Admin User ===");
        
        // Check if admin exists
        try (Connection conn = config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            
            pstmt.setString(1, adminEmail);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Admin user found!");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Stored Password Hash: " + rs.getString("password"));
                
                // Test password hashing
                String testPasswordHash = dbConfig.hashPassword(adminPassword);
                String storedHash = rs.getString("password");
                
                System.out.println("\n=== Password Hash Test ===");
                System.out.println("Provided password: " + adminPassword);
                System.out.println("Hashed password: " + testPasswordHash);
                System.out.println("Stored hash: " + storedHash);
                System.out.println("Hashes match: " + testPasswordHash.equals(storedHash));
                
                // Test login
                System.out.println("\n=== Testing Login ===");
                if (userDAO.loginUser(adminEmail, adminPassword) != null) {
                    System.out.println("✓ Login successful!");
                } else {
                    System.out.println("✗ Login failed!");
                }
                
            } else {
                System.out.println("Admin user NOT found!");
                System.out.println("Creating admin user now...");
                
                if (userDAO.createAdminUser(adminEmail, adminPassword)) {
                    System.out.println("✓ Admin user created successfully!");
                    System.out.println("Email: " + adminEmail);
                    System.out.println("Password: " + adminPassword);
                } else {
                    System.out.println("✗ Failed to create admin user!");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
