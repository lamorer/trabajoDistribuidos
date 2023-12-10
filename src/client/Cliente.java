package client;

import java.net.Socket;
import java.util.Scanner;
import domain.*;
import java.io.*;
import java.util.List;

public class Cliente {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 6000;

    // PRE: --
    // POST: Inicia el cliente, establece conexión con el servidor y participa en el
    // juego.
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Esperando a que se conecten el resto de jugadores...");
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Comienza la interacción con el servidor
                Boolean puedeJugar = in.readBoolean();
                if (puedeJugar) {
                    System.out.print("Introduce el nombre del jugador: ");
                    String nomJugador = scanner.nextLine();
                    out.writeBytes(nomJugador + "\n");
                    out.flush();
                    boolean nomRep = in.readBoolean();
                    while (nomRep) {
                        System.out.print("Nombre ya registrado. Introduce otro nombre: ");
                        nomJugador = scanner.nextLine();
                        out.writeBytes(nomJugador + "\n");
                        out.flush();
                        nomRep = in.readBoolean();
                    }

                    boolean hayGanador = false;
                    while (!hayGanador) { // Si hay un ganador final de la partida, no recibimos más cartas.
                        System.out.println("Obtengo mis cartas: ");
                        List<Carta> cartasJugador = (List<Carta>) in.readObject();

                        // Muestra las cartas del jugador
                        for (Carta carta : cartasJugador) {
                            System.out.println(carta.toString());
                        }

                        // Verifica si el jugador está en All-In
                        boolean enAllIn = in.readBoolean();
                        if (!enAllIn) {
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
                                    boolean juega = in.readBoolean();
                                    if (juega) {
                                        while (!(linea = in.readLine()).equals(".")) {
                                            System.out.println(linea);
                                        }

                                        // El jugador elige una opción
                                        opcion = scanner.nextInt();
                                        scanner.nextLine();
                                        out.writeInt(opcion);
                                        out.flush();

                                        // Si la opción es 3 (apostar), se solicita la cantidad
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
                                    }
                                    fin = in.readBoolean();
                                }
                            }
                        }

                        // Recibe la información del ganador
                        String ganador;
                        while (!(ganador = in.readLine()).equals(".")) {
                            System.out.println(ganador);
                        }

                        // Verifica si hay un ganador final
                        hayGanador = in.readBoolean();
                    }

                    // Muestra el ganador final
                    String ganadorFinal;
                    while (!(ganadorFinal = in.readLine()).equals(".")) {
                        System.out.println(ganadorFinal);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
