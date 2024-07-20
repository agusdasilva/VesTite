package InterfazGrafica;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

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
    protected JTable cajaTable;
    private JPanel vesTitePanel;
    private JPanel botonesPanel;
    private JPanel extraPanel;
    private JPanel datosPanel;
    private JButton abrirCajaButton;
    private JButton cerrarCajaButton;
    private JTextField agregarDineroText;
    private JTextField quitarDineroText;
    private JButton actualizarButton;
    protected JTextField BuscarFechaText;
    private JButton buscarFechButton;
    protected JTabbedPane tabbedPane1;
    private JButton agregarProductoButton;
    protected JTable ropaTable;
    protected JTable compraTable;
    private JButton comprarButton;
    private JButton agregarClienteButton;
    private JButton agregarProveedorButton;
    protected JButton venderButton;
    protected JTable ventaTable;
    protected JTable clientesTable;
    protected JTable proveedorTable;
    protected JTextField buscarProductoText;
    protected JTextField nombreProductoText;
    protected JTextField talleProductoText;
    protected JTextField categoriaProductoText;
    protected JTextField precioProductoText;
    protected JTextField stockProductoText;
    protected JTextField colorProductoText;
    protected JComboBox<String> ropaBox;
    protected JTextField idVentaText;
    protected JTextField productoVentaText;
    protected JTextField talleVentaText;
    protected JTextField cantidadVentaText;
    protected JTextField precioCantVentaText;
    protected JTextField colorVentaText;
    protected JTextField totalVentaText;
    protected JTextField clienteVentaText;
    protected JList carritoVentaList;
    protected JButton carritoButton;


    public MenuPrincipal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        redondearBotones();
        crearTablas();
        agregarActionListeners();
        CajaManager.inicializarCajaManager(MenuPrincipal.this);
        ProductoManager.inicializarProductoManager(MenuPrincipal.this);
        new VentasManager(MenuPrincipal.this);

    }

    public void redondearBotones() {
        redondeoButon(abrirCajaButton);
        redondeoButon(cerrarCajaButton);
        redondeoButon(actualizarButton);
        redondeoButon(agregarProductoButton);
        redondeoButon(agregarClienteButton);
        redondeoButon(agregarProveedorButton);
        redondeoButon(comprarButton);
        redondeoButon(venderButton);
        redondeoButon(carritoButton);
    }

    public static void redondeoButon(JButton button) {
        Font boldFont = new Font(button.getFont().getName(), Font.BOLD, button.getFont().getSize());
        button.setFont(boldFont);

        button.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                button.repaint();
            }
        });

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
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

        // Establecer el borde del botón
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Mantener el color de fondo original y cambiar solo temporalmente al interactuar
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalBackground = button.getBackground();

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.GRAY); // Cambia a gris al hacer clic
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBackground); // Vuelve al color original
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().darker()); // Realza el color al pasar el mouse
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBackground); // Vuelve al color original
            }
        });
    }
    public void crearTabla(JTable table, String[] columnas) {
        DefaultTableModel model2 = new DefaultTableModel(columnas, 0);
        table.setModel(model2);

    }

    public void crearTablas() {
        String[] columnNames1 = {"Fecha", "Total", "Entrada", "Salida", "Nota"};
        crearTabla(cajaTable, columnNames1);
        String[] columnNames2 = {"Id", "Nombre", "Categoria", "Talle", "Precio", "Color", "Stock"};
        crearTabla(ropaTable, columnNames2);
        String[] columnNames3 = {"Id", "Nombre y Apellido", "Dni", "Contacto", "Nota"};
        crearTabla(clientesTable, columnNames3);
        String[] columnNames4 = {"Id", "Nombre y Apellido", "Dni", "Contacto", "Nota"};
        crearTabla(proveedorTable, columnNames4);
        String[] columnNames5 = {"Id", "Producto", "Color", "Talle", "Precio", "Stock"};
        crearTabla(ventaTable, columnNames5);
        String[] columnNames6 = {"Id", "Producto", "Talle", "Cantidad", "Precio de venta", "Precio de compra", "Stock"};
        crearTabla(compraTable, columnNames6);
    }

    public void agregarActionListeners() {
        actualizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CajaManager.manejoCaja(agregarDineroText, quitarDineroText, cajaTable);
            }
        });

        agregarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProductoManager.agregarProducto(MenuPrincipal.this);
            }
        });
    }



}