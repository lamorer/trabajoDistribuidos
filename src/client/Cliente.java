package client;

import java.net.Socket;
import java.util.Scanner;

import domain.Carta;
import domain.Jugador;

import java.io.*;

public class Cliente {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 6000;
    private static Jugador jugador;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner=new Scanner(System.in);) {
            System.out.println("Introduce el nombre del jugador:");
            String nomJugador = scanner.nextLine();
            jugador=new Jugador(nomJugador);
                        
            Carta c1 = (Carta) in.readObject();
            System.out.println(c1.toString());
            Carta c2 = (Carta) in.readObject();
            System.out.println(c2.toString());

            jugador.recibirCartas(c1, c2);
            jugador.mostrarCartas();
            
            // Envía confirmación al servidor
            out.writeBytes("Cartas recibidas!");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
