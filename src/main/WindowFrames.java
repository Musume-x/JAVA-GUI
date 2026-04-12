package main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Top-right Back: closes this frame, then opens the previous screen (see {@code onBack}).
 * Not used on the login or register windows themselves.
 */
public final class WindowFrames {

    private WindowFrames() {
    }

    public static void setContentWithBack(JFrame frame, Component content, Runnable onBack) {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JButton back = new JButton("Back");
        back.setToolTipText("Return to the previous screen");
        back.addActionListener(e -> {
            frame.dispose();
            if (onBack != null) {
                SwingUtilities.invokeLater(onBack);
            }
        });
        top.add(back);
        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        frame.setContentPane(root);
    }
}
