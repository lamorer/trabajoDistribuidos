package graphicInterface;

import javax.swing.*;
import java.awt.*;

public class Interfaz extends JFrame {
    private JLabel[] communityCards;
    private JLabel[][] playerCards;
    private JLabel[] playerChips;

    public Interfaz() {
        setTitle("Mesa de Poker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Zona de cartas comunitarias
        JPanel communityCardsPanel = new JPanel();
        communityCards = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            communityCards[i] = new JLabel("?");
            communityCardsPanel.add(communityCards[i]);
        }
        add(communityCardsPanel, BorderLayout.CENTER);

        // Posiciones de los jugadores
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new GridLayout(0, 4)); // Puedes ajustar el número de jugadores
        playerCards = new JLabel[4][2];
        playerChips = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            JPanel playerInfo = new JPanel();
            playerInfo.setLayout(new BoxLayout(playerInfo, BoxLayout.Y_AXIS));

            for (int j = 0; j < 2; j++) {
                playerCards[i][j] = new JLabel("?");
                playerInfo.add(playerCards[i][j]);
            }

            playerChips[i] = new JLabel("Chips: 1000"); // Reemplaza con los valores reales de fichas
            playerInfo.add(playerChips[i]);

            playersPanel.add(playerInfo);
        }
        add(playersPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Interfaz();
        });
    }
}
