/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package admin;

import dao.EnrollmentDAO;
import dao.PaymentTransactionDAO;
import dao.UserDAO;
import gui.ReceiptDialog;
import main.AppNavigator;
import main.login;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author miwa
 */
public class adminDashPane extends javax.swing.JPanel {
    
    private EnrollmentDAO enrollmentDAO;
    private UserDAO userDAO;
    private PaymentTransactionDAO paymentTransactionDAO;
    /** Parallel to jList1 rows when there are pending applications (empty when none). */
    private final List<Integer> pendingListApplicationIds = new ArrayList<>();
    /** Which dataset jTable1 is showing: dashboard, enrollments, finance, or users. */
    private int dashMainTableMode = 0;
    private static final int DASH_MAIN = 0;
    private static final int DASH_ENROLL = 1;
    private static final int DASH_FINANCE = 2;
    private static final int DASH_USERS = 3;
    private JButton recordPaymentBtn;

    /** Creates new form adminDashPane */
    public adminDashPane() {
        initComponents();
        ensureLoggedIn();
        enrollmentDAO = new EnrollmentDAO();
        userDAO = new UserDAO();
        paymentTransactionDAO = new PaymentTransactionDAO();
        recordPaymentBtn = new JButton("Record payment");
        recordPaymentBtn.setFont(new java.awt.Font("Trebuchet MS", 0, 12));
        recordPaymentBtn.setVisible(false);
        recordPaymentBtn.addActionListener(e -> openRecordPaymentDialog());
        jPanel1.add(recordPaymentBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 82, 160, 28));
        // Update counts after component is fully initialized
        SwingUtilities.invokeLater(() -> {
            updateCounts();
            showDashboard(); // Show dashboard view by default
        });
        addEventHandlers();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new main.login().setVisible(true);
        });
    }
    
    private void ensureLoggedIn() {
        if (login.getCurrentUser() == null) {
            SwingUtilities.invokeLater(() -> {
                login log = new login();
                log.setVisible(true);
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    frame.dispose();
                }
            });
        }
    }
    
    private void addEventHandlers() {
        // Dashboard click handler
        jLabel7.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDashboard();
            }
        });
        
        // Enrollment Management click handler
        jLabel9.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showEnrollmentManagement();
            }
        });
        
        // Financials click handler
        jLabel10.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showFinancials();
            }
        });
        
        // User Management click handler
        jLabel11.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUserManagement();
            }
        });
        
        // Masterlist click handler
        Masterlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openMasterlist();
            }
        });
        
        profile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openAdminProfile();
            }
        });
        
        logout1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                performLogout();
            }
        });

        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleMainTableDoubleClick();
                }
            }
        });

        jList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handlePendingListReview();
                }
            }
        });
    }

    private void handleMainTableDoubleClick() {
        if (dashMainTableMode == DASH_FINANCE) {
            handleFinancialReceipt();
            return;
        }
        handleEnrollmentTableReview();
    }

    private void handleFinancialReceipt() {
        if (!(jTable1.getModel() instanceof DefaultTableModel)) {
            return;
        }
        DefaultTableModel m = (DefaultTableModel) jTable1.getModel();
        if (m.getColumnCount() != 8 || !"Txn ID".equals(m.getColumnName(0))) {
            return;
        }
        int viewRow = jTable1.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction row first.");
            return;
        }
        int row = jTable1.convertRowIndexToModel(viewRow);
        Object idObj = m.getValueAt(row, 0);
        if (idObj == null) {
            return;
        }
        int txnId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());
        Map<String, Object> tx = paymentTransactionDAO.findById(txnId);
        ReceiptDialog.show(this, tx);
    }

    private void openRecordPaymentDialog() {
        JTextField userIdF = new JTextField(6);
        JTextField amountF = new JTextField(10);
        JTextField purposeF = new JTextField("Enrollment / processing fee", 24);
        JComboBox<String> method = new JComboBox<>(new String[]{"Cash", "Bank transfer", "GCash", "Card", "Other"});
        JTextField notesF = new JTextField(20);
        JPanel p = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Student user ID (from User Management) *"));
        p.add(userIdF);
        p.add(new JLabel("Amount (PHP) *"));
        p.add(amountF);
        p.add(new JLabel("Purpose"));
        p.add(purposeF);
        p.add(new JLabel("Payment method"));
        p.add(method);
        p.add(new JLabel("Notes (optional)"));
        p.add(notesF);
        int ok = JOptionPane.showConfirmDialog(this, p, "Record payment", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        int userId;
        try {
            userId = Integer.parseInt(userIdF.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "User ID must be a whole number.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountF.getText().trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userDAO.getUserById(userId) == null) {
            JOptionPane.showMessageDialog(this, "No user exists with that ID.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer recorder = login.getCurrentUser() != null ? login.getCurrentUser().getId() : null;
        int newId = paymentTransactionDAO.insert(userId, amount, purposeF.getText().trim(),
                method.getSelectedItem() != null ? method.getSelectedItem().toString() : "",
                notesF.getText().trim(),
                recorder);
        if (newId > 0) {
            JOptionPane.showMessageDialog(this, "Payment recorded.");
            Map<String, Object> tx = paymentTransactionDAO.findById(newId);
            ReceiptDialog.show(this, tx);
            loadPaymentsTable();
        } else {
            JOptionPane.showMessageDialog(this, "Could not save the payment. Check the console for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Double-click a row in Enrollment Management to approve or reject a pending application.
     */
    private void handleEnrollmentTableReview() {
        if (!(jTable1.getModel() instanceof DefaultTableModel)) {
            return;
        }
        DefaultTableModel m = (DefaultTableModel) jTable1.getModel();
        if (m.getColumnCount() != 7 || !"ID".equals(m.getColumnName(0)) || !"Status".equals(m.getColumnName(5))) {
            return;
        }
        int viewRow = jTable1.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an application row first.");
            return;
        }
        int row = jTable1.convertRowIndexToModel(viewRow);
        Object idObj = m.getValueAt(row, 0);
        if (idObj == null) {
            return;
        }
        int applicationId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());
        String fullName = m.getValueAt(row, 2) != null ? m.getValueAt(row, 2).toString() : "";
        String program = m.getValueAt(row, 3) != null ? m.getValueAt(row, 3).toString() : "";
        String level = m.getValueAt(row, 4) != null ? m.getValueAt(row, 4).toString() : "";
        Object statusObj = m.getValueAt(row, 5);
        String status = statusObj != null ? statusObj.toString().trim() : "";

        if (!"pending".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                    "Only pending applications can be approved or rejected here.\nCurrent status: " + status);
            return;
        }

        reviewPendingApplication(applicationId, fullName, program, level, true);
    }

    /**
     * Double-click an item under Pending Approval on the dashboard.
     */
    private void handlePendingListReview() {
        int idx = jList1.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a pending application in the list first.");
            return;
        }
        if (pendingListApplicationIds.isEmpty() || idx >= pendingListApplicationIds.size()) {
            JOptionPane.showMessageDialog(this, "No pending applications to review.");
            return;
        }
        int applicationId = pendingListApplicationIds.get(idx);
        String fullName = "";
        String program = "";
        String level = "";
        for (java.util.Map<String, Object> en : enrollmentDAO.getPendingEnrollments()) {
            Object idObj = en.get("id");
            int id = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());
            if (id == applicationId) {
                fullName = en.get("full_name") != null ? en.get("full_name").toString() : "";
                program = en.get("program") != null ? en.get("program").toString() : "";
                level = en.get("level") != null ? en.get("level").toString() : "";
                break;
            }
        }
        reviewPendingApplication(applicationId, fullName, program, level, false);
    }

    /**
     * @param fromEnrollmentTable if true, reopen Enrollment Management after success; if false, stay on dashboard and refresh counts.
     */
    private void reviewPendingApplication(int applicationId, String fullName, String program, String level,
            boolean fromEnrollmentTable) {
        String summary = "Application #" + applicationId + "\n"
                + (fullName.isEmpty() ? "" : "Name: " + fullName + "\n")
                + "Program: " + program + "\n"
                + "Level: " + level;
        Object[] options = {"Approve", "Reject", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                summary,
                "Review enrollment",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        Runnable afterSuccess = () -> {
            updateCounts();
            if (fromEnrollmentTable) {
                showEnrollmentManagement();
            }
        };

        if (choice == 0) {
            if (enrollmentDAO.updateApplicationStatus(applicationId, "confirmed")) {
                JOptionPane.showMessageDialog(this, "Application approved.");
                afterSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Could not update application.");
            }
        } else if (choice == 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reject application #" + applicationId + "?",
                    "Confirm reject",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && enrollmentDAO.updateApplicationStatus(applicationId, "rejected")) {
                JOptionPane.showMessageDialog(this, "Application rejected.");
                afterSuccess.run();
            }
        }
    }
    
    public void showDashboard() {
        dashMainTableMode = DASH_MAIN;
        recordPaymentBtn.setVisible(false);
        // Refresh counts and show default dashboard view
        updateCounts();
        jLabel3.setText("Total Applicants: " + (enrollmentDAO.getTotalApplicants() > 0 ? enrollmentDAO.getTotalApplicants() : enrollmentDAO.getTotalStudents()));
        
        // Clear the table or show default message
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Dashboard Overview"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(model);
    }
    
    public void showEnrollmentManagement() {
        dashMainTableMode = DASH_ENROLL;
        recordPaymentBtn.setVisible(false);
        try {
            // Get enrollment applications
            List<java.util.Map<String, Object>> enrollments = enrollmentDAO.getAllEnrollments();
            
            String[] columnNames = {"ID", "User ID", "Full Name", "Program", "Level", "Status", "Created At"};
            Object[][] data = new Object[enrollments.size()][7];
            
            for (int i = 0; i < enrollments.size(); i++) {
                java.util.Map<String, Object> enrollment = enrollments.get(i);
                data[i][0] = enrollment.get("id");
                data[i][1] = enrollment.get("user_id");
                data[i][2] = enrollment.get("full_name") != null ? enrollment.get("full_name").toString() : "";
                data[i][3] = enrollment.get("program") != null ? enrollment.get("program").toString() : "";
                data[i][4] = enrollment.get("level") != null ? enrollment.get("level").toString() : "";
                data[i][5] = enrollment.get("status");
                data[i][6] = enrollment.get("created_at") != null ? enrollment.get("created_at").toString() : "";
            }
            
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            jTable1.setModel(model);
            jLabel3.setText("Enrollment Management - Total Applications: " + enrollments.size()
                    + " (double-click a pending row to approve or reject)");
            
        } catch (Exception e) {
            System.err.println("Error loading enrollments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void showFinancials() {
        dashMainTableMode = DASH_FINANCE;
        recordPaymentBtn.setVisible(true);
        loadPaymentsTable();
    }

    private void loadPaymentsTable() {
        try {
            List<Map<String, Object>> rows = paymentTransactionDAO.findAllWithPayer();
            NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            String[] columnNames = {"Txn ID", "Receipt No", "User ID", "Payer", "Amount", "Purpose", "Method", "Date"};
            Object[][] data = new Object[rows.size()][8];
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> r = rows.get(i);
                data[i][0] = r.get("id");
                data[i][1] = r.get("receipt_no");
                data[i][2] = r.get("user_id");
                data[i][3] = r.get("payer_name") != null ? r.get("payer_name").toString() : "";
                double amt = r.get("amount") instanceof Number ? ((Number) r.get("amount")).doubleValue() : 0;
                data[i][4] = money.format(amt);
                data[i][5] = r.get("purpose");
                data[i][6] = r.get("payment_method");
                data[i][7] = r.get("created_at") != null ? r.get("created_at").toString() : "";
            }
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jTable1.setModel(model);
            jLabel3.setText("Financials — double-click a row for receipt | " + rows.size() + " transaction(s)");
            NumberFormat cur = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            jLabel6.setText("Total Revenue: " + cur.format(paymentTransactionDAO.sumTotalAmount()));
        } catch (Exception e) {
            System.err.println("Error loading payments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void showUserManagement() {
        dashMainTableMode = DASH_USERS;
        recordPaymentBtn.setVisible(false);
        try {
            List<java.util.Map<String, Object>> users = userDAO.getAllUsers();
            
            // Create column names
            String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Role", "Created At"};
            
            // Create data array
            Object[][] data = new Object[users.size()][6];
            
            for (int i = 0; i < users.size(); i++) {
                java.util.Map<String, Object> user = users.get(i);
                data[i][0] = user.get("id");
                data[i][1] = user.get("first_name");
                data[i][2] = user.get("last_name");
                data[i][3] = user.get("email");
                data[i][4] = user.get("role");
                data[i][5] = user.get("created_at") != null ? user.get("created_at").toString() : "";
            }
            
            // Create non-editable table model
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make all cells non-editable
                }
            };
            
            // Set the model to jTable1
            jTable1.setModel(model);
            
            // Update label to show User Management
            jLabel3.setText("User Management - Total Users: " + users.size());
            
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateCounts() {
        try {
            // Get total students (registered users)
            int totalStudents = enrollmentDAO.getTotalStudents();
            // Get enrollment applications count
            int enrollmentApps = enrollmentDAO.getTotalApplicants();
            // Use students count if enrollment apps is 0, otherwise use enrollment apps
            int totalApplicants = enrollmentApps > 0 ? enrollmentApps : totalStudents;
            
            int pendingApproval = enrollmentDAO.getPendingApproval();
            int confirmedEnrollees = enrollmentDAO.getConfirmedEnrollees();
            
            jLabel3.setText("Total Applicants: " + totalApplicants);
            jLabel4.setText("Pending Approval: " + pendingApproval);
            jLabel5.setText("Confirmed Enrollees: " + confirmedEnrollees);
            
            // Also update jLabel8 which shows "Confirmed Enrollees" header
            jLabel8.setText("Confirmed Enrollees: " + confirmedEnrollees);
            
            // Update pending approval list
            updatePendingApprovalList();
            
            // Update confirmed enrollees table
            updateConfirmedEnrolleesTable();

            NumberFormat cur = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
            jLabel6.setText("Total Revenue: " + cur.format(paymentTransactionDAO.sumTotalAmount()));
            
        } catch (Exception e) {
            System.err.println("Error updating counts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updatePendingApprovalList() {
        try {
            List<java.util.Map<String, Object>> pending = enrollmentDAO.getPendingEnrollments();
            DefaultListModel<String> listModel = new DefaultListModel<>();
            pendingListApplicationIds.clear();

            for (java.util.Map<String, Object> enrollment : pending) {
                Object idObj = enrollment.get("id");
                int appId = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());
                pendingListApplicationIds.add(appId);
                String name = enrollment.get("full_name") != null ? enrollment.get("full_name").toString() : "";
                String item = "App #" + enrollment.get("id")
                        + (name.isEmpty() ? "" : " — " + name)
                        + " — " + (enrollment.get("program") != null ? enrollment.get("program").toString() : "")
                        + " (" + (enrollment.get("level") != null ? enrollment.get("level").toString() : "") + ")";
                listModel.addElement(item);
            }

            if (listModel.isEmpty()) {
                listModel.addElement("No pending approvals");
            }

            jList1.setModel(listModel);
            jLabel4.setText(pendingListApplicationIds.isEmpty()
                    ? "Pending Approval"
                    : "Pending Approval (double-click to review)");
        } catch (Exception e) {
            System.err.println("Error updating pending approval list: " + e.getMessage());
        }
    }
    
    private void updateConfirmedEnrolleesTable() {
        try {
            List<java.util.Map<String, Object>> confirmed = enrollmentDAO.getAllEnrollments();
            confirmed.removeIf(e -> !"confirmed".equals(e.get("status")));
            
            String[] columnNames = {"ID", "User ID", "Full Name", "Program", "Level", "Status", "Created At"};
            Object[][] data = new Object[confirmed.size()][7];
            
            for (int i = 0; i < confirmed.size(); i++) {
                java.util.Map<String, Object> enrollment = confirmed.get(i);
                data[i][0] = enrollment.get("id");
                data[i][1] = enrollment.get("user_id");
                data[i][2] = enrollment.get("full_name") != null ? enrollment.get("full_name").toString() : "";
                data[i][3] = enrollment.get("program");
                data[i][4] = enrollment.get("level");
                data[i][5] = enrollment.get("status");
                data[i][6] = enrollment.get("created_at") != null ? enrollment.get("created_at").toString() : "";
            }
            
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            jTable2.setModel(model);
        } catch (Exception e) {
            System.err.println("Error updating confirmed enrollees table: " + e.getMessage());
        }
    }
    
    public void refreshCounts() {
        updateCounts();
    }
    
    private void openAdminProfile() {
        AppNavigator.showAdminProfile(() -> AppNavigator.showAdminDashboard(AppNavigator::showLogin));
        JFrame currentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }
    
    private void openUserManagement() {
        if (login.getCurrentUser() == null) {
            ensureLoggedIn();
            return;
        }
        AppNavigator.showUserManagement();
        JFrame currentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            login.setCurrentUser(null);
            login log = new login();
            log.setVisible(true);
            JFrame currentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
            if (currentFrame != null) {
                currentFrame.dispose();
            }
        }
    }
    
    private void openMasterlist() {
        if (login.getCurrentUser() == null) {
            login log = new login();
            log.setVisible(true);
            JFrame currentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
            if (currentFrame != null) {
                currentFrame.dispose();
            }
            return;
        }
        
        AppNavigator.showMasterlist();
        JFrame currentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logout = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        panelRound5 = new onlineenrollment.main.PanelRound();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        panelRound1 = new onlineenrollment.main.PanelRound();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        panelRound2 = new onlineenrollment.main.PanelRound();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        profile = new javax.swing.JLabel();
        logout1 = new javax.swing.JLabel();
        Masterlist = new javax.swing.JLabel();
        panelRound3 = new onlineenrollment.main.PanelRound();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        panelRound4 = new onlineenrollment.main.PanelRound();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        panelRound6 = new onlineenrollment.main.PanelRound();
        jLabel6 = new javax.swing.JLabel();

        logout.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        logout.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logout.setText("Logout");

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelRound5.setBackground(new java.awt.Color(255, 255, 255));
        panelRound5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelRound5.setRoundBottomLeft(15);
        panelRound5.setRoundBottomRight(15);
        panelRound5.setRoundTopLeft(15);
        panelRound5.setRoundTopRight(15);

        jLabel5.setFont(new java.awt.Font("Trebuchet MS", 0, 13)); // NOI18N
        jLabel5.setText("Confirmed Enrollees");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane7.setViewportView(jTable2);

        jLabel8.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jLabel8.setText("Confirmed Enrollees");

        javax.swing.GroupLayout panelRound5Layout = new javax.swing.GroupLayout(panelRound5);
        panelRound5.setLayout(panelRound5Layout);
        panelRound5Layout.setHorizontalGroup(
            panelRound5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                    .addGroup(panelRound5Layout.createSequentialGroup()
                        .addGroup(panelRound5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRound5Layout.setVerticalGroup(
            panelRound5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound5Layout.createSequentialGroup()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(78, 78, 78)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(panelRound5, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 380, 520, 160));

        panelRound1.setBackground(new java.awt.Color(255, 255, 255));
        panelRound1.setRoundBottomLeft(30);
        panelRound1.setRoundBottomRight(30);
        panelRound1.setRoundTopLeft(30);
        panelRound1.setRoundTopRight(30);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/scc-logo.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("ST. CECILIA'S COLLEGE - CEBU, INC.");

        javax.swing.GroupLayout panelRound1Layout = new javax.swing.GroupLayout(panelRound1);
        panelRound1.setLayout(panelRound1Layout);
        panelRound1Layout.setHorizontalGroup(
            panelRound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(698, Short.MAX_VALUE))
        );
        panelRound1Layout.setVerticalGroup(
            panelRound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
            .addGroup(panelRound1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.add(panelRound1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 1010, 60));

        panelRound2.setBackground(new java.awt.Color(255, 255, 255));
        panelRound2.setRoundBottomLeft(8);
        panelRound2.setRoundBottomRight(8);
        panelRound2.setRoundTopLeft(8);
        panelRound2.setRoundTopRight(8);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Dashboard");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Enrollment Management");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Financials");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("User Management");

        profile.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        profile.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        profile.setText("Profile");

        logout1.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        logout1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logout1.setText("Logout");

        Masterlist.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        Masterlist.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Masterlist.setText("Masterlist");

        javax.swing.GroupLayout panelRound2Layout = new javax.swing.GroupLayout(panelRound2);
        panelRound2.setLayout(panelRound2Layout);
        panelRound2Layout.setHorizontalGroup(
            panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelRound2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(profile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logout1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelRound2Layout.createSequentialGroup()
                        .addComponent(Masterlist, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRound2Layout.setVerticalGroup(
            panelRound2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(profile, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(Masterlist, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(logout1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64))
        );

        jPanel1.add(panelRound2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 150, 460));

        panelRound3.setBackground(new java.awt.Color(255, 255, 255));
        panelRound3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelRound3.setRoundBottomLeft(15);
        panelRound3.setRoundBottomRight(15);
        panelRound3.setRoundTopLeft(15);
        panelRound3.setRoundTopRight(15);

        jLabel3.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        jLabel3.setText("Total Aplicants");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout panelRound3Layout = new javax.swing.GroupLayout(panelRound3);
        panelRound3.setLayout(panelRound3Layout);
        panelRound3Layout.setHorizontalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
                    .addGroup(panelRound3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRound3Layout.setVerticalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 731, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(panelRound3, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 80, 680, 290));

        panelRound4.setBackground(new java.awt.Color(255, 255, 255));
        panelRound4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelRound4.setRoundBottomLeft(15);
        panelRound4.setRoundBottomRight(15);
        panelRound4.setRoundTopLeft(15);
        panelRound4.setRoundTopRight(15);

        jLabel4.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jLabel4.setText("Pending Approval");

        jList1.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane5.setViewportView(jList1);

        javax.swing.GroupLayout panelRound4Layout = new javax.swing.GroupLayout(panelRound4);
        panelRound4.setLayout(panelRound4Layout);
        panelRound4Layout.setHorizontalGroup(
            panelRound4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelRound4Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRound4Layout.setVerticalGroup(
            panelRound4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound4Layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 726, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(panelRound4, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 80, 130, 290));

        panelRound6.setBackground(new java.awt.Color(255, 255, 255));
        panelRound6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelRound6.setRoundBottomLeft(15);
        panelRound6.setRoundBottomRight(15);
        panelRound6.setRoundTopLeft(15);
        panelRound6.setRoundTopRight(15);

        jLabel6.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jLabel6.setText("Total Revenue");

        javax.swing.GroupLayout panelRound6Layout = new javax.swing.GroupLayout(panelRound6);
        panelRound6.setLayout(panelRound6Layout);
        panelRound6Layout.setHorizontalGroup(
            panelRound6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(176, Short.MAX_VALUE))
        );
        panelRound6Layout.setVerticalGroup(
            panelRound6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound6Layout.createSequentialGroup()
                .addGap(354, 354, 354)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(panelRound6, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 380, 290, 160));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Masterlist;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel logout;
    private javax.swing.JLabel logout1;
    private onlineenrollment.main.PanelRound panelRound1;
    private onlineenrollment.main.PanelRound panelRound2;
    private onlineenrollment.main.PanelRound panelRound3;
    private onlineenrollment.main.PanelRound panelRound4;
    private onlineenrollment.main.PanelRound panelRound5;
    private onlineenrollment.main.PanelRound panelRound6;
    private javax.swing.JLabel profile;
    // End of variables declaration//GEN-END:variables

}
