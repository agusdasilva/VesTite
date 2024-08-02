package InterfazGrafica;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VentasManager extends DatosManager {
    private final MenuPrincipal menuPrincipal;
    private final List<Object[]> carrito;
    private AjustesManager ajustesManager;
    private JPopupMenu popupMenu = new JPopupMenu();
    private DefaultListModel<String> listModel = new DefaultListModel<>();

    public VentasManager(MenuPrincipal menuPrincipal, AjustesManager ajustesManager) {
        this.menuPrincipal = menuPrincipal;
        this.ajustesManager = ajustesManager;
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

        agregarAutocompletadoCliente(menuPrincipal.clienteVentaText);
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
        String clienteNombre = "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO ventas (fecha_hora, cliente_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));

            int clienteId;
            try {
                clienteId = Integer.parseInt(menuPrincipal.clienteVentaText.getText().trim());
                if (!esClienteValido(clienteId)) {
                    JOptionPane.showMessageDialog(menuPrincipal, "El ID del cliente no es válido.");
                    return;
                }
                clienteNombre = obtenerNombreCliente(clienteId);
            } catch (NumberFormatException e) {
                clienteId = 0;  // ID del cliente no proporcionado o no válido
            }

            stmt.setInt(2, clienteId);
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
                String sql = "INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idVenta);
                stmt.setInt(2, (int) producto[0]);
                stmt.setInt(3, (int) producto[4]);
                stmt.setBigDecimal(4, (BigDecimal) producto[5]);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(menuPrincipal, "Error al registrar el detalle de la venta en la base de datos.");
            }
        }

        generarPDFFactura(idVenta, clienteNombre, totalVenta);

        carrito.clear();
        actualizarCarritoList();
        actualizarTotalVenta();
    }

    private boolean esClienteValido(int clienteId) {
        boolean valido = false;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM clientes WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                valido = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return valido;
    }

    private String obtenerNombreCliente(int clienteId) {
        String nombre = "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM clientes WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nombre = rs.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombre;
    }

    private void generarPDFFactura(int idVenta, String clienteNombre, BigDecimal totalVenta) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("factura_" + idVenta + ".pdf"));
            document.open();

            document.add(new Paragraph("Factura de Venta\n\n"));
            document.add(new Paragraph("ID Venta: " + idVenta));
            document.add(new Paragraph("Cliente: " + clienteNombre));
            document.add(new Paragraph("Fecha: " + new Timestamp(System.currentTimeMillis())));
            document.add(new Paragraph("\nDatos del Local:\n"));
            document.add(new Paragraph("Nombre del Local: " + ajustesManager.getNombreLocal()));
            document.add(new Paragraph("Dirección: " + ajustesManager.getDireccionLocal()));
            document.add(new Paragraph("Contacto: " + ajustesManager.getContactoLocal()));
            document.add(new Paragraph("Mensaje: " + ajustesManager.getMensajeLocal()));
            document.add(new Paragraph("\nProductos:\n"));

            PdfPTable table = new PdfPTable(5);
            table.addCell(new PdfPCell(new Paragraph("ID Producto")));
            table.addCell(new PdfPCell(new Paragraph("Nombre")));
            table.addCell(new PdfPCell(new Paragraph("Color")));
            table.addCell(new PdfPCell(new Paragraph("Talle")));
            table.addCell(new PdfPCell(new Paragraph("Cantidad")));

            for (Object[] producto : carrito) {
                table.addCell(producto[0].toString());
                table.addCell(producto[1].toString());
                table.addCell(producto[2].toString());
                table.addCell(producto[3].toString());
                table.addCell(producto[4].toString());
            }

            document.add(table);
            document.add(new Paragraph("\nTotal: " + totalVenta.toString()));

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    public void agregarAutocompletadoCliente(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarSugerenciasClientes(textField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarSugerenciasClientes(textField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarSugerenciasClientes(textField.getText());
            }
        });
    }

    public void actualizarSugerenciasClientes(String texto) {
        popupMenu.removeAll();
        if (texto.isEmpty() || texto.length() <= 1) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT nombre FROM clientes WHERE nombre LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + texto + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nombreCliente = rs.getString("nombre");
                JMenuItem item = new JMenuItem(nombreCliente);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        menuPrincipal.clienteVentaText.setText(nombreCliente);
                        popupMenu.setVisible(false);
                    }
                });
                popupMenu.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (popupMenu.getComponentCount() > 0) {
            popupMenu.show(menuPrincipal.clienteVentaText, 0, menuPrincipal.clienteVentaText.getHeight());
        }
    }
    protected static List<String> obtenerSugerenciasCliente(String texto) {
        List<String> sugerencias = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT nombre FROM clientes WHERE nombre LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, texto + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sugerencias.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sugerencias;
    }

}
