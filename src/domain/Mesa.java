package domain;

import java.util.ArrayList;
import java.util.List;

public class Mesa {
    private List<Jugador> jugadores;
    private List<Carta> cartasEnMesa;

    public Mesa() {
        jugadores = new ArrayList<>();
        cartasEnMesa = new ArrayList<>();
    }

    public List<Carta> getCartasEnMesa() {
        return cartasEnMesa;
    }
    
    public List<Jugador> getJugadores(){
        return this.jugadores;
    }

    public void agregarJugador(Jugador jugador) {
        jugadores.add(jugador);
    }

    public void repartirCartasIniciales(Cartas cartas) {
        for (Jugador jugador : jugadores) {
            Carta carta1 = cartas.repartirCarta();
            Carta carta2 = cartas.repartirCarta();
            jugador.recibirCartas(carta1, carta2);
        }
    }

    public void agregarCartaAMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

}
