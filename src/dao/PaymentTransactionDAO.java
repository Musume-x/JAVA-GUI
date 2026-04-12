package dao;

import config.config;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Stored payment rows used for official receipts (admin records a transaction).
 */
public class PaymentTransactionDAO {

    private static final String DETAIL_SQL =
            "SELECT pt.id, pt.receipt_no, pt.user_id, pt.amount, pt.purpose, pt.payment_method, pt.notes, "
            + "pt.recorded_by_user_id, pt.created_at, "
            + "TRIM(COALESCE(u.first_name,'') || ' ' || COALESCE(u.last_name,'')) AS payer_name, "
            + "TRIM(COALESCE(ru.first_name,'') || ' ' || COALESCE(ru.last_name,'')) AS recorder_name "
            + "FROM payment_transactions pt "
            + "JOIN users u ON u.id = pt.user_id "
            + "LEFT JOIN users ru ON ru.id = pt.recorded_by_user_id ";

    public List<Map<String, Object>> findAllWithPayer() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = DETAIL_SQL + "ORDER BY pt.created_at DESC, pt.id DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing payment transactions: " + e.getMessage());
        }
        return list;
    }

    public List<Map<String, Object>> findByUserId(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = DETAIL_SQL + "WHERE pt.user_id = ? ORDER BY pt.created_at DESC, pt.id DESC";
        try (Connection conn = config.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing user payment transactions: " + e.getMessage());
        }
        return list;
    }

    public Map<String, Object> findById(int id) {
        String sql = DETAIL_SQL + "WHERE pt.id = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading payment transaction: " + e.getMessage());
        }
        return null;
    }

    public Map<String, Object> findByReceiptNo(String receiptNo) {
        if (receiptNo == null || receiptNo.trim().isEmpty()) {
            return null;
        }
        String sql = DETAIL_SQL + "WHERE pt.receipt_no = ?";
        try (Connection conn = config.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading payment by receipt: " + e.getMessage());
        }
        return null;
    }

    public double sumTotalAmount() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payment_transactions";
        try (Connection conn = config.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error summing payments: " + e.getMessage());
        }
        return 0;
    }

    /**
     * @return new row id, or -1 on failure
     */
    public int insert(int userId, double amount, String purpose, String paymentMethod, String notes,
            Integer recordedByUserId) {
        if (userId <= 0 || amount < 0 || Double.isNaN(amount)) {
            return -1;
        }
        try (Connection conn = config.connectDB()) {
            conn.setAutoCommit(false);
            try {
                String receiptNo = allocateReceiptNo(conn);
                String sql = "INSERT INTO payment_transactions (receipt_no, user_id, amount, purpose, payment_method, notes, recorded_by_user_id) "
                        + "VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, receiptNo);
                    ps.setInt(2, userId);
                    ps.setDouble(3, amount);
                    ps.setString(4, purpose != null ? purpose : "");
                    ps.setString(5, paymentMethod != null ? paymentMethod : "");
                    ps.setString(6, notes != null ? notes : "");
                    if (recordedByUserId != null) {
                        ps.setInt(7, recordedByUserId);
                    } else {
                        ps.setNull(7, java.sql.Types.INTEGER);
                    }
                    ps.executeUpdate();
                }
                int id = lastInsertId(conn);
                conn.commit();
                return id;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error inserting payment: " + e.getMessage());
                return -1;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting payment (connection): " + e.getMessage());
            return -1;
        }
    }

    private static int lastInsertId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT last_insert_rowid()");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return (int) rs.getLong(1);
            }
        }
        return -1;
    }

    private static String allocateReceiptNo(Connection conn) throws SQLException {
        String day = new SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(new Date());
        String prefix = "RCP-" + day + "-";
        int next = 1;
        String sql = "SELECT receipt_no FROM payment_transactions WHERE receipt_no LIKE ? ORDER BY receipt_no DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    if (last != null && last.length() > prefix.length()) {
                        try {
                            next = Integer.parseInt(last.substring(prefix.length())) + 1;
                        } catch (NumberFormatException ignored) {
                            next = 1;
                        }
                    }
                }
            }
        }
        return prefix + String.format(Locale.ROOT, "%04d", next);
    }

    private static Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("id", rs.getInt("id"));
        m.put("receipt_no", rs.getString("receipt_no"));
        m.put("user_id", rs.getInt("user_id"));
        m.put("amount", rs.getDouble("amount"));
        m.put("purpose", rs.getString("purpose"));
        m.put("payment_method", rs.getString("payment_method"));
        m.put("notes", rs.getString("notes"));
        int rb = rs.getInt("recorded_by_user_id");
        if (rs.wasNull()) {
            m.put("recorded_by_user_id", null);
        } else {
            m.put("recorded_by_user_id", rb);
        }
        m.put("created_at", rs.getString("created_at"));
        m.put("payer_name", rs.getString("payer_name"));
        m.put("recorder_name", rs.getString("recorder_name"));
        return m;
    }
}
