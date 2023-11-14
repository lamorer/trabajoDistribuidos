package domain;

import java.util.*;

public class Mesa {
    private HashMap<Integer,Jugador> jugadores;
    private List<Carta> cartasEnMesa;
    private int fichasMesa;

    public Mesa() {
        jugadores = new HashMap<>();
        cartasEnMesa = new ArrayList<>();
        fichasMesa = 0;
    }

    public int getFichas (){
        return fichasMesa;
    }

    public List<Carta> getCartasEnMesa() {
        return cartasEnMesa;
    }

    public List<Jugador> getJugadores() {
        return new ArrayList<>(jugadores.values());
    }

    public void agregarJugador(Jugador jugador) {
        jugadores.put(jugadores.size(),jugador);
    }

    //Baraja debe estar barajada.
    public void repartirCartasIniciales(Baraja baraja) {
        for (Jugador jugador : jugadores.values()) {
            Carta carta1 = baraja.repartirCarta();
            Carta carta2 = baraja.repartirCarta();
            jugador.recibirCartas(carta1, carta2);
        }
    }

    public void agregarCartaAMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

    public void agregarFichas(int fichas){
        fichasMesa+=fichas;
    }

    public boolean apostar(int cant, Jugador jugador){
        int fichasJugador = jugador.getFichas();
        if(fichasJugador>=cant){
            jugador.setFichas(fichasJugador);
            fichasMesa+=cant;
            return true;
        } else {
            return false;
        }
    }
}
