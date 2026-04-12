package gui;

import dao.PaymentTransactionDAO;
import main.login;
import model.User;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Student view: list own payments and open receipt on double-click.
 */
public class paymentReceiptsPage extends JPanel {

    private final PaymentTransactionDAO dao = new PaymentTransactionDAO();
    private JTable table;

    public paymentReceiptsPage() {
        initUi();
        ensureLoggedIn();
        reload();
    }

    private void initUi() {
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("<html><b>Payment receipts</b> — double-click a row to view your official receipt.</html>"),
                BorderLayout.NORTH);
        table = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{
            "Receipt No.", "Amount", "Purpose", "Method", "Date"
        }) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedReceipt();
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void ensureLoggedIn() {
        if (login.getCurrentUser() == null) {
            SwingUtilities.invokeLater(() -> {
                new main.login().setVisible(true);
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    frame.dispose();
                }
            });
        }
    }

    private void reload() {
        User u = login.getCurrentUser();
        if (u == null) {
            return;
        }
        List<Map<String, Object>> rows = dao.findByUserId(u.getId());
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        for (Map<String, Object> r : rows) {
            double amt = r.get("amount") instanceof Number ? ((Number) r.get("amount")).doubleValue() : 0;
            model.addRow(new Object[]{
                r.get("receipt_no"),
                money.format(amt),
                r.get("purpose"),
                r.get("payment_method"),
                r.get("created_at")
            });
        }
    }

    private void openSelectedReceipt() {
        int vr = table.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(this, "Select a receipt row first.");
            return;
        }
        int row = table.convertRowIndexToModel(vr);
        Object rno = ((DefaultTableModel) table.getModel()).getValueAt(row, 0);
        if (rno == null) {
            return;
        }
        Map<String, Object> tx = dao.findByReceiptNo(rno.toString());
        ReceiptDialog.show(this, tx);
    }
}
