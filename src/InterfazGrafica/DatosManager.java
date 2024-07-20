package InterfazGrafica;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DatosManager {
    protected static TableRowSorter<DefaultTableModel> rowSorter;

    protected static void agregarTableModelListener(JTable table, TableModelListener listener) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addTableModelListener(listener);
    }

    protected static void agregarPopupMenu(JTable table, ActionListener eliminarListener) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem eliminarItem = new JMenuItem("Eliminar");
        popupMenu.add(eliminarItem);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });

        eliminarItem.addActionListener(eliminarListener);
    }

    protected static void eliminarRegistro(String tableName, int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar el registro de la base de datos.");
        }
    }

    protected static void actualizarRegistro(String tableName, int id, String columnName, Object newValue) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            if (newValue instanceof String) {
                stmt.setString(1, (String) newValue);
            } else if (newValue instanceof BigDecimal) {
                stmt.setBigDecimal(1, (BigDecimal) newValue);
            } else if (newValue instanceof Integer) {
                stmt.setInt(1, (Integer) newValue);
            }

            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al actualizar el registro en la base de datos.");
        }
    }

    protected static void agregarDocumentListener(JTable tabla, JTextField buscar) {
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>((DefaultTableModel) tabla.getModel());
        tabla.setRowSorter(rowSorter);

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarFiltro(buscar, rowSorter);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarFiltro(buscar, rowSorter);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarFiltro(buscar, rowSorter);
            }
        });
    }

    private static void actualizarFiltro(JTextField buscar, TableRowSorter<DefaultTableModel> rowSorter) {
        String texto = buscar.getText().trim();
        if (texto.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
}
