package server;

import java.util.concurrent.*;
import domain.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int NUMERO_JUGADORES_ESPERADOS = 4;
    private static final int PORT = 6000;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        List<Socket> clientes = new ArrayList<>();

        try (ServerSocket servidor = new ServerSocket(PORT)) {
            System.out.println("Servidor de poker en funcionamiento en el puerto " + PORT);
            while (true) {
                Mesa mesa = new Mesa(); // Crearemos tantas mesas y barajas como partidas se abran
                Baraja baraja = new Baraja();
                baraja.barajar();
                try {
                    for (int i = 0; i < NUMERO_JUGADORES_ESPERADOS; i++) { // Dejamos conectar a 3 jugadores
                        Socket cliente = servidor.accept();
                        clientes.add(cliente);
                    }
                    pool.execute(new Runnable() {
                        public void run() {
                            atenderPeticion(clientes, mesa, baraja);
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

    public static void atenderPeticion(List<Socket> clientes, Mesa mesa, Baraja baraja) {
        HashMap<String, JugadorInfo> jugadores = conectarJugadores(clientes, mesa);
        iniciarPartida(mesa);
        ponerCiegas(mesa);
        repartirCartas(jugadores, mesa, baraja);
        hablarPrimeraRonda(jugadores, mesa);
        for (int i = 0; i < 3; i++) { // FLOP
            mesa.agregarCartaAMesa(baraja.repartirCarta());
        }
        hablar(jugadores, mesa);
        mesa.agregarCartaAMesa(baraja.repartirCarta()); // TURN
        hablar(jugadores, mesa);
        mesa.agregarCartaAMesa(baraja.repartirCarta()); // RIVER
        hablar(jugadores, mesa);
        finalizarRonda(jugadores, mesa);
        System.out.println(mesa.informacionMesa());
    }

    public static HashMap<String, JugadorInfo> conectarJugadores(List<Socket> clientes, Mesa mesa) {
        String nomJugador = "";
        HashMap<String, JugadorInfo> jugadoresConectados = new HashMap<>();
        for (Socket cliente : clientes) {
            try {
                ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
                out.writeBoolean(true);
                out.flush();
                nomJugador = in.readLine();
                Jugador j = new Jugador(nomJugador);
                mesa.agregarJugador(j);
                jugadoresConectados.put(nomJugador, new JugadorInfo(in, out));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jugadoresConectados;
    }

    private static void iniciarPartida(Mesa mesa) {
        System.out.println("Comienza la partida!");
        mesa.establecerOrdenInicial();
        System.out.println("Orden de juego inicial:");
        List<Jugador> jugadoresOrdenados = mesa.getJugadores();
        for (int i = 0; i < jugadoresOrdenados.size(); i++) {
            System.out.println((i + 1) + ": " + jugadoresOrdenados.get(i).getNombre());
        }
    }

    public static void ponerCiegas(Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        mesa.apostar(10, jugadores.get(0));
        mesa.apostar(20, jugadores.get(1));
        if (mesa.getFichas() != 0) {
            System.out.println(mesa.getFichas());
        }
        for (Jugador j : jugadores) {
            System.out.println(j.getNombre() + ": " + j.getFichas());
        }
    }

    private static void repartirCartas(HashMap<String, JugadorInfo> clientes, Mesa mesa, Baraja baraja) {
        mesa.repartirCartasIniciales(baraja);
        List<Carta> cartasJugador = new ArrayList<>();
        JugadorInfo s;
        for (Jugador j : mesa.getJugadores()) {
            s = clientes.get(j.getNombre());
            try {
                ObjectOutputStream out = s.getOutputStream();
                ObjectInputStream in = s.getInputStream();
                cartasJugador = Arrays.asList(j.getCartas());
                out.writeObject(cartasJugador); // Envía la lista de cartas de todos los jugadores
                out.flush();
                String confirmacion = in.readLine();
                System.out.println(confirmacion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void hablarPrimeraRonda(HashMap<String, JugadorInfo> clientes, Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        int indicePrimerJugador = 2; // Índice del tercer jugador (BOTÓN)

        for (int i = 0; i < jugadores.size(); i++) {
            int indiceJugador = (indicePrimerJugador + i) % jugadores.size();
            Jugador j = jugadores.get(indiceJugador);
            JugadorInfo s = clientes.get(j.getNombre());
            try {
                ObjectOutputStream out = s.getOutputStream();
                ObjectInputStream in = s.getInputStream();
                enviarInformacionMesa(in, out, mesa);
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
                        noIr(j, mesa);
                        System.out.println("Elección de " + j.getNombre() + ": No ir");
                        break;
                    case 2:
                        System.out.println("Elección de " + j.getNombre() + ": Igualar");
                        break;
                    case 3:
                        System.out.println("Elección de " + j.getNombre() + ": Subir");
                        break;
                    default:
                        System.out.println("Opción no válida. Inténtalo de nuevo.");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void hablar(HashMap<String, JugadorInfo> clientes, Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        JugadorInfo s;
        for (Jugador j : jugadores) {
            s = clientes.get(j.getNombre());
            try {
                ObjectOutputStream out = s.getOutputStream();
                ObjectInputStream in = s.getInputStream();
                enviarInformacionMesa(in, out, mesa);
                if(mesa.getJugadoresOrden().get(j)!=0){
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
                            noIr(j, mesa);
                            System.out.println("Elección de " + j.getNombre() + ": No ir");
                            break;
                        case 2:
                            igualar(j, mesa);
                            System.out.println("Elección de " + j.getNombre() + ": Igualar");
                            break;
                        case 3:
                            System.out.println("Elección de " + j.getNombre() + ": Subir");
                            boolean cantCorrecta = false; 
                            int cant = 0;
                            while(!cantCorrecta){
                                out.writeBytes("¿Cuánto quieres subir?\n");
                                out.flush();
                                cant = in.readInt();
                                
                                if(cant<mesa.getApuesta()){
                                    cantCorrecta = false;                   //MIRAR A VER CUÁL ES LA CANTIDAD CORRECTA PARA SUBIR
                                    out.writeBoolean(cantCorrecta);
                                    out.flush();
                                } else{
                                    cantCorrecta = true;
                                    out.writeBoolean(cantCorrecta);
                                    out.flush();
                                    out.writeBytes("Debes subir por lo menos a "+mesa.getApuesta()+"\n");
                                    out.flush();
                                }                                
                            }
                            subir(j,mesa,cant);
                            break;
                        default:
                            System.out.println("Opción no válida. Inténtalo de nuevo.");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void finalizarRonda(HashMap<String, JugadorInfo> clientes, Mesa mesa) {}

    public static void enviarInformacionMesa(ObjectInputStream in, ObjectOutputStream out, Mesa mesa) {
        try {
            out.writeBytes(mesa.informacionMesa());
            out.writeBytes(".\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void noIr(Jugador j, Mesa mesa) {
        mesa.setOrdenJugador(j, 0);
    }

    public static void igualar(Jugador j, Mesa mesa){
        mesa.apostar(mesa.getApuesta(), j);
    }

    public static void subir(Jugador j, Mesa mesa, int cant){
        mesa.apostar(cant,j);
        mesa.setApuesta(cant);
    }
}
