package domain;

import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.*;

public class Mesa {
    private HashMap<Jugador, Integer> jugadores;
    private List<Carta> cartasEnMesa;
    private int fichasMesa;
    private int apuesta;
    private static int ciegaGrande = 30;
    private static int ciegaPequena = 15;
    private int ronda;
    private Jugador ganador;

    // PRE: --
    // POST: Crea una instancia de Mesa con valores iniciales.
    public Mesa() {
        jugadores = new HashMap<>();
        cartasEnMesa = new ArrayList<>();
        fichasMesa = 0;
        apuesta = ciegaGrande;
        ronda = 1;
        ganador = null;
    }

    // PRE: --
    // POST: devuelve la cantidad de fichas en la mesa.
    public int getFichas() {
        return fichasMesa;
    }

    // PRE: --
    // POST: devuelve la ronda actual.
    public int getRonda() {
        return ronda;
    }

    // PRE: r >= 1
    // POST: Establece la ronda actual.
    public void setRonda(int r) {
        this.ronda = r;
    }

    // PRE: fichas >= 0
    // POST: Establece la cantidad de fichas en la mesa.
    public void setFichas(int fichas) {
        this.fichasMesa = fichas;
    }

    // PRE: --
    // POST: devuelve la lista de cartas en la mesa.
    public List<Carta> getCartasEnMesa() {
        return cartasEnMesa;
    }

    // PRE: --
    // POST: devuelve una lista ordenada de jugadores por su posición en la mesa.
    public List<Jugador> getJugadores() {
        List<Jugador> jugadores = new ArrayList<>(this.jugadores.keySet());
        jugadores.sort(Comparator.comparingInt(j -> this.jugadores.get(j)));
        return jugadores;
    }

    // PRE: --
    // POST: devuelve una lista ordenada de jugadores vivos por su posición en la
    // mesa.
    public List<Jugador> getJugadoresVivos() {
        List<Jugador> jugs = new ArrayList<>(this.jugadores.keySet());
        jugs.sort(Comparator.comparingInt(j -> this.jugadores.get(j)));
        List<Jugador> jugadoresVivos = jugs.stream()
                .filter(j -> this.jugadores.get(j) <= 10)
                .collect(Collectors.toList());

        return jugadoresVivos;
    }

    // PRE: --
    // POST: devuelve la apuesta actual.
    public int getApuesta() {
        return apuesta;
    }

    // PRE: --
    // POST: Incrementa la ronda en uno.
    public void siguienteRonda() {
        ronda++;
    }

    // PRE: --
    // POST: devuelve la cantidad de la ciega grande.
    public int getCiegaGrande() {
        return ciegaGrande;
    }

    // PRE: --
    // POST: devuelve la cantidad de la ciega pequeña.
    public int getCiegaPequena() {
        return ciegaPequena;
    }

    // PRE: jugador no nulo
    // POST: Agrega un jugador a la mesa con su posición.
    public void agregarJugador(Jugador jugador) {
        jugadores.put(jugador, jugadores.size() + 1);
    }

    // PRE: apuesta >= 0
    // POST: Establece la apuesta actual.
    public void setApuesta(int apuesta) {
        this.apuesta = apuesta;
    }

    // PRE: baraja debe estar barajada
    // POST: Reparte cartas iniciales a los jugadores.
    public void repartirCartasIniciales(Baraja baraja) {
        for (Jugador jugador : jugadores.keySet()) {
            Carta carta1 = baraja.repartirCarta();
            Carta carta2 = baraja.repartirCarta();
            jugador.recibirCartas(carta1, carta2);
        }
    }

    // PRE: carta no nula
    // POST: Agrega una carta a la mesa.
    public void agregarCartaAMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

    // PRE: fichas >= 0
    // POST: Agrega fichas al bote de la mesa.
    public void agregarFichas(int fichas) {
        fichasMesa += fichas;
    }

    // PRE: cant >= 0, jugador no nulo
    // POST: Realiza una apuesta por parte del jugador si tiene suficientes fichas.
    // devuelve true si la apuesta fue exitosa, false si no tiene suficientes fichas.
    public boolean apostar(int cant, Jugador jugador) {
        int fichasJugador = jugador.getFichas();
        if (fichasJugador >= cant) {
            jugador.setFichas(fichasJugador - cant);
            fichasMesa += cant;
            jugador.setApuesta(jugador.getApuesta() + cant);
            return true;
        } else {
            return false;
        }
    }

    // PRE: cant >= 0
    // POST: Reduce la cantidad de fichas en el bote de la mesa.
    public void quitarFichas(int cant) {
        fichasMesa -= cant;
    }

    // PRE: --
    // POST: Limpia las cartas en la mesa.
    public void limpiarMesa() {
        cartasEnMesa.clear();
    }

    // PRE: --
    // POST: Establece un orden inicial aleatorio para los jugadores.
    public void establecerOrdenInicial() {
        List<Jugador> j = new ArrayList<>(jugadores.keySet());
        Collections.shuffle(j);
        jugadores.clear();
        for (int i = 0; i < j.size(); i++) {
            jugadores.put(j.get(i), i + 1);
        }
    }

    // PRE: i != 0
    // POST: Aumenta o disminuye la posición del jugador en el orden de la mesa.
    public void aumentarOrdenJugador(Jugador j, int i) {
        jugadores.put(j, jugadores.get(j) + i);
    }

    // PRE: --
    // POST: devuelve un mapa de jugadores ordenados por su posición en la mesa.
    public HashMap<Jugador, Integer> getJugadoresOrden() {
        return jugadores;
    }

    // PRE: --
    // POST: Comprueba si hay un único ganador entre los jugadores vivos.
    public void comprobarGanador() {
        List<Jugador> jugadores = getJugadores();
        List<Integer> jugadoresVivos = new ArrayList<>();
        int i = 0;
        while (i < jugadores.size()) {
            if (jugadores.get(i).getFichas() != 0) {
                jugadoresVivos.add(i);
            }
            i++;
        }
        if (jugadoresVivos.size() == 1) {
            ganador = jugadores.get(jugadoresVivos.get(0));
        }
    }

    // PRE: --
    // POST: devuelve el jugador ganador.
    public Jugador getGanador() {
        return ganador;
    }

    // PRE: --
    // POST: Establece un nuevo orden de jugadores para la siguiente ronda.
    public void establecerOrdenSiguienteRonda() {
        for (Entry<Jugador, Integer> j : jugadores.entrySet()) {
            if (j.getValue() > 10) {
                jugadores.put(j.getKey(), jugadores.get(j.getKey()) - 10);
            }
        }
        List<Jugador> j = getJugadores();
        jugadores.clear();
        for (int i = 0; i < j.size() - 1; i++) {
            jugadores.put(j.get(i), i + 2);
        }
        jugadores.put(j.get(j.size() - 1), 1);
    }

    // PRE: j no nulo
    // POST: Elimina a un jugador de la mesa.
    public void removeJugador(Jugador j) {
        jugadores.remove(j);
    }

    // PRE: --
    // POST: devuelve una cadena con información sobre el estado de la mesa.
    public String informacionMesa() {
        String s = "Bote: " + this.fichasMesa + "\n";
        for (Carta c : cartasEnMesa) {
            s += c.toString() + "\n";
        }
        for (Jugador j : jugadores.keySet()) {
            s += j.getNombre() + ": " + j.getFichas() + "\n";
        }
        return s;
    }
}
