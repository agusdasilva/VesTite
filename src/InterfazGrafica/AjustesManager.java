package InterfazGrafica;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AjustesManager {
    private MenuPrincipal menuPrincipal;

    public AjustesManager(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        cargarDatosLocal();
        agregarListeners();
    }

    private void cargarDatosLocal() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM ajustes WHERE id = 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                menuPrincipal.nombreLocalText.setText(rs.getString("nombre_local"));
                menuPrincipal.direccionLocalText.setText(rs.getString("direccion_local"));
                menuPrincipal.contactoLocalText.setText(rs.getString("contacto_local"));
                menuPrincipal.mensajeLocalText.setText(rs.getString("mensaje_local"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarListeners() {
        menuPrincipal.actualizarLocalButton.addActionListener(e -> actualizarDatosLocal());
        menuPrincipal.exportarLocalButton.addActionListener(e -> exportarDatosLocal());
        menuPrincipal.importarLocalButton.addActionListener(e -> importarDatosLocal());
    }

    private void actualizarDatosLocal() {
        String nombreLocal = menuPrincipal.nombreLocalText.getText();
        String direccionLocal = menuPrincipal.direccionLocalText.getText();
        String contactoLocal = menuPrincipal.contactoLocalText.getText();
        String mensajeLocal = menuPrincipal.mensajeLocalText.getText();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE ajustes SET nombre_local = ?, direccion_local = ?, contacto_local = ?, mensaje_local = ? WHERE id = 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombreLocal);
            pstmt.setString(2, direccionLocal);
            pstmt.setString(3, contactoLocal);
            pstmt.setString(4, mensajeLocal);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exportarDatosLocal() {
        try (Connection conn = DatabaseConnection.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter("DatosLocal.txt"))) {

            // Exportar datos de la tabla 'caja'
            exportarTabla(conn, writer, "caja");
            // Exportar datos de la tabla 'clientes'
            exportarTabla(conn, writer, "clientes");
            // Exportar datos de la tabla 'productos'
            exportarTabla(conn, writer, "productos");
            // Exportar datos de la tabla 'ventas'
            exportarTabla(conn, writer, "ventas");
            // Exportar datos de la tabla 'detalle_ventas'
            exportarTabla(conn, writer, "detalle_ventas");
            // Exportar datos de la tabla 'ajustes'
            exportarTabla(conn, writer, "ajustes");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void exportarTabla(Connection conn, PrintWriter writer, String tabla) throws SQLException {
        String sql = "SELECT * FROM " + tabla;
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int columnCount = rs.getMetaData().getColumnCount();
            writer.println("Tabla: " + tabla);

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    writer.print(rs.getString(i));
                    if (i < columnCount) {
                        writer.print("|");
                    }
                }
                writer.println();
            }
            writer.println();
        }
    }

    private void importarDatosLocal() {
        try (Connection conn = DatabaseConnection.getConnection();
             BufferedReader reader = new BufferedReader(new FileReader("DatosLocal.txt"))) {

            String line;
            String currentTable = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Tabla: ")) {
                    currentTable = line.substring(7);
                    borrarDatosTabla(conn, currentTable);
                } else if (!line.isEmpty() && currentTable != null) {
                    insertarDatosTabla(conn, currentTable, line);
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void borrarDatosTabla(Connection conn, String tabla) throws SQLException {
        String sql = "DELETE FROM " + tabla;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }
    private void insertarDatosTabla(Connection conn, String tabla, String datos) throws SQLException {
        String[] valores = datos.split("\\|");
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            placeholders.append("?");
            if (i < valores.length - 1) {
                placeholders.append(", ");
            }
        }
        String sql = "INSERT INTO " + tabla + " VALUES (" + placeholders.toString() + ")";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                if ("null".equalsIgnoreCase(valores[i])) {
                    pstmt.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    pstmt.setString(i + 1, valores[i]);
                }
            }
            pstmt.executeUpdate();
        }
    }

    public String getNombreLocal() {
        return menuPrincipal.nombreLocalText.getText();
    }

    public String getDireccionLocal() {
        return menuPrincipal.direccionLocalText.getText();
    }

    public String getContactoLocal() {
        return menuPrincipal.contactoLocalText.getText();
    }

    public String getMensajeLocal() {
        return menuPrincipal.mensajeLocalText.getText();
    }
}
