package onlineenrollment.main;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

public class PanelRound extends JPanel {

    private int roundTopLeft = 0;
    private int roundTopRight = 0;
    private int roundBottomLeft = 0;
    private int roundBottomRight = 0;

    public PanelRound() {
        setOpaque(false);
    }

    public void setRoundTopLeft(int radius) {
        this.roundTopLeft = radius;
        repaint();
    }

    public void setRoundTopRight(int radius) {
        this.roundTopRight = radius;
        repaint();
    }

    public void setRoundBottomLeft(int radius) {
        this.roundBottomLeft = radius;
        repaint();
    }

    public void setRoundBottomRight(int radius) {
        this.roundBottomRight = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());

        int width = getWidth();
        int height = getHeight();
        int radius = Math.max(
                Math.max(roundTopLeft, roundTopRight),
                Math.max(roundBottomLeft, roundBottomRight)
        );

        g2.fill(new RoundRectangle2D.Double(
                0, 0, width, height,
                radius * 2, radius * 2
        ));

        g2.dispose();
        super.paintComponent(g);
    }
}
