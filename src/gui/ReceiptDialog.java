package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Simple official-style receipt view for a payment transaction row.
 */
public final class ReceiptDialog {

    private ReceiptDialog() {
    }

    public static String formatReceiptText(Map<String, Object> tx) {
        if (tx == null) {
            return "";
        }
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        double amt = 0;
        Object a = tx.get("amount");
        if (a instanceof Number) {
            amt = ((Number) a).doubleValue();
        }
        String receiptNo = nz(tx.get("receipt_no"));
        String when = nz(tx.get("created_at"));
        String payer = nz(tx.get("payer_name"));
        String uid = tx.get("user_id") != null ? tx.get("user_id").toString() : "";
        String purpose = nz(tx.get("purpose"));
        String method = nz(tx.get("payment_method"));
        String notes = nz(tx.get("notes"));
        String recorder = nz(tx.get("recorder_name"));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("   ST. CECILIA'S COLLEGE - CEBU, INC.\n");
        sb.append("        OFFICIAL PAYMENT RECEIPT\n");
        sb.append("========================================\n");
        sb.append("Receipt No.   ").append(receiptNo).append("\n");
        sb.append("Date / Time   ").append(when).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Payor         ").append(payer).append("\n");
        sb.append("User ID       ").append(uid).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Description   ").append(purpose.isEmpty() ? "(none)" : purpose).append("\n");
        sb.append("Amount        ").append(money.format(amt)).append("\n");
        sb.append("Payment       ").append(method.isEmpty() ? "(not specified)" : method).append("\n");
        if (!notes.isEmpty()) {
            sb.append("Notes         ").append(notes).append("\n");
        }
        sb.append("----------------------------------------\n");
        sb.append("Received by   ").append(recorder.isEmpty() ? "—" : recorder).append("\n");
        sb.append("========================================\n");
        sb.append("This document is system-generated for\n");
        sb.append("your enrollment / finance records.\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    private static String nz(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private static String safeReceiptFileBase(String receiptNo) {
        String base = nz(receiptNo).replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (base.isEmpty()) {
            base = "receipt";
        }
        return "Receipt-" + base;
    }

    public static void show(java.awt.Component parent, Map<String, Object> tx) {
        if (tx == null) {
            JOptionPane.showMessageDialog(parent, "No transaction data to display.", "Receipt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String receiptBody = formatReceiptText(tx);
        JTextArea area = new JTextArea(receiptBody);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setRows(22);
        area.setColumns(44);

        JButton copy = new JButton("Copy text");
        copy.addActionListener(e -> {
            area.selectAll();
            area.copy();
            JOptionPane.showMessageDialog(area, "Receipt text copied to clipboard.", "Receipt", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton download = new JButton("Download…");
        download.setToolTipText("Save this receipt as a text file on your computer");
        download.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save receipt");
            fc.setSelectedFile(new File(safeReceiptFileBase(nz(tx.get("receipt_no"))) + ".txt"));
            fc.setFileFilter(new FileNameExtensionFilter("Text receipt (.txt)", "txt"));
            if (fc.showSaveDialog(area) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File f = fc.getSelectedFile();
            String path = f.getAbsolutePath();
            if (!path.toLowerCase(Locale.ROOT).endsWith(".txt")) {
                f = new File(path + ".txt");
            }
            try {
                Files.write(f.toPath(), receiptBody.getBytes(StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(area,
                        "Receipt saved to:\n" + f.getAbsolutePath(),
                        "Download complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(area,
                        "Could not save the file:\n" + ex.getMessage(),
                        "Save failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton close = new JButton("Close");
        JPanel south = new JPanel();
        south.add(copy);
        south.add(download);
        south.add(close);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent),
                tx.get("receipt_no") != null ? "Receipt " + tx.get("receipt_no") : "Payment receipt",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dlg.getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
        dlg.getContentPane().add(south, BorderLayout.SOUTH);
        close.addActionListener(e -> dlg.dispose());
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }
}
