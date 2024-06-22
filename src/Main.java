import InterfazGrafica.MenuPrincipal;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new MenuPrincipal();
                    frame.setSize(1300, 650);
                    frame.setVisible(true);
                    frame.setLocationRelativeTo(null);
                }
            });
        System.out.println("hola");
    }
}