package gui;

import main.AppNavigator;
import main.login;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class programPage extends JPanel {
    private final String program;
    private final String defaultLevel;

    public programPage(String program, String defaultLevel) {
        this.program = program;
        this.defaultLevel = defaultLevel;
        initUi();
        ensureLoggedIn();
    }

    private void initUi() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel(program + " Page - Click Enroll to continue", JLabel.CENTER);
        JButton enrollBtn = new JButton("Enroll " + program);
        enrollBtn.addActionListener(e -> openEnrollment());
        add(label, BorderLayout.CENTER);
        add(enrollBtn, BorderLayout.SOUTH);
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

    private void openEnrollment() {
        AppNavigator.showProgramEnrollment(program, defaultLevel,
                () -> AppNavigator.showProgramPage(program, defaultLevel, () -> AppNavigator.showLanding(AppNavigator::showLogin)));
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }
}

