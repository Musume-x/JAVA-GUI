/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import dao.UserDAO;
import dao.MasterlistDAO;
import main.login;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.regex.Pattern;
import java.util.List;

/**
 *
 * @author miwa
 */
public class masterlist extends javax.swing.JPanel {
    
    private final UserDAO userDAO = new UserDAO();
    private final MasterlistDAO masterlistDAO = new MasterlistDAO();
    private TableRowSorter<DefaultTableModel> studentSorter;
    private TableRowSorter<DefaultTableModel> courseSorter;
    private TableRowSorter<DefaultTableModel> subjectSorter;
    private TableRowSorter<DefaultTableModel> sectionSorter;

    /**
     * Creates new form masterlist
     */
    public masterlist() {
        initComponents();
        ensureLoggedIn();
        initTableModels();
        loadStudentsOnly();
        loadSubjects();
        loadCourses();
        loadSections();
        setupSearchFiltering();
        addEventHandlers();
    }

    private void ensureLoggedIn() {
        if (login.getCurrentUser() == null) {
            login log = new login();
            log.setVisible(true);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    private void initTableModels() {
        DefaultTableModel students = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "First Name", "Last Name", "Email", "Role", "Created At"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentMasterlist.setModel(students);
        studentSorter = new TableRowSorter<>(students);
        studentMasterlist.setRowSorter(studentSorter);

        DefaultTableModel courses = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Course Code", "Course Name", "Level"}
        );
        courseMasterlist.setModel(courses);
        courseSorter = new TableRowSorter<>(courses);
        courseMasterlist.setRowSorter(courseSorter);

        DefaultTableModel subjects = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Subject Code", "Subject Name", "Units"}
        );
        subjectMasterlist.setModel(subjects);
        subjectSorter = new TableRowSorter<>(subjects);
        subjectMasterlist.setRowSorter(subjectSorter);

        DefaultTableModel sections = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Section", "Adviser", "Room"}
        );
        sectionMasterlist.setModel(sections);
        sectionSorter = new TableRowSorter<>(sections);
        sectionMasterlist.setRowSorter(sectionSorter);
    }
    
    private void loadSubjects() {
        DefaultTableModel model = (DefaultTableModel) subjectMasterlist.getModel();
        model.setRowCount(0);
        for (java.util.Map<String, Object> r : masterlistDAO.getAllSubjects()) {
            model.addRow(new Object[]{
                r.get("id"),
                r.get("subject_code"),
                r.get("subject_name"),
                r.get("units")
            });
        }
    }
    
    private void loadCourses() {
        DefaultTableModel model = (DefaultTableModel) courseMasterlist.getModel();
        model.setRowCount(0);
        for (java.util.Map<String, Object> r : masterlistDAO.getAllCourses()) {
            model.addRow(new Object[]{
                r.get("id"),
                r.get("course_code"),
                r.get("course_name"),
                r.get("level")
            });
        }
    }
    
    private void loadSections() {
        DefaultTableModel model = (DefaultTableModel) sectionMasterlist.getModel();
        model.setRowCount(0);
        for (java.util.Map<String, Object> r : masterlistDAO.getAllSections()) {
            model.addRow(new Object[]{
                r.get("id"),
                r.get("section_name"),
                r.get("adviser"),
                r.get("room")
            });
        }
    }
    
    private void loadStudentsOnly() {
        try {
            List<java.util.Map<String, Object>> users = userDAO.getAllUsers();
            DefaultTableModel model = (DefaultTableModel) studentMasterlist.getModel();
            model.setRowCount(0);
            
            for (java.util.Map<String, Object> u : users) {
                Object roleObj = u.get("role");
                String role = roleObj != null ? roleObj.toString() : "";
                if (!"student".equalsIgnoreCase(role)) {
                    continue;
                }
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
            System.err.println("Error loading students in masterlist: " + e.getMessage());
        }
    }
    
    private void setupSearchFiltering() {
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
    }
    
    private void applyFilter() {
        String text = searchBar.getText();
        if (text == null || text.trim().isEmpty()) {
            studentSorter.setRowFilter(null);
            courseSorter.setRowFilter(null);
            subjectSorter.setRowFilter(null);
            sectionSorter.setRowFilter(null);
            return;
        }
        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + Pattern.quote(text.trim()));
        studentSorter.setRowFilter(rf);
        courseSorter.setRowFilter(rf);
        subjectSorter.setRowFilter(rf);
        sectionSorter.setRowFilter(rf);
    }
    
    private void addEventHandlers() {
        // Sidebar navigation (panelRound3)
        jLabel8.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDashboard();
            }
        });
        jLabel12.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDashboardAndShowEnrollments();
            }
        });
        jLabel13.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDashboardAndShowFinancials();
            }
        });
        jLabel14.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUserManagement();
            }
        });
        // Masterlist1: current screen
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                performLogout();
            }
        });

        // CRUD button (top bar)
        editCRUD.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCrudDialog();
            }
        });
        jLabel3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCrudDialog();
            }
        });
    }
    
    private void openDashboard() {
        adminDashPane pane = new adminDashPane();
        openPanelInFrame(pane, "Admin Dashboard");
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
    
    private void openUserManagement() {
        userManagement panel = new userManagement();
        openPanelInFrame(panel, "User Management");
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
        String[] tables = {"Subject Masterlist", "Course Masterlist", "Section Masterlist"};
        int tableChoice = JOptionPane.showOptionDialog(
            this,
            "Select a masterlist",
            "Edit Masterlist",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            tables,
            tables[0]
        );
        if (tableChoice < 0) return;
        
        String[] actions = {"Add", "Edit Selected", "Delete Selected"};
        int action = JOptionPane.showOptionDialog(
            this,
            "Select an action",
            "Edit Masterlist",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            actions,
            actions[0]
        );
        if (action < 0) return;
        
        if (tableChoice == 0) {
            crudSubjects(action);
        } else if (tableChoice == 1) {
            crudCourses(action);
        } else if (tableChoice == 2) {
            crudSections(action);
        }
    }
    
    private void crudSubjects(int action) {
        if (action == 0) {
            String code = JOptionPane.showInputDialog(this, "Subject Code:");
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Subject Name:");
            if (name == null) return;
            String unitsStr = JOptionPane.showInputDialog(this, "Units:", "0");
            if (unitsStr == null) return;
            int units = 0;
            try { units = Integer.parseInt(unitsStr.trim()); } catch (Exception e) {}
            if (!masterlistDAO.addSubject(code.trim(), name.trim(), units)) {
                JOptionPane.showMessageDialog(this, "Failed to add subject.");
            }
            loadSubjects();
            return;
        }
        
        int viewRow = subjectMasterlist.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a subject row first.");
            return;
        }
        int modelRow = subjectMasterlist.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) subjectMasterlist.getModel();
        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        
        if (action == 1) {
            String code = JOptionPane.showInputDialog(this, "Subject Code:", model.getValueAt(modelRow, 1));
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Subject Name:", model.getValueAt(modelRow, 2));
            if (name == null) return;
            String unitsStr = JOptionPane.showInputDialog(this, "Units:", model.getValueAt(modelRow, 3));
            if (unitsStr == null) return;
            int units = 0;
            try { units = Integer.parseInt(unitsStr.trim()); } catch (Exception e) {}
            if (!masterlistDAO.updateSubject(id, code.trim(), name.trim(), units)) {
                JOptionPane.showMessageDialog(this, "Failed to update subject.");
            }
            loadSubjects();
        } else if (action == 2) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected subject?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            if (!masterlistDAO.deleteSubject(id)) {
                JOptionPane.showMessageDialog(this, "Failed to delete subject.");
            }
            loadSubjects();
        }
    }
    
    private void crudCourses(int action) {
        if (action == 0) {
            String code = JOptionPane.showInputDialog(this, "Course Code:");
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Course Name:");
            if (name == null) return;
            String level = JOptionPane.showInputDialog(this, "Level:", "");
            if (level == null) return;
            if (!masterlistDAO.addCourse(code.trim(), name.trim(), level.trim())) {
                JOptionPane.showMessageDialog(this, "Failed to add course.");
            }
            loadCourses();
            return;
        }
        
        int viewRow = courseMasterlist.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a course row first.");
            return;
        }
        int modelRow = courseMasterlist.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) courseMasterlist.getModel();
        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        
        if (action == 1) {
            String code = JOptionPane.showInputDialog(this, "Course Code:", model.getValueAt(modelRow, 1));
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Course Name:", model.getValueAt(modelRow, 2));
            if (name == null) return;
            String level = JOptionPane.showInputDialog(this, "Level:", model.getValueAt(modelRow, 3));
            if (level == null) return;
            if (!masterlistDAO.updateCourse(id, code.trim(), name.trim(), level.trim())) {
                JOptionPane.showMessageDialog(this, "Failed to update course.");
            }
            loadCourses();
        } else if (action == 2) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected course?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            if (!masterlistDAO.deleteCourse(id)) {
                JOptionPane.showMessageDialog(this, "Failed to delete course.");
            }
            loadCourses();
        }
    }
    
    private void crudSections(int action) {
        if (action == 0) {
            String name = JOptionPane.showInputDialog(this, "Section Name:");
            if (name == null) return;
            String adviser = JOptionPane.showInputDialog(this, "Adviser:", "");
            if (adviser == null) return;
            String room = JOptionPane.showInputDialog(this, "Room:", "");
            if (room == null) return;
            if (!masterlistDAO.addSection(name.trim(), adviser.trim(), room.trim())) {
                JOptionPane.showMessageDialog(this, "Failed to add section.");
            }
            loadSections();
            return;
        }
        
        int viewRow = sectionMasterlist.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a section row first.");
            return;
        }
        int modelRow = sectionMasterlist.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) sectionMasterlist.getModel();
        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        
        if (action == 1) {
            String name = JOptionPane.showInputDialog(this, "Section Name:", model.getValueAt(modelRow, 1));
            if (name == null) return;
            String adviser = JOptionPane.showInputDialog(this, "Adviser:", model.getValueAt(modelRow, 2));
            if (adviser == null) return;
            String room = JOptionPane.showInputDialog(this, "Room:", model.getValueAt(modelRow, 3));
            if (room == null) return;
            if (!masterlistDAO.updateSection(id, name.trim(), adviser.trim(), room.trim())) {
                JOptionPane.showMessageDialog(this, "Failed to update section.");
            }
            loadSections();
        } else if (action == 2) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected section?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            if (!masterlistDAO.deleteSection(id)) {
                JOptionPane.showMessageDialog(this, "Failed to delete section.");
            }
            loadSections();
        }
    }
    
    private javax.swing.JTable getTableByChoice(int tableChoice) {
        switch (tableChoice) {
            case 0:
                return subjectMasterlist;
            case 1:
                return courseMasterlist;
            case 2:
                return sectionMasterlist;
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelRound2 = new onlineenrollment.main.PanelRound();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        Masterlist = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        panelRound1 = new onlineenrollment.main.PanelRound();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        editCRUD = new onlineenrollment.main.PanelRound();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        searchBar = new javax.swing.JTextField();
        panelRound3 = new onlineenrollment.main.PanelRound();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        Masterlist1 = new javax.swing.JLabel();
        logout = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        studentMasterlist = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        courseMasterlist = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        subjectMasterlist = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        sectionMasterlist = new javax.swing.JTable();

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

        panelRound1.setBackground(new java.awt.Color(255, 255, 255));
        panelRound1.setRoundBottomLeft(30);
        panelRound1.setRoundBottomRight(30);
        panelRound1.setRoundTopLeft(30);
        panelRound1.setRoundTopRight(30);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/scc-logo.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("ST. CECILIA'S COLLEGE - CEBU, INC.");

        jLabel3.setText("EDIT");

        javax.swing.GroupLayout editCRUDLayout = new javax.swing.GroupLayout(editCRUD);
        editCRUD.setLayout(editCRUDLayout);
        editCRUDLayout.setHorizontalGroup(
            editCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editCRUDLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addContainerGap())
        );
        editCRUDLayout.setVerticalGroup(
            editCRUDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel4.setText("search");

        javax.swing.GroupLayout panelRound1Layout = new javax.swing.GroupLayout(panelRound1);
        panelRound1.setLayout(panelRound1Layout);
        panelRound1Layout.setHorizontalGroup(
            panelRound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(213, 213, 213)
                .addComponent(editCRUD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );
        panelRound1Layout.setVerticalGroup(
            panelRound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelRound1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelRound1Layout.createSequentialGroup()
                        .addGroup(panelRound1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(editCRUD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchBar, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                        .addGap(0, 5, Short.MAX_VALUE)))
                .addContainerGap())
        );

        panelRound3.setBackground(new java.awt.Color(255, 255, 255));
        panelRound3.setRoundBottomLeft(8);
        panelRound3.setRoundBottomRight(8);
        panelRound3.setRoundTopLeft(8);
        panelRound3.setRoundTopRight(8);

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Dashboard");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Enrollment Management");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Financials");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("User Management");

        Masterlist1.setFont(new java.awt.Font("Trebuchet MS", 0, 18)); // NOI18N
        Masterlist1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Masterlist1.setText("Masterlist");

        logout.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        logout.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logout.setText("Logout");

        javax.swing.GroupLayout panelRound3Layout = new javax.swing.GroupLayout(panelRound3);
        panelRound3.setLayout(panelRound3Layout);
        panelRound3Layout.setHorizontalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelRound3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Masterlist1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelRound3Layout.createSequentialGroup()
                        .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 9, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelRound3Layout.setVerticalGroup(
            panelRound3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRound3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(Masterlist1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );

        studentMasterlist.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(studentMasterlist);

        courseMasterlist.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(courseMasterlist);

        subjectMasterlist.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(subjectMasterlist);

        sectionMasterlist.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(sectionMasterlist);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelRound1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelRound3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelRound1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(panelRound3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(19, 19, 19))))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Masterlist;
    private javax.swing.JLabel Masterlist1;
    private javax.swing.JTable courseMasterlist;
    private onlineenrollment.main.PanelRound editCRUD;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel logout;
    private onlineenrollment.main.PanelRound panelRound1;
    private onlineenrollment.main.PanelRound panelRound2;
    private onlineenrollment.main.PanelRound panelRound3;
    private javax.swing.JTextField searchBar;
    private javax.swing.JTable sectionMasterlist;
    private javax.swing.JTable studentMasterlist;
    private javax.swing.JTable subjectMasterlist;
    // End of variables declaration//GEN-END:variables
}
