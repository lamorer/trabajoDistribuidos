package server;

import java.util.concurrent.*;
import domain.*;
import services.ReglasPoker;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    private static final int NUMERO_JUGADORES_ESPERADOS = 6;
    private static final int PORT = 6000;

    // PRE: --
    // POST: Se inicia el servidor y se espera la conexión de jugadores para iniciar
    // partidas.
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        List<Socket> clientes = new ArrayList<>();

        try (ServerSocket servidor = new ServerSocket(PORT)) {
            System.out.println("Servidor de poker en funcionamiento en el puerto " + PORT);
            while (true) {
                Mesa mesa = new Mesa(); // Crearemos tantas mesas y barajas como partidas se abran
                try {
                    for (int i = 0; i < NUMERO_JUGADORES_ESPERADOS; i++) { // Dejamos conectar a 6 jugadores
                        Socket cliente = servidor.accept();
                        clientes.add(cliente);
                    }
                    pool.execute(new Runnable() {
                        public void run() {
                            atenderPeticion(clientes, mesa);
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

    // PRE: clientes contiene conexiones de clientes válidas. mesa es una instancia
    // válida de la clase Mesa.
    // POST: Atiende la petición de los jugadores y maneja la lógica de la partida.
    public static void atenderPeticion(List<Socket> clientes, Mesa mesa) {
        HashMap<String, JugadorInfo> jugadores = conectarJugadores(clientes, mesa);
        Jugador ganador = null;
        iniciarPartida(mesa);
        Baraja baraja;
        while (mesa.getGanador() == null) {
            baraja = new Baraja();
            baraja.barajar();
            mesa.limpiarMesa();
            inicializarApuestas(mesa);
            mesa.setFichas(0);
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
            enviarBool(jugadores, mesa.getGanador() != null);
        }
        System.out.println("GANADOR: " + mesa.getGanador().getNombre());
        List<Jugador> ganadorFinal = new ArrayList<>();
        ganadorFinal.add(mesa.getGanador());
        for (JugadorInfo j : jugadores.values()) {
            enviarInformacionJugadores(j.getInputStream(), j.getOutputStream(), mesa, ganadorFinal);
        }
    }

    // PRE: jugadores contiene información válida de los jugadores.
    // POST: Envía un booleano a todos los jugadores conectados.
    public static void enviarBool(HashMap<String, JugadorInfo> jugadores, boolean b) {
        Collection<JugadorInfo> jugadoresInfo = jugadores.values();
        for (JugadorInfo j : jugadoresInfo) {
            try {
                ObjectOutputStream out = j.getOutputStream();
                out.writeBoolean(b);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // PRE: mesa es una instancia válida de la clase Mesa.
    // POST: Inicializa las apuestas en la mesa.
    public static void inicializarApuestas(Mesa mesa) {
        mesa.setApuesta(0);
        List<Jugador> jugadores = mesa.getJugadores();
        for (Jugador j : jugadores) {
            j.setApuesta(0);
        }
    }

    // PRE: clientes contiene conexiones de clientes válidas. mesa es una instancia
    // válida de la clase Mesa.
    // POST: Conecta a los jugadores al servidor. Se valida el nombre de los
    // jugadores para evitar repeticiones. La información de los jugadores se
    // almacena en jugadoresConectados.
    public static HashMap<String, JugadorInfo> conectarJugadores(List<Socket> clientes, Mesa mesa) {
        String nomJugador = "";
        HashMap<String, JugadorInfo> jugadoresConectados = new HashMap<>();
        for (Socket cliente : clientes) {
            try {
                ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
                out.writeBoolean(true);
                out.flush();
                boolean nomRep = true;
                while (nomRep) {
                    nomJugador = in.readLine();
                    nomRep = false;
                    List<Jugador> jug = mesa.getJugadores();
                    for (Jugador j : jug) {
                        if (nomJugador.equals(j.getNombre())) {
                            nomRep = true;
                        }
                    }
                    out.writeBoolean(nomRep);
                    out.flush();
                }
                Jugador j = new Jugador(nomJugador);
                mesa.agregarJugador(j);
                jugadoresConectados.put(nomJugador, new JugadorInfo(in, out));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jugadoresConectados;
    }

    // PRE: mesa es una instancia válida de la clase Mesa.
    // POST: Inicia la partida, estableciendo el orden inicial de juego.
    private static void iniciarPartida(Mesa mesa) {
        System.out.println("Comienza la partida!");
        mesa.establecerOrdenInicial();
        System.out.println("Orden de juego inicial:");
    }

    // PRE: mesa es una instancia válida de la clase Mesa.
    // POST: Coloca las ciegas en la mesa, realizando apuestas iniciales.
    public static void ponerCiegas(Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        mesa.apostar(mesa.getCiegaPequena(), jugadores.get(0));
        mesa.apostar(mesa.getCiegaGrande(), jugadores.get(1));
        if (mesa.getFichas() != 0) {
            System.out.println("Bote: " + mesa.getFichas());
        }
        for (Jugador j : jugadores) {
            System.out.println(j.getNombre() + ": " + j.getFichas());
        }
    }

    // PRE: clientes contiene información válida de los jugadores conectados. mesa
    // es una instancia válida de la clase Mesa. baraja es una instancia válida de
    // la clase Baraja.
    // POST: Reparte las cartas iniciales a los jugadores.
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

    // PRE: clientes contiene información válida de los jugadores conectados.
    // mesa es una instancia válida de la clase Mesa.
    // POST: Los jugadores realizan acciones durante la ronda, como apostar, igualar
    // o pasar.
    public static Jugador hablar(HashMap<String, JugadorInfo> clientes, Mesa mesa) {
        List<Jugador> jugadores = mesa.getJugadores();
        int indicePrimerJugador = 0;
        if (mesa.getRonda() == 1) {
            indicePrimerJugador = 2; // Índice del tercer jugador (BOTÓN)
        }
        Jugador ganador;
        boolean fin = false;
        boolean hablaronTodos = false;
        boolean enAllIn = false;
        for (Jugador j : mesa.getJugadoresVivos()) {
            if (j.getFichas() == 0) { // Si hay algún jugador en All-In se finaliza la ronda
                enAllIn = true;
            }
        }
        enviarBool(clientes, enAllIn);
        if (!enAllIn) {
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
                if (mesa.getJugadoresOrden().get(j) <= 10 && j.getApuesta() != mesa.getApuesta()
                        && j.getFichas() != 0) {
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
                            out.writeBoolean(hablaronTodos && fin);
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
                            out.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                hablaronTodos = true;
                fin = true;
                for (Jugador j : jugadores) {
                    if (mesa.getJugadoresOrden().get(j) <= 10 && j.getApuesta() < mesa.getApuesta()) {
                        fin = false;
                    }
                }
            }
        }
        System.out.println("FIN DE LA RONDA");
        mesa.siguienteRonda();
        return null;
    }

    // PRE: s es una instancia válida de JugadorInfo. mesa es una instancia válida
    // de la clase Mesa. j es una instancia válida de la clase Jugador. ronda es un
    // entero que indica la ronda actual.
    // POST: El jugador realiza una acción durante su turno.
    public static void accionJugador(JugadorInfo s, Mesa mesa, Jugador j, int ronda) {
        try {
            ObjectOutputStream out = s.getOutputStream();
            ObjectInputStream in = s.getInputStream();
            enviarInformacionMesa(in, out, mesa);
            boolean juega = true;
            if (juega) {
                juega = mesa.getJugadoresOrden().get(j) <= 10 && j.getFichas() != 0; // Si es mayor que 10, el jugador
                                                                                     // ya no está en la mano
            }
            out.writeBoolean(juega);
            out.flush();
            if (juega) {
                String igualarPasar = "Igualar a " + mesa.getApuesta();
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

    // PRE: mesa es una instancia válida de la clase Mesa.
    // POST: Comprueba si hay un ganador en la mesa.
    public static Jugador comprobar(Mesa mesa) {
        HashMap<Jugador, Integer> jugadoresOrden = mesa.getJugadoresOrden();
        List<Jugador> jugadores = mesa.getJugadores();
        int gana = 0;
        Jugador ganador = null;
        int i = 0;
        while (i < jugadores.size() && gana < 2) {
            if (jugadoresOrden.get(jugadores.get(i)) <= 10) {
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

    // PRE: clientes contiene información válida de los jugadores conectados. mesa
    // es una instancia válida de la clase Mesa. ganador es una instancia válida de
    // la clase Jugador o null.
    // POST: Finaliza la ronda, distribuyendo fichas y actualizando la información
    // de los jugadores.
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
        List<Jugador> jug = mesa.getJugadores();
        for (Jugador j : jug) {
            if (j.getFichas() == 0) {
                mesa.removeJugador(j);
                try {
                    ObjectOutputStream out = clientes.get(j.getNombre()).getOutputStream();
                    out.writeBoolean(true);
                    out.flush();
                    out.writeBytes("Has finalizado en posición " + clientes.values().size() + "/"
                            + NUMERO_JUGADORES_ESPERADOS + "\n");
                    out.writeBytes(".\n");
                    out.flush();
                    clientes.get(j.getNombre()).getInputStream().close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientes.remove(j.getNombre());
            }
        }
    }

    // PRE: in y out son instancias válidas de ObjectInputStream y
    // ObjectOutputStream respectivamente. mesa es una instancia válida de la clase
    // Mesa. ganadores es una lista válida de instancias de la clase Jugador.
    // POST: Envía información sobre los jugadores a través de out.
    public static void enviarInformacionJugadores(ObjectInputStream in, ObjectOutputStream out, Mesa mesa,
            List<Jugador> ganadores) {
        try {
            if (ganadores.size() == 1) {
                out.writeBytes("---------------------GANADOR ");
            } else {
                out.writeBytes("---------------------GANADORES ");
            }
            for (Jugador j : ganadores) {
                out.writeBytes(j.getNombre() + (ganadores.size() > 1 ? " " : "----------------------------\n"));
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

    // PRE: in y out son instancias válidas de ObjectInputStream y
    // ObjectOutputStream respectivamente. mesa es una instancia válida de la clase
    // Mesa.
    // POST: Envía información sobre la mesa a través de out.
    public static void enviarInformacionMesa(ObjectInputStream in, ObjectOutputStream out, Mesa mesa) {
        try {
            out.writeBytes(mesa.informacionMesa());
            out.writeBytes(".\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // PRE: j es una instancia válida de la clase Jugador.
    // mesa es una instancia válida de la clase Mesa.
    // cant es un entero que representa la cantidad a subir (en el caso de subir).
    // POST: Realizan acciones específicas del juego durante el turno de un jugador.

    public static void noIr(Jugador j, Mesa mesa) {
        mesa.aumentarOrdenJugador(j, 10);
    }

    public static void igualarPasar(Jugador j, Mesa mesa, String igualarPasar) {
        if (igualarPasar.split(" ")[0].equals("Igualar")) {
            int fichas = j.getFichas();
            if (mesa.getApuesta() > fichas) {
                List<Jugador> jugs = mesa.getJugadoresVivos();
                int num = 0;
                for (Jugador j1 : jugs) {
                    if (!j1.getNombre().equals(j.getNombre()) && j1.getApuesta() == mesa.getApuesta()) {
                        j1.setFichas(j1.getFichas() + mesa.getApuesta() - fichas);
                        j1.setApuesta(fichas);
                        num++;
                    }
                }
                mesa.quitarFichas((mesa.getApuesta() - fichas) * num);
                mesa.apostar(fichas, j);
                mesa.setApuesta(fichas);
                System.out.println("Ha apostado " + fichas);
            } else {
                System.out.println("Ha apostado " + (mesa.getApuesta() - j.getApuesta()));
                mesa.apostar(mesa.getApuesta() - j.getApuesta(), j);
            }
        }
    }

    public static void subir(Jugador j, Mesa mesa, int cant) {
        mesa.setApuesta(cant + j.getApuesta());
        mesa.apostar(cant, j);
    }
}