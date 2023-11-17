package server;

import java.util.concurrent.*;
import domain.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int NUMERO_JUGADORES_ESPERADOS = 2;
    private static final int PORT = 6000;
    private static Baraja baraja;
    private static Mesa mesa;
    private static CountDownLatch countDownLatch = new CountDownLatch(NUMERO_JUGADORES_ESPERADOS);
    private static boolean partidaIniciada = false;
    private static boolean cartasRepatidas = false;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        baraja = new Baraja();
        baraja.barajar();

        try (ServerSocket servidor = new ServerSocket(PORT)) {
            System.out.println("Servidor de poker en funcionamiento en el puerto " + PORT);
            mesa=new Mesa();
            while (true) {
                try {
                    Socket cliente = servidor.accept();
                    ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());

                    pool.execute(new Runnable() {
                        public void run() {
                            atenderPeticion(in, out);
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

    public static void atenderPeticion(ObjectInputStream in, ObjectOutputStream out) {
        try {
            if (countDownLatch.getCount() > 0) {
                out.writeBoolean(true);
                out.flush();
                String nomJugador = in.readLine();
                Jugador j = new Jugador(nomJugador);

                synchronized (mesa) {
                    mesa.agregarJugador(j);
                }
                countDownLatch.countDown();
                System.out.println("Quedan " + countDownLatch.getCount() + " jugadores por unirse.");
            } else {
                out.writeBoolean(false);
                out.flush();
                System.out.println("La partida ya ha comenzado!");
            }
            // if(countDownLatch.getCount()==0){
            //     for(Jugador j: mesa.getJugadores()){
            //         System.out.println(j.getNombre());
            //     }
            // }
            iniciarPartida(out, in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void iniciarPartida(ObjectOutputStream out, ObjectInputStream in) {
        try{
            System.out.println("Comienza la partida!");
            countDownLatch.await();
            synchronized (mesa) {
            if (!partidaIniciada) {
                    mesa.establecerOrdenInicial();
                    System.out.println("Orden de juego inicial:");
                    List<Jugador> jugadoresOrdenados = mesa.getJugadores();
                    for (int i = 0; i < jugadoresOrdenados.size(); i++) {
                        System.out.println((i + 1) + ": " + jugadoresOrdenados.get(i).getNombre());
                    }
                }
            partidaIniciada = true;
            }
            jugarMano(out, in);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }


    private static void jugarMano(ObjectOutputStream out, ObjectInputStream in) {
        try {
            synchronized (baraja) {
            if (!cartasRepatidas) {
                
                    mesa.repartirCartasIniciales(baraja);
                }
            cartasRepatidas=true;
            }
            List<List<Carta>> cartasJugadores = new ArrayList<>();

            for (Jugador j : mesa.getJugadores()) {
                cartasJugadores.add(Arrays.asList(j.getCartas()));
            }

            out.writeObject(cartasJugadores); // Envía la lista de cartas de todos los jugadores
            out.flush();

            System.out.println(in.readLine());
            cartasRepatidas = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
