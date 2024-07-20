package InterfazGrafica;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;

public class ProductoManager extends DatosManager {

    public static void agregarProducto(MenuPrincipal aux) {
        String nombre = aux.nombreProductoText.getText();
        String categoria = aux.categoriaProductoText.getText();
        String talle = aux.talleProductoText.getText();
        BigDecimal precio = new BigDecimal(aux.precioProductoText.getText());
        String color = aux.colorProductoText.getText();
        int stock;

        try {
            stock = Integer.parseInt(aux.stockProductoText.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El stock debe ser un número entero.");
            return;
        }

        int id = 0;

        // Agregar a la base de datos y obtener el ID generado automáticamente
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO productos (nombre, categoria, talle, precio, color, stock) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nombre);
            stmt.setString(2, categoria);
            stmt.setString(3, talle);
            stmt.setBigDecimal(4, precio);
            stmt.setString(5, color);
            stmt.setInt(6, stock);
            stmt.executeUpdate();

            // Obtener el ID generado automáticamente
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al agregar el producto a la base de datos.");
            return;
        }

        // Agregar a ropaTable
        DefaultTableModel model = (DefaultTableModel) aux.ropaTable.getModel();
        model.addRow(new Object[]{id, nombre, categoria, talle, precio, color, stock});
        JOptionPane.showMessageDialog(null, "Producto agregado exitosamente.");
        ProductoManager.cargarDatosRopaTable(aux, true);
        ProductoManager.cargarDatosVentaTable(aux);
    }

    public static void cargarDatosRopaTable(MenuPrincipal aux, boolean mostrarConStock) {
        DefaultTableModel model = (DefaultTableModel) aux.ropaTable.getModel();
        model.setRowCount(0); // Limpiar tabla antes de cargar

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombre, categoria, talle, precio, color, stock FROM productos";
            if (mostrarConStock) {
                sql += " WHERE stock > 0";
            }
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String categoria = rs.getString("categoria");
                String talle = rs.getString("talle");
                BigDecimal precio = rs.getBigDecimal("precio");
                String color = rs.getString("color");
                int stock = rs.getInt("stock");

                model.addRow(new Object[]{id, nombre, categoria, talle, precio, color, stock});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los productos desde la base de datos.");
        }
    }

    public static void inicializarProductoManager(MenuPrincipal aux) {
        agregarDocumentListener(aux.ropaTable,aux.buscarProductoText);
        ropaComboBoxListener(aux);
        agregarTableModelListener(aux.ropaTable, new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    int id = (int) ((DefaultTableModel) aux.ropaTable.getModel()).getValueAt(row, 0);
                    Object newValue = ((DefaultTableModel) aux.ropaTable.getModel()).getValueAt(row, column);
                    String columnName = ((DefaultTableModel) aux.ropaTable.getModel()).getColumnName(column);

                    actualizarRegistro("productos", id, columnName, newValue);
                }
            }
        });

        agregarPopupMenu(aux.ropaTable, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = aux.ropaTable.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) ((DefaultTableModel) aux.ropaTable.getModel()).getValueAt(selectedRow, 0);
                    eliminarRegistro("productos", id);
                    ((DefaultTableModel) aux.ropaTable.getModel()).removeRow(selectedRow);
                }
            }
        });
    }

    public static void ropaComboBoxListener(MenuPrincipal aux) {
        aux.ropaBox.addItem("Mostrar con Stock");
        aux.ropaBox.addItem("Mostrar todos");
        aux.ropaBox.setSelectedIndex(0); // Predeterminado: "Mostrar con Stock"

        aux.ropaBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    cargarDatosRopaTable(aux, aux.ropaBox.getSelectedIndex() == 0);
                }
            }
        });
    }
    public static void cargarDatosVentaTable(MenuPrincipal aux) {
        DefaultTableModel model = (DefaultTableModel) aux.ventaTable.getModel();
        model.setRowCount(0); // Limpiar tabla antes de cargar

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombre, categoria, talle, precio, color, stock FROM productos WHERE stock > 0";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                //String categoria = rs.getString("categoria");
                String color = rs.getString("color");
                String talle = rs.getString("talle");
                BigDecimal precio = rs.getBigDecimal("precio");
                int stock = rs.getInt("stock");

                model.addRow(new Object[]{id, nombre, color, talle, precio, stock});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los productos desde la base de datos.");
        }
    }

}
