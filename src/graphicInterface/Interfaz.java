package graphicInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interfaz extends JFrame {

    private JTextField nombreTextField;

    public Interfaz() {
        // Configuración del JFrame
        setTitle("Interfaz con Botones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new BorderLayout());

        // Crear el área de texto para el nombre
        nombreTextField = new JTextField();
        add(nombreTextField, BorderLayout.NORTH);

        // Crear los botones
        JButton noIrButton = new JButton("No ir");
        JButton pasarButton = new JButton("Pasar");
        JButton subirButton = new JButton("Subir");

        // Agregar oyentes de eventos a los botones
        noIrButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Acciones cuando se hace clic en el botón "No ir"
                JOptionPane.showMessageDialog(Interfaz.this, "No ir pulsado");
            }
        });

        pasarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Acciones cuando se hace clic en el botón "Pasar"
                JOptionPane.showMessageDialog(Interfaz.this, "Pasar pulsado");
            }
        });

        subirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Acciones cuando se hace clic en el botón "Subir"
                JOptionPane.showMessageDialog(Interfaz.this, "Subir pulsado");
            }
        });

        // Crear un panel para contener los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(noIrButton);
        buttonPanel.add(pasarButton);
        buttonPanel.add(subirButton);

        // Agregar el panel de botones al JFrame
        add(buttonPanel, BorderLayout.SOUTH);

        // Hacer visible la interfaz
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Interfaz();
            }
        });
    }
}
   
