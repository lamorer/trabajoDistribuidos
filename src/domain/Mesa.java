package domain;

import java.util.Map.Entry;
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

    public Mesa() {
        jugadores = new HashMap<>();
        cartasEnMesa = new ArrayList<>();
        fichasMesa = 0;
        apuesta = ciegaGrande;
        ronda = 1;
        ganador = null;
    }

    public int getFichas() {
        return fichasMesa;
    }

    public int getRonda() {
        return ronda;
    }

    public void setRonda(int r) {
        this.ronda = r;
    }

    public void setFichas(int fichas) {
        this.fichasMesa = fichas;
    }

    public List<Carta> getCartasEnMesa() {
        return cartasEnMesa;
    }

    public List<Jugador> getJugadores() {
        List<Jugador> jugadores = new ArrayList<>(this.jugadores.keySet());
        jugadores.sort(Comparator.comparingInt(j -> this.jugadores.get(j)));
        return jugadores;
    }

    public int getApuesta() {
        return apuesta;
    }

    public void siguienteRonda() {
        ronda++;
    }

    public int getCiegaGrande() {
        return ciegaGrande;
    }

    public int getCiegaPequena() {
        return ciegaPequena;
    }

    public void agregarJugador(Jugador jugador) {
        jugadores.put(jugador, jugadores.size() + 1);
    }

    public void setApuesta(int apuesta) {
        this.apuesta = apuesta;
    }

    // Baraja debe estar barajada.
    public void repartirCartasIniciales(Baraja baraja) {
        for (Jugador jugador : jugadores.keySet()) {
            Carta carta1 = baraja.repartirCarta();
            Carta carta2 = baraja.repartirCarta();
            jugador.recibirCartas(carta1, carta2);
        }
    }

    public void agregarCartaAMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

    public void agregarFichas(int fichas) {
        fichasMesa += fichas;
    }

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

    public void establecerOrdenInicial() {
        List<Jugador> j = new ArrayList<>(jugadores.keySet());
        Collections.shuffle(j);
        jugadores.clear();
        for (int i = 0; i < j.size(); i++) {
            jugadores.put(j.get(i), i + 1);
        }
    }

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

    public void setOrdenJugador(Jugador j, int i) {
        jugadores.put(j, i);
    }

    public HashMap<Jugador, Integer> getJugadoresOrden() {
        return jugadores;
    }

    public void comprobarGanador() {
        List<Jugador> jugadores = getJugadores();
        List<Integer> jugadoresVivos = new ArrayList<>();
        int i = 0;
        while (i < jugadores.size()) {
            if (jugadores.get(i).getFichas() != 0) {
                jugadoresVivos.add(i);
            }
        }
        if (jugadoresVivos.size() == 1) {
            ganador = jugadores.get(jugadoresVivos.get(1));
        }
    }

    public Jugador getGanador(){
        return ganador;
    }
}