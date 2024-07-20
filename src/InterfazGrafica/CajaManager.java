package InterfazGrafica;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;

public class CajaManager extends DatosManager {
    public static void manejoCaja(JTextField agregarDineroText, JTextField quitarDineroText, JTable cajaTable) {
        BigDecimal cantidad;
        String tipoMovimiento;

        if (!agregarDineroText.getText().isEmpty()) {
            cantidad = new BigDecimal(agregarDineroText.getText());
            tipoMovimiento = "entrada";
        } else if (!quitarDineroText.getText().isEmpty()) {
            cantidad = new BigDecimal(quitarDineroText.getText());
            tipoMovimiento = "salida";
        } else {
            JOptionPane.showMessageDialog(null, "Ingrese una cantidad en agregar o quitar dinero.");
            return;
        }

        String nota = "Caja";

        int id = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO caja (tipo_movimiento, cantidad, nota) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, tipoMovimiento);
            stmt.setBigDecimal(2, cantidad);
            stmt.setString(3, nota);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al manejar el movimiento de caja en la base de datos.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) cajaTable.getModel();
        model.addRow(new Object[]{id, tipoMovimiento, cantidad, new Timestamp(System.currentTimeMillis()), nota});
        JOptionPane.showMessageDialog(null, "Movimiento de caja registrado exitosamente.");
    }

    public static void cargarDatosIniciales(JTable cajaTable) {
        DefaultTableModel model = (DefaultTableModel) cajaTable.getModel();
        model.setRowCount(0); // Limpiar la tabla

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, tipo_movimiento, cantidad, fecha_hora, nota FROM caja";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String tipoMovimiento = rs.getString("tipo_movimiento");
                BigDecimal cantidad = rs.getBigDecimal("cantidad");
                Timestamp fechaHora = rs.getTimestamp("fecha_hora");
                String nota = rs.getString("nota");

                model.addRow(new Object[]{id, tipoMovimiento, cantidad, fechaHora, nota});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los datos iniciales de la base de datos.");
        }
    }

    public static void inicializarCajaManager(MenuPrincipal aux) {
        agregarDocumentListener(aux.cajaTable, aux.BuscarFechaText);
        agregarTableModelListener(aux.cajaTable, new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    int id = (int) ((DefaultTableModel) aux.cajaTable.getModel()).getValueAt(row, 0);
                    Object newValue = ((DefaultTableModel) aux.cajaTable.getModel()).getValueAt(row, column);
                    String columnName = ((DefaultTableModel) aux.cajaTable.getModel()).getColumnName(column);

                    actualizarRegistro("caja", id, columnName, newValue);
                }
            }
        });

        agregarPopupMenu(aux.cajaTable, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = aux.cajaTable.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) ((DefaultTableModel) aux.cajaTable.getModel()).getValueAt(selectedRow, 0);
                    eliminarRegistro("caja", id);
                    ((DefaultTableModel) aux.cajaTable.getModel()).removeRow(selectedRow);
                }
            }
        });

        agregarDocumentListener(aux.cajaTable, aux.BuscarFechaText);
        cargarDatosIniciales(aux.cajaTable);
    }
}
