package client;

import java.net.Socket;
import java.util.Scanner;
import domain.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Cliente {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 6000;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Introduce el nombre del jugador:");
            String nomJugador = scanner.nextLine();

            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                Boolean puedeJugar = in.readBoolean();
                System.out.println(puedeJugar);

                if (puedeJugar) {
                    out.writeBytes(nomJugador + "\n");
                    out.flush();

                    System.out.println("Obtengo mis cartas: ");
                    List<List<Carta>> cartasJugadores = (List<List<Carta>>) in.readObject();

                    // Procesa e imprime las cartas de todos los jugadores
                    for (List<Carta> cartas : cartasJugadores) {
                        System.out.println(Arrays.toString(cartas.toArray()));
                    }

                    out.writeBytes("Cartas recibidas!\n");
                    out.flush();
                } else {
                    System.out.println("Ya no puedes unirte a la mesa, la partida ya ha comenzado!");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
