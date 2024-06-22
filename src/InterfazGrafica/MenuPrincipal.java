package InterfazGrafica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MenuPrincipal extends JFrame {
    private JPanel mainPanel;
    private JButton ajustesButton;
    private JButton salirButton;
    private JButton cajaButton;
    private JButton categoriasButton;
    private JButton productosButton;
    private JButton proveedoresButton;
    private JButton clientesButton;
    private JButton ventaButton;
    private JTable historialTable;
    private JPanel vesTitePanel;
    private JPanel botonesPanel;
    private JPanel extraPanel;
    private JPanel datosPanel;
    private JButton abrirCajaButton;
    private JButton cerrarCajaButton;
    private JTextField agregarDineroText;
    private JTextField quitarDineroText;
    private JButton actualizarButton;
    private JTextField BuscarFechaText;
    private JButton buscarFechButton;
    private JTabbedPane tabbedPane1;
    private JButton agregarRopaButton;
    private JTable ropaTable;

    public MenuPrincipal(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        redondeoButon(abrirCajaButton);
        redondeoButon(cerrarCajaButton);
        redondeoButon(actualizarButton);
        redondeoButon(buscarFechButton);


        String[] columnNames1 = {"Fecha", "Monto Inicial", "Entrada", "Salida",  "Nota"};
        DefaultTableModel model1 = new DefaultTableModel(columnNames1,0);
        historialTable.setModel(model1);

        String[] columnNames2 = {"Id", "Nombre", "Talle", "Categoria", "Precio", "Stock"};
        DefaultTableModel model2 = new DefaultTableModel(columnNames2,0);
        ropaTable.setModel(model2);


    }
    public void redondear(){
        redondeoButon(abrirCajaButton);
        redondeoButon(cerrarCajaButton);
        redondeoButon(actualizarButton);
        redondeoButon(buscarFechButton);
        redondeoButon(agregarRopaButton);
    }
    public void redondeoButon(JButton buton){

        Font boldFont = new Font(buton.getFont().getName(), Font.BOLD, buton.getFont().getSize());
        buton.setFont(boldFont);
        buton.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                buton.repaint();
            }
        });
        buton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 30, 30);
                super.paint(g2, c);
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getForeground());
                FontMetrics fm = g.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });
    }

}
