package utils;

import main.login;
import model.User;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Session {
    private Session() {}

    public static User getCurrentUser() {
        return login.getCurrentUser();
    }

    public static void setCurrentUser(User user) {
        login.setCurrentUser(user);
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static void logoutAndGoToLogin(JComponent source) {
        setCurrentUser(null);
        login log = new login();
        log.setVisible(true);
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(source);
        if (frame != null) {
            frame.dispose();
        }
    }
}

