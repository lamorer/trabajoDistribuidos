package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import domain.*;

public class Servidor {
    private static final int PORT = 6000;
    private static Baraja baraja;
    private static Mesa mesa;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        baraja = new Baraja();
        baraja.barajar();
        System.out.println(baraja.getSize());
        try (ServerSocket servidor = new ServerSocket(PORT)) {
            System.out.println("Servidor de poker en funcionamiento en el puerto " + PORT);
            while (true) {
                try {
                    Socket cliente = servidor.accept();
                    pool.execute(new Runnable() {
                        public void run() {
                            atenderPeticion(cliente);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

    public static void atenderPeticion(Socket cliente) {
        try (ObjectOutputStream dos = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream dis = new ObjectInputStream(cliente.getInputStream())) {
            Carta carta1;
            Carta carta2;
            synchronized (baraja) {
                carta1 = baraja.repartirCarta();
                carta2 = baraja.repartirCarta();
            }
            dos.writeObject(carta1);
            dos.writeObject(carta2);
            dos.flush();
            System.out.println(baraja.getSize());

            // Espera a que el cliente confirme que recibió la carta
            System.out.println(dis.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
