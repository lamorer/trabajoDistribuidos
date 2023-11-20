package server;

import java.util.concurrent.*;
import domain.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int NUMERO_JUGADORES_ESPERADOS = 3;
    private static final int PORT = 6000;
    private static Baraja baraja;
    private static Mesa mesa;
    private static CountDownLatch countDownLatch = new CountDownLatch(NUMERO_JUGADORES_ESPERADOS);
    private static boolean partidaIniciada = false;
    private static boolean cartasRepatidas = false;
    private static boolean ciegas = false;
    private static int indiceJugadorActual = 0;
    private static boolean todosHablaron = false;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        baraja = new Baraja();
        baraja.barajar();

        try (ServerSocket servidor = new ServerSocket(PORT)) {
            System.out.println("Servidor de poker en funcionamiento en el puerto " + PORT);
            mesa = new Mesa();
            while (true) {
                try {
                    Socket cliente = servidor.accept();
                    //Añadir tantos accept como clientes quiera tener por partidas
                    ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());

                    pool.execute(new Runnable() {
                        public void run() {
                            atenderPeticion(in, out);    //Cada hilo es una partida abierta
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
        String nomJugador = conectarJugadores(in, out);
        iniciarPartida();
        ponerCiegas();
        repartirCartas(out, in, nomJugador);
        while(!todosHablaron){
            hablar(in, out, nomJugador);
        }
        enviarInformacionMesa(in, out);
        System.out.println(mesa.informacionMesa());
    }

    public static String conectarJugadores(ObjectInputStream in, ObjectOutputStream out) {
        try {
            String nomJugador = "";
            if (countDownLatch.getCount() > 0) {
                out.writeBoolean(true);
                out.flush();
                nomJugador = in.readLine();
                Jugador j = new Jugador(nomJugador);

                synchronized (mesa) {
                    mesa.agregarJugador(j);
                }
                countDownLatch.countDown();
            } else {
                out.writeBoolean(false);
                out.flush();
                System.out.println("La partida ya ha comenzado!");
            }
            return nomJugador;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void iniciarPartida() {
        try {
            countDownLatch.await();
            synchronized (mesa) {
                if (!partidaIniciada) {
                    System.out.println("Comienza la partida!");
                    mesa.establecerOrdenInicial();
                    System.out.println("Orden de juego inicial:");
                    List<Jugador> jugadoresOrdenados = mesa.getJugadores();
                    for (int i = 0; i < jugadoresOrdenados.size(); i++) {
                        System.out.println((i + 1) + ": " + jugadoresOrdenados.get(i).getNombre());
                    }
                }
                partidaIniciada = true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void ponerCiegas() {
        List<Jugador> jugadores = mesa.getJugadores();
        if (!ciegas) {
            mesa.apostar(10, jugadores.get(0));
            mesa.apostar(20, jugadores.get(1));
            if (mesa.getFichas() != 0) {
                System.out.println(mesa.getFichas());
            }
            for (Jugador j : jugadores) {
                System.out.println(j.getNombre() + ": " + j.getFichas());
            }
            ciegas = true;
        }
    }

    private static void repartirCartas(ObjectOutputStream out, ObjectInputStream in, String nomJugador) {
        try {
            synchronized (baraja) {
                if (!cartasRepatidas) {
                    mesa.repartirCartasIniciales(baraja);
                }
                cartasRepatidas = true;
            }
            List<Carta> cartasJugador = new ArrayList<>();

            for (Jugador j : mesa.getJugadores()) {
                if (j.getNombre().equals(nomJugador)) {
                    cartasJugador = Arrays.asList(j.getCartas());
                }
            }

            out.writeObject(cartasJugador); // Envía la lista de cartas de todos los jugadores
            out.flush();
            String confirmacion = in.readLine();
            if (confirmacion != null) { // Si el jugador no ha podido entrar en la partida, obtendremos un null
                System.out.println(confirmacion);
            }

            cartasRepatidas = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void hablar(ObjectInputStream in, ObjectOutputStream out, String nomJugador) {
        try {
            synchronized(mesa){
                List<Jugador> jugadores = mesa.getJugadores();
                Jugador j = jugadores.get(indiceJugadorActual);
                if (j.getNombre().equals(nomJugador)) {
                    enviarInformacionMesa(in, out);
                    String mensaje = "Turno de " + j.getNombre() + ". Elige una opción:\n" +
                            "1. No ir\n" +
                            "2. Igualar\n" +
                            "3. Subir\n" +
                            "Ingresa el número correspondiente a tu elección:\n" + ".\n";

                    out.writeBytes(mensaje);
                    out.flush();

                    int opcion = in.readInt();
                    switch (opcion) {
                        case 1:
                            noIr(nomJugador);
                            break;
                        case 2:
                            System.out.println("Elección de " + nomJugador + ": Igualar");
                            break;
                        case 3:
                            System.out.println("Elección de " + nomJugador + ": Subir");
                            break;
                        default:
                            System.out.println("Opción no válida. Inténtalo de nuevo.");
                            break;
                    }
                
                    indiceJugadorActual++;
                    if(indiceJugadorActual==jugadores.size()){
                        todosHablaron=true;
                        indiceJugadorActual=0;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void enviarInformacionMesa(ObjectInputStream in, ObjectOutputStream out){
        try{
            out.writeBytes(mesa.informacionMesa());
            out.writeBytes(".\n");
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void noIr(String nomJugador){
        for(Jugador j: mesa.getJugadores()){
            if(j.getNombre().equals(nomJugador)){
                mesa.setOrdenJugador(j, 0);
            }
        }
        
    }
}
