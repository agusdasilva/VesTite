package InterfazGrafica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.*;

public class CajaManager {
    public static BigDecimal calcularTotal(Connection conn) throws SQLException {
        String sql = "SELECT SUM(CASE WHEN tipo_movimiento = 'entrada' THEN cantidad ELSE -cantidad END) AS total FROM caja";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            return rs.getBigDecimal("total");
        }
        return BigDecimal.ZERO;
    }

    public static void actualizarCaja(Connection conn, String tipoMovimiento, BigDecimal cantidad) throws SQLException {
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO caja (tipo_movimiento, cantidad, nota) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tipoMovimiento);
            stmt.setBigDecimal(2, cantidad);
            stmt.setString(3, "Caja");
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    public static ResultSet buscarMovimientosPorFecha(Connection conn, String fecha) throws SQLException {
        String sql = "SELECT * FROM caja WHERE DATE(fecha_hora) = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, fecha);
        return stmt.executeQuery();
    }
    public static void manejoCaja(JTextField agregarDineroText, JTextField quitarDineroText, JTable historialTable) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String tipoMovimiento;
            BigDecimal cantidad;

            if (!agregarDineroText.getText().isEmpty()) {
                tipoMovimiento = "entrada";
                cantidad = new BigDecimal(agregarDineroText.getText());
            } else if (!quitarDineroText.getText().isEmpty()) {
                tipoMovimiento = "salida";
                cantidad = new BigDecimal(quitarDineroText.getText());
            } else {
                JOptionPane.showMessageDialog(null, "Ingrese un valor en agregar o quitar dinero", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CajaManager.actualizarCaja(conn, tipoMovimiento, cantidad);
            BigDecimal total = CajaManager.calcularTotal(conn);

            DefaultTableModel model = (DefaultTableModel) historialTable.getModel();
            String fecha = java.time.LocalDate.now().toString();
            String nota = "Caja";

            model.addRow(new Object[]{fecha, total.toString(), tipoMovimiento.equals("entrada") ? cantidad.toString() : "0.00", tipoMovimiento.equals("salida") ? cantidad.toString() : "0.00", nota});

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
    public static void buscarFechaEnCaja(JTextField BuscarFechaText,  JTable historialTable){
        String fecha = BuscarFechaText.getText().trim();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = CajaManager.buscarMovimientosPorFecha(conn, fecha);
            DefaultTableModel model = (DefaultTableModel) historialTable.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                String fechaMovimiento = rs.getString("fecha_hora");
                BigDecimal cantidad = rs.getBigDecimal("cantidad");
                String tipoMovimiento = rs.getString("tipo_movimiento");
                String nota = rs.getString("nota");

                model.addRow(new Object[]{fechaMovimiento, cantidad.toString(), tipoMovimiento.equals("entrada") ? cantidad.toString() : "0.00", tipoMovimiento.equals("salida") ? cantidad.toString() : "0.00", nota});
            }

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}