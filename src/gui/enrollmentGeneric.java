package gui;

import dao.EnrollmentDAO;
import main.AppNavigator;
import main.login;
import model.User;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class enrollmentGeneric extends JPanel {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final String program;
    private final String defaultLevel;

    private JTextField fullNameField;
    private JTextField birthdateField;
    private JTextField contactField;
    private JTextField levelField;

    public enrollmentGeneric(String program, String defaultLevel) {
        this.program = program;
        this.defaultLevel = defaultLevel;
        initUi();
        ensureLoggedIn();
        prefillFromUser();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        fullNameField = new JTextField();
        birthdateField = new JTextField();
        contactField = new JTextField();
        levelField = new JTextField(defaultLevel);
        JButton submit = new JButton("Submit " + program + " Enrollment");

        int r = 0;
        addRow(form, gbc, r++, "Full Name", fullNameField);
        addRow(form, gbc, r++, "Birthdate", birthdateField);
        addRow(form, gbc, r++, "Contact No", contactField);
        addRow(form, gbc, r++, "Level / Track", levelField);

        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.gridwidth = 2;
        form.add(submit, gbc);

        submit.addActionListener(e -> handleSubmit());
        add(form, BorderLayout.CENTER);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void ensureLoggedIn() {
        if (login.getCurrentUser() == null) {
            SwingUtilities.invokeLater(() -> {
                new login().setVisible(true);
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.dispose();
            });
        }
    }

    private void prefillFromUser() {
        User u = login.getCurrentUser();
        if (u == null) return;
        fullNameField.setText(u.getFullName());
        birthdateField.setText(u.getBirthdate() != null ? u.getBirthdate() : "");
        contactField.setText(u.getContact() != null ? u.getContact() : "");
    }

    private void handleSubmit() {
        User u = login.getCurrentUser();
        if (u == null) {
            ensureLoggedIn();
            return;
        }
        String full = fullNameField.getText().trim();
        String birth = birthdateField.getText().trim();
        String contact = contactField.getText().trim();
        String level = levelField.getText().trim();
        if (full.isEmpty() || level.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name and level are required.");
            return;
        }
        boolean ok = enrollmentDAO.submitEnrollment(u.getId(), program, full, birth, contact, level);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to submit enrollment.");
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Enrollment submitted. Status is pending until an administrator approves your application.");
        AppNavigator.showLanding(AppNavigator::showLogin);
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }
}

