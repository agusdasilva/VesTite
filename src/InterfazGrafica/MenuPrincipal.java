package InterfazGrafica;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private JButton actualizarButton;
    protected JTabbedPane tabbedPane1;
    protected JTable compraTable;
    private JButton comprarButton;
    private JButton agregarProveedorButton;
    protected JTable proveedorTable;
    //caja
    protected JButton abrirCajaButton;
    protected JButton cerrarCajaButton;
    private JTextField agregarDineroText;
    private JTextField quitarDineroText;
    protected JTextField BuscarFechaText;
    //producto
    protected JTable ropaTable;
    protected JTextField buscarProductoText;
    protected JTextField nombreProductoText;
    protected JTextField talleProductoText;
    private JButton agregarProductoButton;
    protected JTextField categoriaProductoText;
    protected JTextField precioProductoText;
    protected JTextField stockProductoText;
    protected JTextField colorProductoText;
    protected JComboBox<String> ropaBox;
    // Venta
    protected JList<String> sugerenciaClienteList;
    private JPopupMenu popupMenu = new JPopupMenu();
    protected JButton venderButton;
    protected JTable ventaTable;
    protected JTextField idVentaText;
    protected JTextField productoVentaText;
    protected JTextField talleVentaText;
    protected JTextField cantidadVentaText;
    protected JTextField colorVentaText;
    protected JTextField totalVentaText;
    protected JTextField clienteVentaText;
    protected JList carritoVentaList;
    protected JButton carritoButton;
    // Cliente
    protected JTextField buscadorClienteText;
    protected JTextField nombreClienteText;
    protected JTextField dniClienteText;
    protected JTextField contactoClienteText;
    protected JTextField notaClienteText;
    protected JButton agregarClienteButton;
    protected JTable clientesTable;

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
        new ClienteManager(MenuPrincipal.this);

        // Agregar action listeners para cambiar pestañas
        agregarCambioPestanas();

        clienteVentaText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                popupMenu.setVisible(false); // Ocultar el popup cuando el JTextField pierde el foco
            }
        });

        clienteVentaText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    // Navegar hacia abajo en el menú de sugerencias con la tecla de flecha hacia abajo
                    int selectedIndex = sugerenciaClienteList.getSelectedIndex();
                    if (selectedIndex < sugerenciaClienteList.getModel().getSize() - 1) {
                        sugerenciaClienteList.setSelectedIndex(selectedIndex + 1);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    // Navegar hacia arriba en el menú de sugerencias con la tecla de flecha hacia arriba
                    int selectedIndex = sugerenciaClienteList.getSelectedIndex();
                    if (selectedIndex > 0) {
                        sugerenciaClienteList.setSelectedIndex(selectedIndex - 1);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Seleccionar el cliente con la tecla Enter
                    seleccionarClienteSugerido();
                }
            }
        });

        clienteVentaText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarSugerenciasClientes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarSugerenciasClientes();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarSugerenciasClientes();
            }
        });
    }

    private void actualizarSugerenciasClientes() {
        String texto = clienteVentaText.getText();
        popupMenu.removeAll();
        if (texto.isEmpty() || texto.length() <= 1) {
            popupMenu.setVisible(false);
            return;
        }

        List<String> sugerencias = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT nombre FROM clientes WHERE nombre LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + texto + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sugerencias.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!sugerencias.isEmpty()) {
            sugerenciaClienteList = new JList<>(sugerencias.toArray(new String[0]));
            sugerenciaClienteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            sugerenciaClienteList.setFocusable(false);
            sugerenciaClienteList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        seleccionarClienteSugerido();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(sugerenciaClienteList);
            popupMenu.add(scrollPane);
            popupMenu.show(clienteVentaText, 0, clienteVentaText.getHeight());
            clienteVentaText.requestFocusInWindow();
        } else {
            popupMenu.setVisible(false);
        }
    }

    private void seleccionarClienteSugerido() {
        String clienteSeleccionado = sugerenciaClienteList.getSelectedValue();
        if (clienteSeleccionado != null) {
            clienteVentaText.setText(clienteSeleccionado);
            popupMenu.setVisible(false);
        }
    }

    private void agregarCambioPestanas() {
        cajaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(0); // Índice de la pestaña "Caja"
            }
        });

        clientesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(3); // Índice de la pestaña "Clientes"
            }
        });

        ventaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(1); // Índice de la pestaña "Ventas"
            }
        });

        productosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(5); // Índice de la pestaña "Productos"
            }
        });

        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Salir del programa
            }
        });
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
        String[] columnNames1 = {"Fecha", "Movimiento", "Monto"};
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