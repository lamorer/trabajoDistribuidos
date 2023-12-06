package server;

import java.util.concurrent.*;
import domain.*;
import services.ReglasPoker;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int NUMERO_JUGADORES_ESPERADOS = 2;
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
                    for (int i = 0; i < NUMERO_JUGADORES_ESPERADOS; i++) { // Dejamos conectar a 6 jugadores
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
        Jugador ganador = null;
        iniciarPartida(mesa);
        for (Jugador j : mesa.getJugadoresVivos()) {
            System.out.println(j.getNombre());
        }
        while (mesa.getGanador() == null) {
            inicializarApuestas(mesa);
            ponerCiegas(mesa);
            repartirCartas(jugadores, mesa, baraja);
            mesa.setApuesta(mesa.getCiegaGrande());
            ganador = hablar(jugadores, mesa);
            if (ganador == null) {
                for (int i = 0; i < 3; i++) { // FLOP
                    mesa.agregarCartaAMesa(baraja.repartirCarta());
                }
                inicializarApuestas(mesa);
                ganador = hablar(jugadores, mesa);
                if (ganador == null) {
                    mesa.agregarCartaAMesa(baraja.repartirCarta()); // TURN
                    System.out.println(mesa.informacionMesa());
                    inicializarApuestas(mesa);
                    ganador = hablar(jugadores, mesa);
                    
                    if (ganador == null) {
                        mesa.agregarCartaAMesa(baraja.repartirCarta()); // RIVER
                        System.out.println(mesa.informacionMesa());
                        inicializarApuestas(mesa);
                        ganador = hablar(jugadores, mesa);
                    } else {
                        enviarBool(jugadores, true);
                    }
                } else {
                    enviarBool(jugadores, true);
                }
            } else {
                enviarBool(jugadores, true);
            }
            finalizarRonda(jugadores, mesa, ganador);
            System.out.println(mesa.informacionMesa());
            mesa.comprobarGanador();
            mesa.establecerOrdenSiguienteRonda();
            enviarBool(jugadores,mesa.getGanador()!=null);
        }
        System.out.println("GANADOR: " + mesa.getGanador());
    }

    public static void enviarBool(HashMap<String, JugadorInfo> jugadores, boolean b) {
        for (JugadorInfo j : jugadores.values()) {
            try {
                ObjectOutputStream out = j.getOutputStream();
                out.writeBoolean(b);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void inicializarApuestas(Mesa mesa) {
        mesa.setApuesta(0);
        mesa.setFichas(0);
        List<Jugador> jugadores = mesa.getJugadores();
        for (Jugador j : jugadores) {
            j.setApuesta(0);
        }
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
        for (Jugador j1 : mesa.getJugadores()) {
            System.out.println(j1.getNombre());
        }
    }

    public static void ponerCiegas(Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        mesa.apostar(mesa.getCiegaPequena(), jugadores.get(0));
        mesa.apostar(mesa.getCiegaGrande(), jugadores.get(1));
        if (mesa.getFichas() != 0) {
            System.out.println(mesa.getFichas());
        }
        for (Jugador j : jugadores) {
            System.out.println(j.getNombre() + ": " + j.getFichas());
        }
    }

    private static void repartirCartas(HashMap<String, JugadorInfo> clientes, Mesa mesa, Baraja baraja) {
        mesa.repartirCartasIniciales(baraja);
        mesa.setRonda(1);
        List<Carta> cartasJugador = new ArrayList<>();
        JugadorInfo s;
        for (Jugador j : mesa.getJugadores()) {
            s = clientes.get(j.getNombre());
            try {
                ObjectOutputStream out = s.getOutputStream();
                cartasJugador = Arrays.asList(j.getCartas());
                out.writeObject(cartasJugador); // Envía la lista de cartas de todos los jugadores
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Jugador hablar(HashMap<String, JugadorInfo> clientes, Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        int indicePrimerJugador = 0;
        if (mesa.getRonda() == 1) {
            indicePrimerJugador = 2; // Índice del tercer jugador (BOTÓN)
        }
        Jugador ganador;
        boolean fin = false;
        boolean hablaronTodos = false;
        // Hablan todos una primera vez
        for (int i = 0; i < jugadores.size(); i++) {
            try {
                int indiceJugador = (indicePrimerJugador + i) % jugadores.size();
                Jugador j = jugadores.get(indiceJugador);
                JugadorInfo s = clientes.get(j.getNombre());
                ObjectOutputStream out = s.getOutputStream();
                out.writeBoolean(fin);
                out.flush();
                accionJugador(s, mesa, j, mesa.getRonda());
                ganador = comprobar(mesa);
                if (ganador != null) {
                    System.out.print("Ganador: ");
                    ganador.mostrarCartas();
                    return ganador;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        hablaronTodos = true;
        fin = true;
        for (Jugador j : jugadores) {
            if (mesa.getJugadoresOrden().get(j) != 0 && j.getApuesta() != mesa.getApuesta() && j.getFichas() != 0) {
                fin = false;
            }
        }
        // Si alguien falta por hablar, entramos en el bucle
        while (!(fin && hablaronTodos)) {
            for (int i = 0; i < jugadores.size(); i++) {
                try {
                    int indiceJugador = (indicePrimerJugador + i) % jugadores.size();
                    Jugador j = jugadores.get(indiceJugador);
                    JugadorInfo s = clientes.get(j.getNombre());
                    ObjectOutputStream out = s.getOutputStream();
                    if (j.getApuesta() != mesa.getApuesta()) {
                        out.writeBoolean(hablaronTodos & fin);
                        out.flush();
                        accionJugador(s, mesa, j, mesa.getRonda());
                        ganador = comprobar(mesa);
                        if (ganador != null) {
                            System.out.print("Ganador: ");
                            ganador.mostrarCartas();
                            return ganador;
                        }
                    } else {
                        out.writeBoolean(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            hablaronTodos = true;
            fin = true;
            for (Jugador j : jugadores) {
                if (mesa.getJugadoresOrden().get(j) != 0 && j.getApuesta() < mesa.getApuesta()) {
                    fin = false;
                }
            }
        }
        System.out.println("FIN DE LA RONDA");
        mesa.siguienteRonda();
        return null;
    }

    public static void accionJugador(JugadorInfo s, Mesa mesa, Jugador j, int ronda) {
        try {
            ObjectOutputStream out = s.getOutputStream();
            ObjectInputStream in = s.getInputStream();
            enviarInformacionMesa(in, out, mesa);
            boolean juega = mesa.getJugadoresOrden().get(j) != 0 && j.getFichas() != 0; // Si es 0, el jugador ya no
                                                                                        // está en la mano
            out.writeBoolean(juega);
            out.flush();
            if (juega) {
                String igualarPasar = "Igualar";
                if ((mesa.getRonda() == 1 && j.getApuesta() == mesa.getCiegaGrande()
                        && mesa.getApuesta() == mesa.getCiegaGrande())
                        || (mesa.getRonda() != 1 && mesa.getApuesta() == 0)) {
                    igualarPasar = "Pasar";
                }
                System.out.println("RONDA " + mesa.getRonda());
                String mensaje = "Turno de " + j.getNombre() + ". Elige una opción:\n" +
                        "1. No ir\n" +
                        "2. " + igualarPasar + "\n" +
                        "3. Subir\n" +
                        "Ingresa el número correspondiente a tu elección:\n" + ".\n";

                out.writeBytes(mensaje);
                out.flush();

                int opcion = 0;
                while (opcion > 3 || opcion < 1) {
                    opcion = in.readInt();
                    switch (opcion) {
                        case 1:
                            noIr(j, mesa);
                            System.out.println("Elección de " + j.getNombre() + ": No ir");
                            break;
                        case 2:
                            igualarPasar(j, mesa, igualarPasar);
                            System.out.println("Elección de " + j.getNombre() + ": " + igualarPasar);
                            break;
                        case 3:
                            System.out.println("Elección de " + j.getNombre() + ": Subir");
                            boolean cantCorrecta = false;
                            int cant = 0;
                            int ap;
                            while (!cantCorrecta) {
                                out.writeBytes("¿Cuánto quieres subir?\n");
                                out.flush();
                                cant = in.readInt();
                                ap = mesa.getApuesta() + mesa.getCiegaGrande();
                                if ((cant >= ap && j.getFichas() >= cant) || j.getFichas() == cant) {
                                    cantCorrecta = true;
                                    out.writeBoolean(cantCorrecta);
                                    out.flush();
                                } else {
                                    cantCorrecta = false;
                                    out.writeBoolean(cantCorrecta);
                                    out.flush();
                                    out.writeBytes("Debes subir por lo menos a " + ap + " o echar all-in" + "\n");
                                    out.flush();
                                }
                            }
                            subir(j, mesa, cant);
                            break;
                        default:
                            System.out.println("Opción no válida. Inténtalo de nuevo.");
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Jugador comprobar(Mesa mesa) {
        HashMap<Jugador, Integer> jugadoresOrden = mesa.getJugadoresOrden();
        List<Jugador> jugadores = mesa.getJugadores();
        int gana = 0;
        Jugador ganador = null;
        int i = 0;
        while (i < jugadores.size() && gana < 2) {
            if (jugadoresOrden.get(jugadores.get(i)) != 0) {
                gana++;
                ganador = jugadores.get(i);
            }
            i++;
        }
        if (gana == 1) {
            return ganador;
        } else {
            return null;
        }
    }

    public static void finalizarRonda(HashMap<String, JugadorInfo> clientes, Mesa mesa, Jugador ganador) {
        List<Jugador> ganadores = new ArrayList<>();
        if (ganador != null) {
            ganadores.add(ganador);
        } else {
            ganadores = ReglasPoker.determinarGanador(mesa);
        }
        int cant = mesa.getFichas() / ganadores.size();
        for (Jugador j : ganadores) {
            j.setFichas(j.getFichas() + cant);
        }
        for (JugadorInfo j : clientes.values()) {
            enviarInformacionJugadores(j.getInputStream(), j.getOutputStream(), mesa, ganadores);
        }
    }

    public static void enviarInformacionJugadores(ObjectInputStream in, ObjectOutputStream out, Mesa mesa,
            List<Jugador> ganadores) {
        try {
            if (ganadores.size() == 1) {
                out.writeBytes("---------------------GANADOR ");
            } else {
                out.writeBytes("---------------------GANADORES ");
            }
            for (Jugador j : ganadores) {
                out.writeBytes(j.getNombre() + "----------------------------\n");
                out.flush();
            }
            for (Jugador j : mesa.getJugadores()) {
                out.writeBytes(j.getNombre() + ": " + j.getFichas() + "\n");
                out.flush();
            }
            out.writeBytes(".\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static void igualarPasar(Jugador j, Mesa mesa, String igualarPasar) {
        if (igualarPasar.equals("Igualar")) {
            System.out.println("Ha apostado " + (mesa.getApuesta() - j.getApuesta()));
            mesa.apostar(mesa.getApuesta() - j.getApuesta(), j);
        }
    }

    public static void subir(Jugador j, Mesa mesa, int cant) {
        mesa.setApuesta(cant + j.getApuesta());
        mesa.apostar(cant, j);
    }
}