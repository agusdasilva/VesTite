package InterfazGrafica;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CircularButton extends JButton {

    public CircularButton(String text) {
        super(text);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Ajusta los valores para redondear las esquinas
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30); // Ajusta los valores para redondear las esquinas
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        int width = getWidth();
        int height = getHeight();
        int arcWidth = 30;
        int arcHeight = 30;
        Shape shape = new RoundRectangle2D.Float(0, 0, width, height, arcWidth, arcHeight);
        return shape.contains(x, y);
    }
}
