package InterfazGrafica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MenuPrincipal extends JFrame {
    private JPanel mainPanel;
    private JButton ajustesButton;
    private JButton salirButton;
    private JButton cajaButton;
    private JButton categoriasButton;
    private JButton productosButton;
    private JButton proveedoresButton;
    private JButton clientesButton;
    private JButton nuevaVentaButton;
    private JTabbedPane tabbedPane1;
    private JTable historialTable;
    private JPanel vesTitePanel;
    private JPanel botonesPanel;
    private JPanel extraPanel;
    private JPanel datosPanel;
    private JButton abrirCajaButton;
    private JButton cerrarCajaButton;
    private JTextField agregarDineroText;
    private JTextField quitarDineroText;
    private JButton actualizarButton;
    private JTextField BuscarFechaText;
    private JButton buscarFechButton;

    public MenuPrincipal(){
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();


        String[] columnNames = {"Fecha", "Monto Inicial", "Entrada", "Salida",  "Nota"};
        DefaultTableModel model = new DefaultTableModel(columnNames,0);
        historialTable.setModel(model);

    }


}
