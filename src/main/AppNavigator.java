package main;

import admin.adminDashPane;
import admin.adminForm;
import admin.adminProfile;
import admin.masterlist;
import admin.userManagement;
import gui.enrollmentGeneric;
import gui.enrollmentPreSchool;
import gui.enrollmentStatusPage;
import gui.landingPage;
import gui.preschool;
import gui.preschoolPage;
import gui.programPage;
import gui.userForm;
import gui.userProfile;
import model.User;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Opens main screens so the Back button can return to the real previous view (not only close).
 */
public final class AppNavigator {

    private static final int W = 1020;
    private static final int H = 560;

    private AppNavigator() {
    }

    public static void showLogin() {
        SwingUtilities.invokeLater(() -> {
            login log = new login();
            log.setVisible(true);
        });
    }

    public static void showLanding(Runnable onBack) {
        User u = login.getCurrentUser();
        if (u == null) {
            showLogin();
            return;
        }
        landingPage lp = new landingPage();
        lp.setUserName(u.getFullName());
        JFrame f = new JFrame("Landing Page");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, lp, onBack);
        sizeShow(f);
    }

    public static void showPreschoolPanel(Runnable onBack) {
        showPreschoolPanel(onBack, null);
    }

    /**
     * @param studentName optional label set on the preschool hub after enrollment submit
     */
    public static void showPreschoolPanel(Runnable onBack, String studentName) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        preschool p = new preschool();
        if (studentName != null && !studentName.trim().isEmpty()) {
            p.setStudentName(studentName.trim());
        }
        JFrame f = new JFrame("Pre-School");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, p, onBack);
        sizeShow(f);
    }

    public static void showPreschoolPage(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        preschoolPage pp = new preschoolPage();
        JFrame f = new JFrame("Pre-School");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, pp, onBack);
        sizeShow(f);
    }

    public static void showEnrollmentPreSchool(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        enrollmentPreSchool panel = new enrollmentPreSchool();
        User user = login.getCurrentUser();
        panel.prefillFromUser(user);
        JFrame f = new JFrame("Pre-School Enrollment");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, panel, onBack);
        sizeShow(f);
    }

    public static void showEnrollmentStatus(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        enrollmentStatusPage panel = new enrollmentStatusPage();
        JFrame f = new JFrame("Enrollment Status");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, panel, onBack);
        sizeShow(f);
    }

    public static void showProgramPage(String program, String defaultLevel, Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        programPage p = new programPage(program, defaultLevel);
        JFrame f = new JFrame(program + " Page");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, p, onBack);
        sizeShow(f);
    }

    public static void showProgramEnrollment(String program, String defaultLevel, Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        enrollmentGeneric panel = new enrollmentGeneric(program.toLowerCase(), defaultLevel);
        JFrame f = new JFrame(program + " Enrollment");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, panel, onBack);
        sizeShow(f);
    }

    public static void showUserProfile(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        userProfile p = new userProfile();
        JFrame f = new JFrame("User Profile");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, p, onBack);
        sizeShow(f);
    }

    public static void showUserForm(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        userForm form = new userForm();
        JFrame f = new JFrame("Edit Profile");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, form, onBack);
        sizeShow(f);
    }

    public static void showAdminDashboard(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        JFrame f = new JFrame("Admin Dashboard");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, new adminDashPane(), onBack);
        sizeShow(f);
    }

    public static void showAdminProfile(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        adminProfile p = new adminProfile();
        JFrame f = new JFrame("Admin Profile");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, p, onBack);
        sizeShow(f);
    }

    public static void showAdminForm(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        adminForm form = new adminForm();
        JFrame f = new JFrame("Edit Admin Profile");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, form, onBack);
        sizeShow(f);
    }

    public static void showMasterlist() {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        masterlist panel = new masterlist();
        JFrame f = new JFrame("Masterlist");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, panel, () -> showAdminDashboard(AppNavigator::showLogin));
        sizeShow(f);
    }

    public static void showUserManagement() {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        userManagement panel = new userManagement();
        JFrame f = new JFrame("User Management");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WindowFrames.setContentWithBack(f, panel, () -> showAdminDashboard(AppNavigator::showLogin));
        sizeShow(f);
    }

    public static void showPaymentReceiptsWindow(Runnable onBack) {
        if (login.getCurrentUser() == null) {
            showLogin();
            return;
        }
        gui.paymentReceiptsPage page = new gui.paymentReceiptsPage();
        JFrame f = new JFrame("Payment receipts");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        WindowFrames.setContentWithBack(f, page, onBack);
        f.setSize(720, 440);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static void sizeShow(JFrame f) {
        f.pack();
        f.setSize(W, H);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
