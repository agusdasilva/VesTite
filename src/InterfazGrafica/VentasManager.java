package InterfazGrafica;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VentasManager extends DatosManager {
    private final MenuPrincipal menuPrincipal;
    private final List<Object[]> carrito;

    public VentasManager(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        this.carrito = new ArrayList<>();
        this.rowSorter = new TableRowSorter<>((DefaultTableModel) menuPrincipal.ventaTable.getModel());
        menuPrincipal.ventaTable.setRowSorter(rowSorter);
        inicializarVentasManager();
        ProductoManager.cargarDatosVentaTable(menuPrincipal);
    }

    public void inicializarVentasManager() {
        agregarDocumentListener(menuPrincipal.idVentaText, 0);
        agregarDocumentListener(menuPrincipal.productoVentaText, 1);
        agregarDocumentListener(menuPrincipal.talleVentaText, 3);
        agregarDocumentListener(menuPrincipal.colorVentaText, 4);

        agregarPopupMenu(menuPrincipal.ventaTable, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarProductoDelCarrito();
            }
        });

        menuPrincipal.carritoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProductoAlCarrito();
            }
        });

        menuPrincipal.carritoVentaList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    eliminarProductoDelCarrito();
                }
            }
        });

        menuPrincipal.venderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                efectuarCompra();
            }
        });
    }

    private void agregarDocumentListener(JTextField textField, int columnIndex) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarFiltro(textField, rowSorter, columnIndex);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarFiltro(textField, rowSorter, columnIndex);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarFiltro(textField, rowSorter, columnIndex);
            }
        });
    }

    private void actualizarFiltro(JTextField textField, TableRowSorter<DefaultTableModel> rowSorter, int columnIndex) {
        String texto = textField.getText().trim();
        if (texto.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            String textoEscapado = Pattern.quote(texto);
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + textoEscapado, columnIndex));
        }
    }

    private void agregarProductoAlCarrito() {
        try {
            int selectedRow = menuPrincipal.ventaTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(menuPrincipal, "Seleccione un producto de la tabla.");
                return;
            }

            int modelRow = menuPrincipal.ventaTable.convertRowIndexToModel(selectedRow);
            DefaultTableModel model = (DefaultTableModel) menuPrincipal.ventaTable.getModel();

            int id = (int) model.getValueAt(modelRow, 0);
            String producto = (String) model.getValueAt(modelRow, 1);
            String color = (String) model.getValueAt(modelRow, 2);
            String talle = (String) model.getValueAt(modelRow, 3);
            BigDecimal precio = new BigDecimal(model.getValueAt(modelRow, 4).toString());
            int stockActual = (int) model.getValueAt(modelRow, 5);

            int cantidad = Integer.parseInt(menuPrincipal.cantidadVentaText.getText());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(menuPrincipal, "La cantidad debe ser mayor que cero.");
                return;
            }

            if (cantidad > stockActual) {
                JOptionPane.showMessageDialog(menuPrincipal, "La cantidad no puede ser mayor al stock disponible.");
                return;
            }

            Object[] productoCarrito = {id, producto, color, talle, cantidad, precio.multiply(BigDecimal.valueOf(cantidad))};
            carrito.add(productoCarrito);
            actualizarCarritoList();
            actualizarTotalVenta();

            int nuevoStock = stockActual - cantidad;
            actualizarRegistro("productos", id, "stock", nuevoStock);
            ProductoManager.cargarDatosVentaTable(menuPrincipal);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(menuPrincipal, "Por favor, complete todos los campos correctamente.");
        }
    }

    private void actualizarCarritoList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Object[] producto : carrito) {
            listModel.addElement(producto[0] + " - " + producto[1] + " - " + producto[2] + " - " + producto[3] + " - " + producto[4]);
        }
        menuPrincipal.carritoVentaList.setModel(listModel);
    }

    private void eliminarProductoDelCarrito() {
        int selectedIndex = menuPrincipal.carritoVentaList.getSelectedIndex();
        if (selectedIndex != -1) {
            Object[] producto = carrito.remove(selectedIndex);
            actualizarCarritoList();
            actualizarTotalVenta();

            int id = (int) producto[0];
            int cantidad = (int) producto[4];

            int stockActual = buscarStockPorId(id);
            int nuevoStock = stockActual + cantidad;
            actualizarRegistro("productos", id, "stock", nuevoStock);

            ProductoManager.cargarDatosVentaTable(menuPrincipal);
        }
    }

    private int buscarStockPorId(int id) {
        int stockActual = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT stock FROM productos WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                stockActual = rs.getInt("stock");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockActual;
    }

    private void actualizarTotalVenta() {
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] producto : carrito) {
            total = total.add((BigDecimal) producto[5]);
        }
        menuPrincipal.totalVentaText.setText(total.toString());
    }

    private void efectuarCompra() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(menuPrincipal, "El carrito está vacío. Agregue productos antes de realizar la venta.");
            return;
        }

        BigDecimal totalVenta = BigDecimal.ZERO;
        for (Object[] producto : carrito) {
            totalVenta = totalVenta.add((BigDecimal) producto[5]);
        }

        int idVenta = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO ventas (fecha_venta, cantidad) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setBigDecimal(2, totalVenta);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idVenta = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(menuPrincipal, "Error al registrar la venta en la base de datos.");
            return;
        }

        for (Object[] producto : carrito) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO ventas (id, id_producto, cantidad, precio) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idVenta);
                stmt.setInt(2, (int) producto[0]);
                stmt.setInt(3, (int) producto[4]);
                stmt.setBigDecimal(4, (BigDecimal) producto[5]);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(menuPrincipal, "Error al registrar los productos vendidos en la base de datos.");
                return;
            }
        }

        DefaultTableModel cajaModel = (DefaultTableModel) menuPrincipal.cajaTable.getModel();
        cajaModel.addRow(new Object[]{idVenta, "Venta", totalVenta, new Timestamp(System.currentTimeMillis()), "Venta realizada"});

        carrito.clear();
        actualizarCarritoList();
        actualizarTotalVenta();

        JOptionPane.showMessageDialog(menuPrincipal, "Venta realizada con éxito.");
    }

    public static void agregarPopupMenu(JTable table, ActionListener deleteListener) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Eliminar");
        deleteItem.addActionListener(deleteListener);
        popupMenu.add(deleteItem);
        table.setComponentPopupMenu(popupMenu);
    }
}
