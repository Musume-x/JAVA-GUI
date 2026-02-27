/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import dao.UserDAO;
import main.login;
import model.User;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.RowFilter;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author miwa
 */
public class userManagement extends javax.swing.JPanel {
    
    private final UserDAO userDAO = new UserDAO();
    private TableRowSorter<DefaultTableModel> sorter;
    private JDialog crudDialog;

    /**
     * Creates new form userManagement
     */
    public userManagement() {
        initComponents();
        ensureLoggedIn();
        initUserTable();
        loadUsers();
        addEventHandlers();
        setupSearch();
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
    
    private void initUserTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "First Name", "Last Name", "Email", "Role", "Created At"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usermanagement.setModel(model);
        sorter = new TableRowSorter<>(model);
        usermanagement.setRowSorter(sorter);
    }
    
    private void loadUsers() {
        try {
            List<java.util.Map<String, Object>> users = userDAO.getAllUsers();
            DefaultTableModel model = (DefaultTableModel) usermanagement.getModel();
            model.setRowCount(0);
            
            for (java.util.Map<String, Object> u : users) {
                model.addRow(new Object[]{
                    u.get("id"),
                    u.get("first_name"),
                    u.get("last_name"),
                    u.get("email"),
                    u.get("role"),
                    u.get("created_at")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    private void setupSearch() {
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearch();
            }
        });
    }
    
    private void applySearch() {
        String text = searchBar.getText();
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text.trim())));
    }
    
    private void addEventHandlers() {
        // Sidebar navigation
        jLabel7.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDashboard();
            }
        });
        
        jLabel9.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDashboardAndShowEnrollments();
            }
        });
        
        jLabel10.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDashboardAndShowFinancials();
            }
        });
        
        // User management: current page (no action)
        
        Masterlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openMasterlist();
            }
        });
        
        jLabel5.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                performLogout();
            }
        });
        
        // CRUD button
        adminCRUD.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCrudDialog();
            }
        });
        jLabel12.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCrudDialog();
            }
        });
    }
    
    private void openDashboard() {
        adminDashPane pane = new adminDashPane();
        JFrame frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(pane);
        frame.pack();
        frame.setSize(1020, 560);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) currentFrame.dispose();
    }
    
    private void openDashboardAndShowEnrollments() {
        adminDashPane pane = new adminDashPane();
        pane.showEnrollmentManagement();
        openPanelInFrame(pane, "Enrollment Management");
    }
    
    private void openDashboardAndShowFinancials() {
        adminDashPane pane = new adminDashPane();
        pane.showFinancials();
        openPanelInFrame(pane, "Financials");
    }
    
    private void openMasterlist() {
        masterlist panel = new masterlist();
        openPanelInFrame(panel, "Masterlist");
    }
    
    private void performLogout() {
        login.setCurrentUser(null);
        login log = new login();
        log.setVisible(true);
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) currentFrame.dispose();
    }
    
    private void openPanelInFrame(javax.swing.JPanel panel, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setSize(1020, 560);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) currentFrame.dispose();
    }
    
    private void openCrudDialog() {
        openCrudWindow();
    }

    private void openCrudWindow() {
        if (crudDialog != null && crudDialog.isShowing()) {
            crudDialog.toFront();
            return;
        }
        if (login.getCurrentUser() == null) {
            ensureLoggedIn();
            return;
        }

        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        crudDialog = new JDialog(owner, "User Management - CRUD", true);
        crudDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        crudDialog.setSize(new Dimension(900, 520));
        crudDialog.setLocationRelativeTo(owner);

        // Table (fresh instance, so CRUD is self-contained)
        DefaultTableModel tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "First Name", "Last Name", "Email", "Role", "Created At"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        javax.swing.JTable table = new javax.swing.JTable(tableModel);
        TableRowSorter<DefaultTableModel> localSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(localSorter);
        JScrollPane tableScroll = new JScrollPane(table);

        JTextField filterField = new JTextField();
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
            private void apply() {
                String t = filterField.getText();
                if (t == null || t.trim().isEmpty()) {
                    localSorter.setRowFilter(null);
                } else {
                    localSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t.trim())));
                }
            }
        });

        // Form fields
        JTextField idField = new JTextField();
        idField.setEnabled(false);
        JTextField firstField = new JTextField();
        JTextField lastField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField roleField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton clearBtn = new JButton("Clear");
        JButton closeBtn = new JButton("Close");

        Runnable clear = () -> {
            idField.setText("");
            firstField.setText("");
            lastField.setText("");
            emailField.setText("");
            roleField.setText("");
            passField.setText("");
            table.clearSelection();
        };

        Runnable load = () -> {
            try {
                List<java.util.Map<String, Object>> users = userDAO.getAllUsers();
                tableModel.setRowCount(0);
                for (java.util.Map<String, Object> u : users) {
                    tableModel.addRow(new Object[]{
                        u.get("id"),
                        u.get("first_name"),
                        u.get("last_name"),
                        u.get("email"),
                        u.get("role"),
                        u.get("created_at")
                    });
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(crudDialog, "Failed to load users.");
            }
        };
        load.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            idField.setText(String.valueOf(tableModel.getValueAt(modelRow, 0)));
            firstField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
            lastField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
            emailField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
            roleField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
            passField.setText("");
        });

        addBtn.addActionListener(ev -> {
            String first = firstField.getText().trim();
            String last = lastField.getText().trim();
            String email = emailField.getText().trim();
            String role = roleField.getText().trim();
            String pass = new String(passField.getPassword());
            if (first.isEmpty() || last.isEmpty() || email.isEmpty() || role.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(crudDialog, "Fill First/Last/Email/Role/Password.");
                return;
            }
            User user = new User();
            user.setFirstName(first);
            user.setLastName(last);
            user.setEmail(email);
            user.setRole(role);
            user.setPassword(pass);
            boolean ok = userDAO.registerUser(user);
            if (!ok) {
                JOptionPane.showMessageDialog(crudDialog, "Add failed (email may already exist).");
                return;
            }
            load.run();
            clear.run();
        });

        updateBtn.addActionListener(ev -> {
            if (idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(crudDialog, "Select a user row to update.");
                return;
            }
            int id;
            try {
                id = Integer.parseInt(idField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(crudDialog, "Invalid ID.");
                return;
            }
            User existing = userDAO.getUserById(id);
            if (existing == null) {
                JOptionPane.showMessageDialog(crudDialog, "User not found.");
                return;
            }
            existing.setFirstName(firstField.getText().trim());
            existing.setLastName(lastField.getText().trim());
            existing.setEmail(emailField.getText().trim());
            existing.setRole(roleField.getText().trim());
            boolean ok = userDAO.updateUserAdmin(existing);
            if (!ok) {
                JOptionPane.showMessageDialog(crudDialog, "Update failed.");
                return;
            }
            load.run();
        });

        deleteBtn.addActionListener(ev -> {
            if (idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(crudDialog, "Select a user row to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(crudDialog, "Delete selected user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int id;
            try {
                id = Integer.parseInt(idField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(crudDialog, "Invalid ID.");
                return;
            }
            boolean ok = userDAO.deleteUserById(id);
            if (!ok) {
                JOptionPane.showMessageDialog(crudDialog, "Delete failed.");
                return;
            }
            load.run();
            clear.run();
        });

        refreshBtn.addActionListener(ev -> load.run());
        clearBtn.addActionListener(ev -> clear.run());
        closeBtn.addActionListener(ev -> crudDialog.dispose());

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        top.add(filterField, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int r = 0;
        addFormRow(form, gbc, r++, "ID", idField);
        addFormRow(form, gbc, r++, "First Name", firstField);
        addFormRow(form, gbc, r++, "Last Name", lastField);
        addFormRow(form, gbc, r++, "Email", emailField);
        addFormRow(form, gbc, r++, "Role", roleField);
        addFormRow(form, gbc, r++, "Password (for Add)", passField);

        JPanel buttons = new JPanel();
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(clearBtn);
        buttons.add(closeBtn);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(form, BorderLayout.CENTER);
        right.add(buttons, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(top, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(center, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);

        crudDialog.setContentPane(root);
        crudDialog.setVisible(true);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }
    
    private void crudAddUser() {
        String firstName = JOptionPane.showInputDialog(this, "First name:");
        if (firstName == null) return;
        String lastName = JOptionPane.showInputDialog(this, "Last name:");
        if (lastName == null) return;
        String email = JOptionPane.showInputDialog(this, "Email:");
        if (email == null) return;
        String password = JOptionPane.showInputDialog(this, "Password:");
        if (password == null) return;
        String role = JOptionPane.showInputDialog(this, "Role (admin/student):", "student");
        if (role == null) return;
        
        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());
        user.setPassword(password);
        user.setRole(role.trim());
        
        boolean ok = userDAO.registerUser(user);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to add user (check database/constraints).");
        }
        loadUsers();
    }
    
    private void crudEditSelected() {
        int viewRow = usermanagement.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a user row first.");
            return;
        }
        int modelRow = usermanagement.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) usermanagement.getModel();
        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        
        User existing = userDAO.getUserById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }
        
        String firstName = JOptionPane.showInputDialog(this, "First name:", existing.getFirstName());
        if (firstName == null) return;
        String lastName = JOptionPane.showInputDialog(this, "Last name:", existing.getLastName());
        if (lastName == null) return;
        String email = JOptionPane.showInputDialog(this, "Email:", existing.getEmail());
        if (email == null) return;
        String role = JOptionPane.showInputDialog(this, "Role:", existing.getRole());
        if (role == null) return;
        
        existing.setFirstName(firstName.trim());
        existing.setLastName(lastName.trim());
        existing.setEmail(email.trim());
        existing.setRole(role.trim());
        
        boolean ok = userDAO.updateUserAdmin(existing);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to update user.");
        }
        loadUsers();
    }
    
    private void crudDeleteSelected() {
        int viewRow = usermanagement.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a user row first.");
            return;
        }
        int modelRow = usermanagement.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) usermanagement.getModel();
        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        boolean ok = userDAO.deleteUserById(id);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to delete user.");
        }
        loadUsers();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panelRound1 = new onlineenrollment.main.PanelRound();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        panelRound2 = new onlineenrollment.main.PanelRound();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        Masterlist = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        panelRound3 = new onlineenrollment.main.PanelRound();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        usermanagement = new javax.swing.JTable();
        adminCRUD = new onlineenrollment.main.PanelRound();
        jLabel12 = new javax.swing.JLabel();
        searchBar = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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

        Masterlist.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        Masterlist.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Masterlist.setText("Masterlist");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Logout");

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
                    .addGroup(panelRound2Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(Masterlist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(18, 18, 18)
                .addComponent(Masterlist, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );

        jPanel1.add(panelRound2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 150, 460));

        panelRound3.setBackground(new java.awt.Color(255, 255, 255));
        panelRound3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelRound3.setRoundBottomLeft(15);
        panelRound3.setRoundBottomRight(15);
        panelRound3.setRoundTopLeft(15);
        panelRound3.setRoundTopRight(15);

        jLabel3.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        jLabel3.setText("User Management");

        usermanagement.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(usermanagement);

        adminCRUD.setRoundBottomLeft(8);
        adminCRUD.setRoundBottomRight(8);
        adminCRUD.setRoundTopLeft(8);
        adminCRUD.setRoundTopRight(8);

        jLabel12.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("EDIT");

        javax.swing.GroupLayout adminCRUDLayout = new javax.swing.GroupLayout(adminCRUD);
        adminCRUD.setLayout(adminCRUDLayout);
        adminCRUDLayout.setHorizontalGroup(
            adminCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(adminCRUDLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addContainerGap())
        );
        adminCRUDLayout.setVerticalGroup(
            adminCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel4.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jLabel4.setText("Search:");

        javax.swing.GroupLayout panelRound3Layout = new javax.swing.GroupLayout(panelRound3);
        panelRound3.setLayout(panelRound3Layout);
        panelRound3Layout.setHorizontalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelRound3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panelRound3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(adminCRUD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22))))
        );
        panelRound3Layout.setVerticalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRound3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(adminCRUD, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(searchBar, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(333, 333, 333))
        );

        jPanel1.add(panelRound3, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 80, 830, 450));

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
    private onlineenrollment.main.PanelRound adminCRUD;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private onlineenrollment.main.PanelRound panelRound1;
    private onlineenrollment.main.PanelRound panelRound2;
    private onlineenrollment.main.PanelRound panelRound3;
    private javax.swing.JTextField searchBar;
    private javax.swing.JTable usermanagement;
    // End of variables declaration//GEN-END:variables
}
