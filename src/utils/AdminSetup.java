package utils;

import dao.UserDAO;

/**
 * Utility class to set up admin user
 * Run this once to create an admin account
 */
public class AdminSetup {
    
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        
        // Create default admin user
        String adminEmail = "admin@scc.edu";
        String adminPassword = "admin123";
        
        if (userDAO.createAdminUser(adminEmail, adminPassword)) {
            System.out.println("Admin user created successfully!");
            System.out.println("Email: " + adminEmail);
            System.out.println("Password: " + adminPassword);
        } else {
            System.out.println("Failed to create admin user. It may already exist.");
        }
    }
}
