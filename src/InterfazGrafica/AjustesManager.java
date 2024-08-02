package InterfazGrafica;

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
