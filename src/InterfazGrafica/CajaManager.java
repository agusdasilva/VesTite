package InterfazGrafica;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CajaManager extends DatosManager {

    public static void manejoCaja(JTextField agregarDineroText, JTextField quitarDineroText, JTable cajaTable) {
        BigDecimal cantidad;
        String tipoMovimiento;

        if (!agregarDineroText.getText().isEmpty()) {
            cantidad = new BigDecimal(agregarDineroText.getText());
            agregarDineroText.setText("");
            tipoMovimiento = "Entrada";
        } else if (!quitarDineroText.getText().isEmpty()) {
            cantidad = new BigDecimal(quitarDineroText.getText());
            quitarDineroText.setText("");
            tipoMovimiento = "Salida";
        } else {
            JOptionPane.showMessageDialog(null, "Ingrese una cantidad en agregar o quitar dinero.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO caja (tipo_movimiento, cantidad, fecha_hora) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tipoMovimiento);
            stmt.setBigDecimal(2, cantidad);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al manejar el movimiento de caja en la base de datos.");
            return;
        }

        cargarDatosPorFecha(cajaTable, new Date());
    }

    public static void cargarDatosPorFecha(JTable cajaTable, Date fecha) {
        DefaultTableModel model = (DefaultTableModel) cajaTable.getModel();
        model.setRowCount(0); // Limpiar la tabla

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fechaStr = dateFormat.format(fecha);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT tipo_movimiento, cantidad, fecha_hora FROM caja WHERE DATE(fecha_hora) = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fechaStr);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipoMovimiento = rs.getString("tipo_movimiento");
                BigDecimal cantidad = rs.getBigDecimal("cantidad");
                Timestamp fechaHora = rs.getTimestamp("fecha_hora");

                model.addRow(new Object[]{fechaHora, tipoMovimiento, cantidad});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los datos de la base de datos.");
        }
    }

    public static void cargarDatosPorBusqueda(JTable cajaTable, String busqueda) {
        DefaultTableModel model = (DefaultTableModel) cajaTable.getModel();
        model.setRowCount(0); // Limpiar la tabla

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT tipo_movimiento, cantidad, fecha_hora FROM caja WHERE tipo_movimiento LIKE ? OR cantidad LIKE ? OR DATE(fecha_hora) LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String busquedaParam = "%" + busqueda + "%";
            stmt.setString(1, busquedaParam);
            stmt.setString(2, busquedaParam);
            stmt.setString(3, busquedaParam);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipoMovimiento = rs.getString("tipo_movimiento");
                BigDecimal cantidad = rs.getBigDecimal("cantidad");
                Timestamp fechaHora = rs.getTimestamp("fecha_hora");

                model.addRow(new Object[]{fechaHora, tipoMovimiento, cantidad});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar los datos de la base de datos.");
        }
    }

    public static void cargarDatosCaja(JTable cajaTable) {
        DefaultTableModel cajaModel = (DefaultTableModel) cajaTable.getModel();
        cajaModel.setRowCount(0); // Limpiar datos actuales

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT fecha_hora, tipo_movimiento, cantidad FROM caja";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp fecha = rs.getTimestamp("fecha_hora");
                String tipo = rs.getString("tipo_movimiento");
                BigDecimal monto = rs.getBigDecimal("cantidad");
                cajaModel.addRow(new Object[]{fecha, tipo, monto});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void inicializarCajaManager(MenuPrincipal aux) {
        cargarDatosPorFecha(aux.cajaTable, new Date()); // Cargar datos del día actual

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) aux.cajaTable.getModel());
        aux.cajaTable.setRowSorter(sorter);

        aux.BuscarFechaText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                actualizarTabla();
            }

            public void removeUpdate(DocumentEvent e) {
                actualizarTabla();
            }

            public void insertUpdate(DocumentEvent e) {
                actualizarTabla();
            }

            public void actualizarTabla() {
                String text = aux.BuscarFechaText.getText();
                if (text.trim().length() == 0) {
                    cargarDatosPorFecha(aux.cajaTable, new Date());
                } else {
                    cargarDatosPorBusqueda(aux.cajaTable, text);
                }
            }
        });

        ((DefaultTableModel) aux.cajaTable.getModel()).addTableModelListener(new TableModelListener() {
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

        aux.cerrarCajaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BigDecimal totalCaja = BigDecimal.ZERO;
                DefaultTableModel model = (DefaultTableModel) aux.cajaTable.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String tipoMovimiento = (String) model.getValueAt(i, 1);
                    BigDecimal cantidad = (BigDecimal) model.getValueAt(i, 2);
                    if ("Entrada".equals(tipoMovimiento) || "Venta".equals(tipoMovimiento) || "Venta Afip".equals(tipoMovimiento)) {
                        totalCaja = totalCaja.add(cantidad);
                    } else {
                        totalCaja = totalCaja.subtract(cantidad);
                    }
                }
                JOptionPane.showMessageDialog(null, "El valor final de la caja es: " + totalCaja);

                // Crear el comprobante PDF
                crearComprobantePDF(model, totalCaja);
            }
        });
    }

    private static void crearComprobantePDF(DefaultTableModel model, BigDecimal totalCaja) {
        try {
            String path = "comprobante_cierre_caja.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            document.add(new Paragraph("Resumen financiero"));
            document.add(new Paragraph("Nombre del Local: VessTite"));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            document.add(new Paragraph("Fecha y Hora: " + dateFormat.format(new Date()) + "\n"));

            document.add(new Paragraph(""));

            PdfPTable table = new PdfPTable(3);
            table.addCell("Fecha y Hora");
            table.addCell("Tipo Movimiento");
            table.addCell("Cantidad");

            for (int i = 0; i < model.getRowCount(); i++) {
                table.addCell(model.getValueAt(i, 0).toString());
                table.addCell(model.getValueAt(i, 1).toString());
                table.addCell(model.getValueAt(i, 2).toString());
            }

            document.add(table);

            document.add(new Paragraph(""));
            document.add(new Paragraph("Saldo Final: " + totalCaja));

            document.close();

            JOptionPane.showMessageDialog(null, "Comprobante generado exitosamente en " + path);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al generar el comprobante PDF.");
        }
    }
}
