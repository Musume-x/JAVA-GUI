package gui;

import dao.EnrollmentDAO;
import main.AppNavigator;
import main.login;
import model.User;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class enrollmentStatusPage extends JPanel {
    private final EnrollmentDAO dao = new EnrollmentDAO();
    private JTable table;

    public enrollmentStatusPage() {
        initUi();
        ensureLoggedIn();
        loadStatuses();
    }

    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        table = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"Program", "Level", "Status", "Created At"}));
        add(new JScrollPane(table), BorderLayout.CENTER);
        JButton receiptsBtn = new JButton("Payment receipts & transactions…");
        receiptsBtn.addActionListener((ActionEvent e) -> openPaymentReceiptsWindow());
        add(receiptsBtn, BorderLayout.SOUTH);
    }

    private void openPaymentReceiptsWindow() {
        if (login.getCurrentUser() == null) {
            return;
        }
        AppNavigator.showPaymentReceiptsWindow(
                () -> AppNavigator.showEnrollmentStatus(() -> AppNavigator.showLanding(AppNavigator::showLogin)));
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

    private void loadStatuses() {
        User u = login.getCurrentUser();
        if (u == null) return;
        List<java.util.Map<String, Object>> rows = dao.getEnrollmentStatusByUser(u.getId());
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (java.util.Map<String, Object> r : rows) {
            model.addRow(new Object[]{
                r.get("program"),
                r.get("level"),
                r.get("status"),
                r.get("created_at")
            });
        }
    }
}

