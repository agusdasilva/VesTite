package InterfazGrafica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.*;

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
    private JTable compraTable;
    private JButton comprarButton;
    private JButton agregarClienteButton;
    private JButton agregarProveedorButton;
    private JButton venderButton;
    private JTable ventaTable;
    private JTable clientesTable;
    private JTable proveedorTable;

    public MenuPrincipal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        redondearBotones();
        crearTablas();
        agregarActionListeners();

    }

    public void redondearBotones() {
        redondeoButon(abrirCajaButton);
        redondeoButon(cerrarCajaButton);
        redondeoButon(actualizarButton);
        redondeoButon(buscarFechButton);
        redondeoButon(agregarRopaButton);
        redondeoButon(agregarClienteButton);
        redondeoButon(agregarProveedorButton);
        redondeoButon(comprarButton);
        redondeoButon(venderButton);
    }

    public void redondeoButon(JButton buton) {

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

    public void crearTabla(JTable table, String[] columnas) {
        DefaultTableModel model2 = new DefaultTableModel(columnas, 0);
        table.setModel(model2);

    }

    public void crearTablas() {
        String[] columnNames1 = {"Fecha", "Total", "Entrada", "Salida", "Nota"};
        crearTabla(historialTable, columnNames1);
        String[] columnNames2 = {"Id", "Nombre", "Talle", "Categoria", "Precio", "Stock"};
        crearTabla(ropaTable, columnNames2);
        String[] columnNames3 = {"Id", "Nombre y Apellido", "Dni", "Contacto", "Nota"};
        crearTabla(clientesTable, columnNames3);
        String[] columnNames4 = {"Id", "Nombre y Apellido", "Dni", "Contacto", "Nota"};
        crearTabla(proveedorTable, columnNames4);
        String[] columnNames5 = {"Id", "Producto", "Talle", "Cantidad", "Precio", "Stock"};
        crearTabla(ventaTable, columnNames5);
        String[] columnNames6 = {"Id", "Producto", "Talle", "Cantidad", "Precio de venta", "Precio de compra", "Stock"};
        crearTabla(compraTable, columnNames6);
    }

    public void agregarActionListeners() {
        actualizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CajaManager.manejoCaja(agregarDineroText, quitarDineroText, historialTable);
            }
        });

        buscarFechButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CajaManager.buscarFechaEnCaja(BuscarFechaText, historialTable);
            }
        });
    }
}