/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package admin;

import dao.EnrollmentDAO;
import dao.MasterlistDAO;
import main.AppNavigator;
import main.WindowFrames;
import main.login;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
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
    
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final MasterlistDAO masterlistDAO = new MasterlistDAO();
    private TableRowSorter<DefaultTableModel> studentSorter;
    private TableRowSorter<DefaultTableModel> courseSorter;
    private TableRowSorter<DefaultTableModel> subjectSorter;
    private TableRowSorter<DefaultTableModel> sectionSorter;
    private JDialog crudDialog;

    /**
     * Creates new form masterlist
     */
    public masterlist() {
        initComponents();
        ensureLoggedIn();
        initTableModels();
        loadEnrolleesMasterlist();
        loadSubjectsFromDb();
        loadCoursesFromDb();
        loadSectionsFromDb();
        setupSearchFiltering();
        addEventHandlers();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new main.login().setVisible(true);
        });
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
            new String[]{"App ID", "User ID", "Full Name", "Program", "Level", "Status", "Submitted"}
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
    
    private void loadEnrolleesMasterlist() {
        try {
            List<java.util.Map<String, Object>> rows = enrollmentDAO.getPendingAndConfirmedEnrollments();
            DefaultTableModel model = (DefaultTableModel) studentMasterlist.getModel();
            model.setRowCount(0);
            for (java.util.Map<String, Object> r : rows) {
                model.addRow(new Object[]{
                    r.get("id"),
                    r.get("user_id"),
                    r.get("full_name"),
                    r.get("program"),
                    r.get("level"),
                    r.get("status"),
                    r.get("created_at")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading enrollees in masterlist: " + e.getMessage());
        }
    }
    
    private void loadSubjectsFromDb() {
        try {
            List<java.util.Map<String, Object>> rows = masterlistDAO.getAllSubjects();
            DefaultTableModel model = (DefaultTableModel) subjectMasterlist.getModel();
            model.setRowCount(0);
            for (java.util.Map<String, Object> r : rows) {
                model.addRow(new Object[]{
                    r.get("id"),
                    r.get("subject_code"),
                    r.get("subject_name"),
                    r.get("units")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading subjects into table: " + e.getMessage());
        }
    }
    
    private void loadCoursesFromDb() {
        try {
            List<java.util.Map<String, Object>> rows = masterlistDAO.getAllCourses();
            DefaultTableModel model = (DefaultTableModel) courseMasterlist.getModel();
            model.setRowCount(0);
            for (java.util.Map<String, Object> r : rows) {
                model.addRow(new Object[]{
                    r.get("id"),
                    r.get("course_code"),
                    r.get("course_name"),
                    r.get("level")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading courses into table: " + e.getMessage());
        }
    }
    
    private void loadSectionsFromDb() {
        try {
            List<java.util.Map<String, Object>> rows = masterlistDAO.getAllSections();
            DefaultTableModel model = (DefaultTableModel) sectionMasterlist.getModel();
            model.setRowCount(0);
            for (java.util.Map<String, Object> r : rows) {
                model.addRow(new Object[]{
                    r.get("id"),
                    r.get("section_name"),
                    r.get("adviser"),
                    r.get("room")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading sections into table: " + e.getMessage());
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

        studentMasterlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleStudentEnrollmentReview();
                }
            }
        });
    }

    /**
     * Double-click a pending row on the Enrollees masterlist to approve or reject.
     */
    private void handleStudentEnrollmentReview() {
        if (!(studentMasterlist.getModel() instanceof DefaultTableModel)) {
            return;
        }
        DefaultTableModel m = (DefaultTableModel) studentMasterlist.getModel();
        if (m.getColumnCount() != 7 || !"App ID".equals(m.getColumnName(0)) || !"Status".equals(m.getColumnName(5))) {
            return;
        }
        int viewRow = studentMasterlist.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an enrollee row first.");
            return;
        }
        int row = studentMasterlist.convertRowIndexToModel(viewRow);
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

        if (choice == 0) {
            if (enrollmentDAO.updateApplicationStatus(applicationId, "confirmed")) {
                JOptionPane.showMessageDialog(this, "Application approved.");
                loadEnrolleesMasterlist();
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
                loadEnrolleesMasterlist();
            }
        }
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
        WindowFrames.setContentWithBack(frame, panel, AppNavigator::showMasterlist);
        frame.pack();
        frame.setSize(1020, 560);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
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
        crudDialog = new JDialog(owner, "Masterlist - CRUD", true);
        crudDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        crudDialog.setSize(new Dimension(980, 560));
        crudDialog.setLocationRelativeTo(owner);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Subjects", buildSubjectsCrudTab());
        tabs.addTab("Courses", buildCoursesCrudTab());
        tabs.addTab("Sections", buildSectionsCrudTab());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> crudDialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(closeBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(tabs, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        crudDialog.setContentPane(root);
        crudDialog.setVisible(true);
    }

    private JPanel buildSubjectsCrudTab() {
        // Table
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Subject Code", "Subject Name", "Units"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        javax.swing.JTable table = new javax.swing.JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(table);

        // Search
        JTextField search = new JTextField();
        hookFilter(search, sorter);

        // Form
        JTextField id = new JTextField(); id.setEnabled(false);
        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField units = new JTextField();

        Consumer<Void> reload = (v) -> {
            try {
                List<java.util.Map<String, Object>> rows = masterlistDAO.getAllSubjects();
                model.setRowCount(0);
                for (java.util.Map<String, Object> r : rows) {
                    model.addRow(new Object[]{r.get("id"), r.get("subject_code"), r.get("subject_name"), r.get("units")});
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(crudDialog, "Failed to load subjects.");
            }
        };
        reload.accept(null);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int mr = table.convertRowIndexToModel(viewRow);
            id.setText(String.valueOf(model.getValueAt(mr, 0)));
            code.setText(String.valueOf(model.getValueAt(mr, 1)));
            name.setText(String.valueOf(model.getValueAt(mr, 2)));
            units.setText(String.valueOf(model.getValueAt(mr, 3)));
        });

        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        JButton clear = new JButton("Clear");

        Runnable clearForm = () -> { id.setText(""); code.setText(""); name.setText(""); units.setText(""); table.clearSelection(); };

        add.addActionListener(e -> {
            String c = code.getText().trim();
            String n = name.getText().trim();
            String u = units.getText().trim();
            if (c.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(crudDialog, "Fill subject code and name.");
                return;
            }
            int uVal = 0;
            if (!u.isEmpty()) {
                try { uVal = Integer.parseInt(u); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Units must be a number."); return; }
            }
            boolean ok = masterlistDAO.addSubject(c, n, uVal);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Add failed (code may already exist).");
            reload.accept(null);
            clearForm.run();
            loadSubjectsFromDb();
        });

        update.addActionListener(e -> {
            if (id.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Select a subject row."); return; }
            int idVal;
            try { idVal = Integer.parseInt(id.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Invalid ID."); return; }
            String c = code.getText().trim();
            String n = name.getText().trim();
            String u = units.getText().trim();
            if (c.isEmpty() || n.isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Fill subject code and name."); return; }
            int uVal = 0;
            if (!u.isEmpty()) {
                try { uVal = Integer.parseInt(u); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Units must be a number."); return; }
            }
            boolean ok = masterlistDAO.updateSubject(idVal, c, n, uVal);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Update failed.");
            reload.accept(null);
            loadSubjectsFromDb();
        });

        delete.addActionListener(e -> {
            if (id.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Select a subject row."); return; }
            int confirm = JOptionPane.showConfirmDialog(crudDialog, "Delete selected subject?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int idVal;
            try { idVal = Integer.parseInt(id.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Invalid ID."); return; }
            boolean ok = masterlistDAO.deleteSubject(idVal);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Delete failed.");
            reload.accept(null);
            clearForm.run();
            loadSubjectsFromDb();
        });

        refresh.addActionListener(e -> { reload.accept(null); });
        clear.addActionListener(e -> { clearForm.run(); });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int r = 0;
        addFormRow(form, gbc, r++, "ID", id);
        addFormRow(form, gbc, r++, "Subject Code", code);
        addFormRow(form, gbc, r++, "Subject Name", name);
        addFormRow(form, gbc, r++, "Units", units);

        JPanel btns = new JPanel();
        btns.add(add); btns.add(update); btns.add(delete); btns.add(refresh); btns.add(clear);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(form, BorderLayout.CENTER);
        right.add(btns, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(top, BorderLayout.NORTH);
        left.add(scroll, BorderLayout.CENTER);

        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.add(left, BorderLayout.CENTER);
        tab.add(right, BorderLayout.EAST);
        return tab;
    }

    private JPanel buildCoursesCrudTab() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Course Code", "Course Name", "Level"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        javax.swing.JTable table = new javax.swing.JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(table);

        JTextField search = new JTextField();
        hookFilter(search, sorter);

        JTextField id = new JTextField(); id.setEnabled(false);
        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField level = new JTextField();

        Runnable reload = () -> {
            try {
                List<java.util.Map<String, Object>> rows = masterlistDAO.getAllCourses();
                model.setRowCount(0);
                for (java.util.Map<String, Object> r : rows) {
                    model.addRow(new Object[]{r.get("id"), r.get("course_code"), r.get("course_name"), r.get("level")});
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(crudDialog, "Failed to load courses.");
            }
        };
        reload.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int mr = table.convertRowIndexToModel(viewRow);
            id.setText(String.valueOf(model.getValueAt(mr, 0)));
            code.setText(String.valueOf(model.getValueAt(mr, 1)));
            name.setText(String.valueOf(model.getValueAt(mr, 2)));
            level.setText(String.valueOf(model.getValueAt(mr, 3)));
        });

        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        JButton clear = new JButton("Clear");

        Runnable clearForm = () -> { id.setText(""); code.setText(""); name.setText(""); level.setText(""); table.clearSelection(); };

        add.addActionListener(e -> {
            String c = code.getText().trim();
            String n = name.getText().trim();
            String l = level.getText().trim();
            if (c.isEmpty() || n.isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Fill course code and name."); return; }
            boolean ok = masterlistDAO.addCourse(c, n, l);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Add failed (code may already exist).");
            reload.run(); clearForm.run(); loadCoursesFromDb();
        });
        update.addActionListener(e -> {
            if (id.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Select a course row."); return; }
            int idVal;
            try { idVal = Integer.parseInt(id.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Invalid ID."); return; }
            String c = code.getText().trim();
            String n = name.getText().trim();
            String l = level.getText().trim();
            if (c.isEmpty() || n.isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Fill course code and name."); return; }
            boolean ok = masterlistDAO.updateCourse(idVal, c, n, l);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Update failed.");
            reload.run(); loadCoursesFromDb();
        });
        delete.addActionListener(e -> {
            if (id.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Select a course row."); return; }
            int confirm = JOptionPane.showConfirmDialog(crudDialog, "Delete selected course?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int idVal;
            try { idVal = Integer.parseInt(id.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Invalid ID."); return; }
            boolean ok = masterlistDAO.deleteCourse(idVal);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Delete failed.");
            reload.run(); clearForm.run(); loadCoursesFromDb();
        });
        refresh.addActionListener(e -> reload.run());
        clear.addActionListener(e -> clearForm.run());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int r = 0;
        addFormRow(form, gbc, r++, "ID", id);
        addFormRow(form, gbc, r++, "Course Code", code);
        addFormRow(form, gbc, r++, "Course Name", name);
        addFormRow(form, gbc, r++, "Level", level);

        JPanel btns = new JPanel();
        btns.add(add); btns.add(update); btns.add(delete); btns.add(refresh); btns.add(clear);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(form, BorderLayout.CENTER);
        right.add(btns, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(top, BorderLayout.NORTH);
        left.add(scroll, BorderLayout.CENTER);

        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.add(left, BorderLayout.CENTER);
        tab.add(right, BorderLayout.EAST);
        return tab;
    }

    private JPanel buildSectionsCrudTab() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "Section", "Adviser", "Room"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        javax.swing.JTable table = new javax.swing.JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JScrollPane scroll = new JScrollPane(table);

        JTextField search = new JTextField();
        hookFilter(search, sorter);

        JTextField id = new JTextField(); id.setEnabled(false);
        JTextField section = new JTextField();
        JTextField adviser = new JTextField();
        JTextField room = new JTextField();

        Runnable reload = () -> {
            try {
                List<java.util.Map<String, Object>> rows = masterlistDAO.getAllSections();
                model.setRowCount(0);
                for (java.util.Map<String, Object> r : rows) {
                    model.addRow(new Object[]{r.get("id"), r.get("section_name"), r.get("adviser"), r.get("room")});
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(crudDialog, "Failed to load sections.");
            }
        };
        reload.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int mr = table.convertRowIndexToModel(viewRow);
            id.setText(String.valueOf(model.getValueAt(mr, 0)));
            section.setText(String.valueOf(model.getValueAt(mr, 1)));
            adviser.setText(String.valueOf(model.getValueAt(mr, 2)));
            room.setText(String.valueOf(model.getValueAt(mr, 3)));
        });

        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        JButton clear = new JButton("Clear");

        Runnable clearForm = () -> { id.setText(""); section.setText(""); adviser.setText(""); room.setText(""); table.clearSelection(); };

        add.addActionListener(e -> {
            String s = section.getText().trim();
            if (s.isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Fill section name."); return; }
            boolean ok = masterlistDAO.addSection(s, adviser.getText().trim(), room.getText().trim());
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Add failed (section may already exist).");
            reload.run(); clearForm.run(); loadSectionsFromDb();
        });
        update.addActionListener(e -> {
            if (id.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Select a section row."); return; }
            int idVal;
            try { idVal = Integer.parseInt(id.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Invalid ID."); return; }
            String s = section.getText().trim();
            if (s.isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Fill section name."); return; }
            boolean ok = masterlistDAO.updateSection(idVal, s, adviser.getText().trim(), room.getText().trim());
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Update failed.");
            reload.run(); loadSectionsFromDb();
        });
        delete.addActionListener(e -> {
            if (id.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(crudDialog, "Select a section row."); return; }
            int confirm = JOptionPane.showConfirmDialog(crudDialog, "Delete selected section?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int idVal;
            try { idVal = Integer.parseInt(id.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(crudDialog, "Invalid ID."); return; }
            boolean ok = masterlistDAO.deleteSection(idVal);
            if (!ok) JOptionPane.showMessageDialog(crudDialog, "Delete failed.");
            reload.run(); clearForm.run(); loadSectionsFromDb();
        });
        refresh.addActionListener(e -> reload.run());
        clear.addActionListener(e -> clearForm.run());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        int r = 0;
        addFormRow(form, gbc, r++, "ID", id);
        addFormRow(form, gbc, r++, "Section", section);
        addFormRow(form, gbc, r++, "Adviser", adviser);
        addFormRow(form, gbc, r++, "Room", room);

        JPanel btns = new JPanel();
        btns.add(add); btns.add(update); btns.add(delete); btns.add(refresh); btns.add(clear);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(form, BorderLayout.CENTER);
        right.add(btns, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(top, BorderLayout.NORTH);
        left.add(scroll, BorderLayout.CENTER);

        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.add(left, BorderLayout.CENTER);
        tab.add(right, BorderLayout.EAST);
        return tab;
    }

    private void hookFilter(JTextField field, TableRowSorter<DefaultTableModel> sorter) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
            private void apply() {
                String t = field.getText();
                if (t == null || t.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t.trim())));
                }
            }
        });
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

    private void handleSubjectCrud(int action) {
        DefaultTableModel model = (DefaultTableModel) subjectMasterlist.getModel();
        if (action == 0) {
            String code = JOptionPane.showInputDialog(this, "Subject code:");
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Subject name:");
            if (name == null) return;
            String unitsStr = JOptionPane.showInputDialog(this, "Units (number):", "0");
            if (unitsStr == null) return;
            int units;
            try {
                units = Integer.parseInt(unitsStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid units value.");
                return;
            }
            masterlistDAO.addSubject(code.trim(), name.trim(), units);
            loadSubjectsFromDb();
        } else if (action == 1) {
            int viewRow = subjectMasterlist.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a subject row first.");
                return;
            }
            int modelRow = subjectMasterlist.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
            String curCode = String.valueOf(model.getValueAt(modelRow, 1));
            String curName = String.valueOf(model.getValueAt(modelRow, 2));
            String curUnits = String.valueOf(model.getValueAt(modelRow, 3));
            
            String code = JOptionPane.showInputDialog(this, "Subject code:", curCode);
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Subject name:", curName);
            if (name == null) return;
            String unitsStr = JOptionPane.showInputDialog(this, "Units (number):", curUnits);
            if (unitsStr == null) return;
            int units;
            try {
                units = Integer.parseInt(unitsStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid units value.");
                return;
            }
            masterlistDAO.updateSubject(id, code.trim(), name.trim(), units);
            loadSubjectsFromDb();
        } else if (action == 2) {
            int viewRow = subjectMasterlist.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a subject row first.");
                return;
            }
            int modelRow = subjectMasterlist.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected subject?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            masterlistDAO.deleteSubject(id);
            loadSubjectsFromDb();
        }
    }
    
    private void handleCourseCrud(int action) {
        DefaultTableModel model = (DefaultTableModel) courseMasterlist.getModel();
        if (action == 0) {
            String code = JOptionPane.showInputDialog(this, "Course code:");
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Course name:");
            if (name == null) return;
            String level = JOptionPane.showInputDialog(this, "Level (e.g. SHS, College):");
            if (level == null) return;
            masterlistDAO.addCourse(code.trim(), name.trim(), level.trim());
            loadCoursesFromDb();
        } else if (action == 1) {
            int viewRow = courseMasterlist.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a course row first.");
                return;
            }
            int modelRow = courseMasterlist.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
            String curCode = String.valueOf(model.getValueAt(modelRow, 1));
            String curName = String.valueOf(model.getValueAt(modelRow, 2));
            String curLevel = String.valueOf(model.getValueAt(modelRow, 3));
            
            String code = JOptionPane.showInputDialog(this, "Course code:", curCode);
            if (code == null) return;
            String name = JOptionPane.showInputDialog(this, "Course name:", curName);
            if (name == null) return;
            String level = JOptionPane.showInputDialog(this, "Level:", curLevel);
            if (level == null) return;
            masterlistDAO.updateCourse(id, code.trim(), name.trim(), level.trim());
            loadCoursesFromDb();
        } else if (action == 2) {
            int viewRow = courseMasterlist.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a course row first.");
                return;
            }
            int modelRow = courseMasterlist.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected course?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            masterlistDAO.deleteCourse(id);
            loadCoursesFromDb();
        }
    }
    
    private void handleSectionCrud(int action) {
        DefaultTableModel model = (DefaultTableModel) sectionMasterlist.getModel();
        if (action == 0) {
            String name = JOptionPane.showInputDialog(this, "Section name:");
            if (name == null) return;
            String adviser = JOptionPane.showInputDialog(this, "Adviser:");
            if (adviser == null) adviser = "";
            String room = JOptionPane.showInputDialog(this, "Room:");
            if (room == null) room = "";
            masterlistDAO.addSection(name.trim(), adviser.trim(), room.trim());
            loadSectionsFromDb();
        } else if (action == 1) {
            int viewRow = sectionMasterlist.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a section row first.");
                return;
            }
            int modelRow = sectionMasterlist.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
            String curName = String.valueOf(model.getValueAt(modelRow, 1));
            String curAdviser = String.valueOf(model.getValueAt(modelRow, 2));
            String curRoom = String.valueOf(model.getValueAt(modelRow, 3));
            
            String name = JOptionPane.showInputDialog(this, "Section name:", curName);
            if (name == null) return;
            String adviser = JOptionPane.showInputDialog(this, "Adviser:", curAdviser);
            if (adviser == null) adviser = "";
            String room = JOptionPane.showInputDialog(this, "Room:", curRoom);
            if (room == null) room = "";
            masterlistDAO.updateSection(id, name.trim(), adviser.trim(), room.trim());
            loadSectionsFromDb();
        } else if (action == 2) {
            int viewRow = sectionMasterlist.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a section row first.");
                return;
            }
            int modelRow = sectionMasterlist.convertRowIndexToModel(viewRow);
            int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected section?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            masterlistDAO.deleteSection(id);
            loadSectionsFromDb();
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
