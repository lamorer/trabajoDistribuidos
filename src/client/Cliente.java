package client;

import java.net.Socket;
import java.util.Scanner;
import domain.*;
import java.io.*;
import java.util.List;

public class Cliente {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 6000;
    private static Jugador jugador;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Introduce el nombre del jugador:");
            String nomJugador = scanner.nextLine();
            jugador = new Jugador(nomJugador);

            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                Boolean puedeJugar = in.readBoolean();

                if (puedeJugar) {
                    out.writeBytes(nomJugador + "\n");
                    out.flush();

                    System.out.println("Obtengo mis cartas: ");
                    List<Carta> cartasJugador = (List<Carta>) in.readObject();

                    // Procesa e imprime las cartas de todos los jugadores
                    for (Carta carta : cartasJugador) {
                        System.out.println(carta.toString());
                    }

                    out.writeBytes("Cartas recibidas!\n");
                    out.flush();
                    int opcion;
                    for (int i = 0; i < 4; i++) {
                        boolean fin = in.readBoolean();
                        while (!fin) {
                            // Recibo la información de la mesa
                            String linea;
                            while (!(linea = in.readLine()).equals(".")) {
                                System.out.println(linea);
                            }
                            // Recibo el menú de elección

                            while (!(linea = in.readLine()).equals(".")) {
                                System.out.println(linea);
                            }

                            opcion = scanner.nextInt();
                            scanner.nextLine();
                            out.writeInt(opcion);
                            out.flush();

                            if (opcion == 3) {
                                boolean cantCorrecta = false;
                                while (!cantCorrecta) {
                                    System.out.println(in.readLine());
                                    int cant = scanner.nextInt();
                                    scanner.nextLine();
                                    out.writeInt(cant);
                                    out.flush();
                                    cantCorrecta = in.readBoolean();
                                    if (!cantCorrecta) {
                                        System.out.println(in.readLine());
                                    }
                                }
                            }
                            fin = in.readBoolean();
                        }

                        // RECIBIR INFORMACIÓN DE GANADOR
                        String ganador;
                        while ((ganador = in.readLine()) != ".") {
                            System.out.println(ganador);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}