package services;

import java.util.*;
import java.util.concurrent.*;

import domain.Carta;
import domain.Jugador;
import domain.ManoPoker;
import domain.Mesa;

public class ReglasPoker {
    public static List<Jugador> determinarGanador(Mesa mesa) {
        // Evaluar las manos de los jugadores y determinar al ganador
        List<ManoPoker> manos = new ArrayList<>();
        List<Jugador> jugadores=mesa.getJugadores();
        ExecutorService pool = Executors.newFixedThreadPool(mesa.getJugadores().size());

        //CONTINUAR: Hacer que cada hilo evalúe todas las posibles manos del jugador y determine cual es la mejor
        for(Jugador j: jugadores){
            manos.add(evaluarMano(j, mesa.getCartasEnMesa()));
        }

        List<Jugador> jugadoresGanadores = new ArrayList<>();
        jugadoresGanadores.add(jugadores.get(0));
        ManoPoker manoGanadora = manos.get(0);
        for(int i=1;i<manos.size();i++){
            Jugador j=jugadores.get(i);
            ManoPoker m=manos.get(i);
            if(manoGanadora.getValor()<m.getValor()){
                manoGanadora=m;
                jugadoresGanadores.clear();
                jugadoresGanadores.add(j);
            } else if(manoGanadora.getValor()==m.getValor()){
                jugadoresGanadores.add(j);
            }
        }
        return jugadoresGanadores;
    }

    private static ManoPoker evaluarMano(Jugador jugador, List<Carta> cartasEnMesa) {
        Carta[] cartasJugador=jugador.getCartas();

        //return new ManoPoker(valor, descripcion);
        return null;
    }
}