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
import java.sql.*;

public class ClienteManager extends DatosManager {
    private final MenuPrincipal menuPrincipal;
    private final TableRowSorter<DefaultTableModel> rowSorter;

    public ClienteManager(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        this.rowSorter = new TableRowSorter<>((DefaultTableModel) menuPrincipal.clientesTable.getModel());
        menuPrincipal.clientesTable.setRowSorter(rowSorter);
        inicializarClienteManager();
        cargarDatosIniciales();
    }

    private void inicializarClienteManager() {
        menuPrincipal.agregarClienteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarCliente();
            }
        });

        agregarDocumentListener(menuPrincipal.buscadorClienteText, rowSorter);
        agregarTableModelListener(menuPrincipal.clientesTable);
        agregarPopupMenu(menuPrincipal.clientesTable);
    }

    private void agregarDocumentListener(JTextField textField, TableRowSorter<DefaultTableModel> rowSorter) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarFiltro(textField, rowSorter);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarFiltro(textField, rowSorter);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarFiltro(textField, rowSorter);
            }
        });
    }

    private void actualizarFiltro(JTextField textField, TableRowSorter<DefaultTableModel> rowSorter) {
        String texto = textField.getText().trim();
        if (texto.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }

    private void agregarCliente() {
        String nombre = menuPrincipal.nombreClienteText.getText().trim();
        String dni = menuPrincipal.dniClienteText.getText().trim();
        String contacto = menuPrincipal.contactoClienteText.getText().trim();
        String nota = menuPrincipal.notaClienteText.getText().trim();

        if (nombre.isEmpty() || dni.isEmpty() || contacto.isEmpty()) {
            JOptionPane.showMessageDialog(menuPrincipal, "Por favor, complete todos los campos obligatorios.");
            return;
        }

        int id = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO clientes (nombre, dni, contacto, nota) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nombre);
            stmt.setString(2, dni);
            stmt.setString(3, contacto);
            stmt.setString(4, nota.isEmpty() ? null : nota);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(menuPrincipal, "Error al agregar el cliente en la base de datos.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) menuPrincipal.clientesTable.getModel();
        model.addRow(new Object[]{id, nombre, dni, contacto, nota});
        limpiarCampos();
        JOptionPane.showMessageDialog(menuPrincipal, "Cliente agregado exitosamente.");
    }

    private void cargarDatosIniciales() {
        DefaultTableModel model = (DefaultTableModel) menuPrincipal.clientesTable.getModel();
        model.setRowCount(0); // Limpiar la tabla

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nombre, dni, contacto, nota FROM clientes";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String dni = rs.getString("dni");
                String contacto = rs.getString("contacto");
                String nota = rs.getString("nota");

                model.addRow(new Object[]{id, nombre, dni, contacto, nota});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(menuPrincipal, "Error al cargar los datos iniciales de la base de datos.");
        }
    }

    private void limpiarCampos() {
        menuPrincipal.nombreClienteText.setText("");
        menuPrincipal.dniClienteText.setText("");
        menuPrincipal.contactoClienteText.setText("");
        menuPrincipal.notaClienteText.setText("");
    }

    private void agregarTableModelListener(JTable table) {
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    int id = (int) ((DefaultTableModel) table.getModel()).getValueAt(row, 0);
                    Object newValue = ((DefaultTableModel) table.getModel()).getValueAt(row, column);
                    String columnName = ((DefaultTableModel) table.getModel()).getColumnName(column);

                    actualizarRegistro(id, columnName, newValue);
                }
            }
        });
    }

    private void actualizarRegistro(int id, String columnName, Object newValue) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE clientes SET " + columnName + " = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, newValue);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(menuPrincipal, "Error al actualizar el cliente en la base de datos.");
        }
    }

    private void agregarPopupMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem eliminarItem = new JMenuItem("Eliminar");

        eliminarItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) ((DefaultTableModel) table.getModel()).getValueAt(selectedRow, 0);
                    eliminarRegistro(id);
                    ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
                }
            }
        });

        popupMenu.add(eliminarItem);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && table.getSelectedRow() != -1) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void eliminarRegistro(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM clientes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(menuPrincipal, "Error al eliminar el cliente en la base de datos.");
        }
    }
}
