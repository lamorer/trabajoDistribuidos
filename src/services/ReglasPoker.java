package services;

import java.util.*;
import java.util.concurrent.*;

import domain.Carta;
import domain.Jugador;
import domain.Mano;
import domain.ManoPoker;
import domain.Mesa;
import domain.Palo;

public class ReglasPoker {
    public static List<Jugador> determinarGanador(Mesa mesa) {
        // Evaluar las manos de los jugadores y determinar al ganador
        List<ManoPoker> manos = new ArrayList<>();
        List<Jugador> jugadores=mesa.getJugadores();
        ExecutorService pool = Executors.newFixedThreadPool(mesa.getJugadores().size());

        //CONTINUAR: Hacer que cada hilo evalúe todas las posibles manos del jugador y determine cual es la mejor
        for(Jugador j: jugadores){
            pool.execute(new Runnable(){
                public void run(){
                    manos.add(evaluarMano(j, mesa.getCartasEnMesa()));
                }
            });           
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
        //la lista de cartas contiene la lista de 5 cartas de la mano ordenadas según la jugada (las cartas que acompaña se ordenan por orden desciendente)
        Carta[] cartasJugador=jugador.getCartas();
        List<Carta> cartasTotales = new ArrayList<>();
        cartasTotales.addAll(Arrays.asList(cartasJugador));
        cartasTotales.addAll(cartasEnMesa);
        List<List<Carta>> combinaciones = generarCombinaciones(cartasTotales,5);    //Con esto tenemos todas las combinaciones de cartas de 5 en 5.
        
        ExecutorService pool = Executors.newFixedThreadPool(combinaciones.size());
        List<Future<ManoPoker>> futures = new ArrayList<>();

        for (List<Carta> combinacion : combinaciones) {
            futures.add(pool.submit(new Callable<ManoPoker>() {
                public ManoPoker call(){
                    return obtenerMano(combinacion);
                }
            }));
        }

        List<ManoPoker> manos=new ArrayList<>();
        try {
            for (Future<ManoPoker> future : futures) {
                ManoPoker mano = future.get();
                manos.add(mano);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        ManoPoker mejorMano=manos.get(0);
        for(ManoPoker m : manos){
            if(m.getValor()>mejorMano.getValor()){
                mejorMano=m;
            } else if(m.getValor()==mejorMano.getValor()){

            }
        }

        pool.shutdown();
        return mejorMano;
    }

    private static ManoPoker obtenerMano(List<Carta> combinacion){
        combinacion=ordenarCartas(combinacion);
        if(esEscaleraReal(combinacion)){
            return new ManoPoker(Mano.escaleraReal, combinacion);
        } else if(esEscaleraColor(combinacion)){
            return new ManoPoker(Mano.escaleraColor, combinacion);
        } else { 
            ManoPoker mano=esPoker(combinacion);
            if(mano!=null){
                return mano;
            } else if(esFull(combinacion)){
                return new ManoPoker(Mano.full, combinacion);
            } else if(esColor(combinacion)){
                return new ManoPoker(Mano.color, combinacion);
            } else if(esEscalera(combinacion)){
                return new ManoPoker(Mano.escalera, combinacion);
            } else if(esTrio(combinacion)){
                return new ManoPoker(Mano.trio, combinacion);
            } else if(esDoblePareja(combinacion)){
                return new ManoPoker(Mano.doblePareja, combinacion);
            } else if(esPareja(combinacion)){
                return new ManoPoker(Mano.pareja, combinacion);
            } else{
                return new ManoPoker(Mano.cartaAlta,combinacion);
            }
        }
    }

    private static boolean esEscaleraReal(List<Carta> combinacion) {    
        // Verificar si las cartas son consecutivas del 10 al A
        int valorEsperado = 10;
        for (Carta carta : combinacion) {
            if (carta.getNumero() != valorEsperado) {
                return false;
            }
            valorEsperado++;
        }
    
        // Verificar si todas las cartas son del mismo palo
        Palo palo = combinacion.get(0).getPalo();
        for (Carta carta : combinacion) {
            if (!carta.getPalo().equals(palo)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean esEscaleraColor(List<Carta> combinacion) {    
        // Verificar si las cartas son consecutivas
        int valorEsperado = combinacion.get(0).getNumero() + 1;
        for (int i = 1; i < combinacion.size(); i++) {
            if (combinacion.get(i).getNumero() != valorEsperado) {
                return false;
            }
            valorEsperado++;
        }
    
        // Verificar si todas las cartas son del mismo palo
        Palo palo = combinacion.get(0).getPalo();
        for (Carta carta : combinacion) {
            if (!carta.getPalo().equals(palo)) {
                return false;
            }
        }
    
        return true;
    }
    
    private static ManoPoker esPoker(List<Carta> combinacion) {
        // Contar las cartas con el mismo valor
        int contador = 1;
        int valorAnterior = combinacion.get(0).getNumero();
    
        for (int i = 1; i < combinacion.size(); i++) {
            int valorActual = combinacion.get(i).getNumero();
            if (valorActual == valorAnterior) {
                contador++;
                if (contador == 4) {
                    List<Carta> list=new ArrayList<>();
                    for(int j=1;j<=4;j++){
                        list.add(combinacion.get(i));
                    }
                    if(!combinacion.get(0).equals(combinacion.get(i))){
                        list.add(combinacion.get(0));
                    } else {
                        list.add(combinacion.get(4));
                    }
                    return new ManoPoker(Mano.poker,list); // Hay un poker
                }
            } else {
                contador = 1;
                valorAnterior = valorActual;
            }
        }
    
        return null;
    }

    private static boolean esFull(List<Carta> combinacion) {
        // Verificar si es XXYYY o AAABB
        int valorPrimeraCarta = combinacion.get(0).getNumero();
        int valorSegundaCarta = combinacion.get(1).getNumero();
        int valorTerceraCarta = combinacion.get(2).getNumero();
        int valorCuartoCarta = combinacion.get(3).getNumero();
        int valorQuintaCarta = combinacion.get(4).getNumero();
    
        if ((valorPrimeraCarta == valorSegundaCarta && valorTerceraCarta == valorCuartoCarta && valorCuartoCarta == valorQuintaCarta)
                || (valorPrimeraCarta == valorSegundaCarta && valorSegundaCarta == valorTerceraCarta && valorCuartoCarta == valorQuintaCarta)) {
            return true;
        }
    
        return false;
    }
    
    private static boolean esColor(List<Carta> combinacion) {
        // Verificar si todas las cartas son del mismo palo
        Palo palo = combinacion.get(0).getPalo();
        for (Carta carta : combinacion) {
            if (!carta.getPalo().equals(palo)) {
                return false;
            }
        }
    
        return true;
    }
    
    private static boolean esEscalera(List<Carta> combinacion) {
        // Verificar si las cartas son consecutivas
        int valorPrimeraCarta = combinacion.get(0).getNumero();
        int valorSegundaCarta = combinacion.get(1).getNumero();
        int valorTerceraCarta = combinacion.get(2).getNumero();
        int valorCuartoCarta = combinacion.get(3).getNumero();
        int valorQuintaCarta = combinacion.get(4).getNumero();
    
        if (valorQuintaCarta - valorPrimeraCarta == 4
                && (valorPrimeraCarta == valorSegundaCarta - 1 && valorSegundaCarta == valorTerceraCarta - 1 && valorTerceraCarta == valorCuartoCarta - 1 && valorCuartoCarta == valorQuintaCarta - 1)) {
            return true;
        }
    
        return false;
    }
    
    private static boolean esTrio(List<Carta> combinacion) {
        // Verificar si hay tres cartas con el mismo valor
        int valorPrimeraCarta = combinacion.get(0).getNumero();
        int valorSegundaCarta = combinacion.get(1).getNumero();
        int valorTerceraCarta = combinacion.get(2).getNumero();
        int valorCuartoCarta = combinacion.get(3).getNumero();
        int valorQuintaCarta = combinacion.get(4).getNumero();
    
        if ((valorPrimeraCarta == valorSegundaCarta && valorSegundaCarta == valorTerceraCarta)
                || (valorSegundaCarta == valorTerceraCarta && valorTerceraCarta == valorCuartoCarta)
                || (valorTerceraCarta == valorCuartoCarta && valorCuartoCarta == valorQuintaCarta)) {
            return true;
        }
    
        return false;
    }
    
    private static boolean esDoblePareja(List<Carta> combinacion) {
        // Verificar si hay dos pares de cartas con el mismo valor
        int valorPrimeraCarta = combinacion.get(0).getNumero();
        int valorSegundaCarta = combinacion.get(1).getNumero();
        int valorTerceraCarta = combinacion.get(2).getNumero();
        int valorCuartoCarta = combinacion.get(3).getNumero();
        int valorQuintaCarta = combinacion.get(4).getNumero();
    
        if ((valorPrimeraCarta == valorSegundaCarta && valorTerceraCarta == valorCuartoCarta)
                || (valorPrimeraCarta == valorSegundaCarta && valorCuartoCarta == valorQuintaCarta)
                || (valorSegundaCarta == valorTerceraCarta && valorCuartoCarta == valorQuintaCarta)) {
            return true;
        }
    
        return false;
    }
    
    private static boolean esPareja(List<Carta> combinacion) {
        // Verificar si hay dos cartas con el mismo valor
        int valorPrimeraCarta = combinacion.get(0).getNumero();
        int valorSegundaCarta = combinacion.get(1).getNumero();
        int valorTerceraCarta = combinacion.get(2).getNumero();
        int valorCuartoCarta = combinacion.get(3).getNumero();
        int valorQuintaCarta = combinacion.get(4).getNumero();
    
        if ((valorPrimeraCarta == valorSegundaCarta)
                || (valorSegundaCarta == valorTerceraCarta)
                || (valorTerceraCarta == valorCuartoCarta)
                || (valorCuartoCarta == valorQuintaCarta)) {
            return true;
        }
    
        return false;
    }


    private static List<Carta> ordenarCartas(List<Carta> combinacion){
        Collections.sort(combinacion, new Comparator<Carta>() {
            @Override
            public int compare(Carta carta1, Carta carta2) {
                // Compara las cartas por su valor en orden ascendente
                return carta1.getNumero() - carta2.getNumero();
            }
        });
        return combinacion;
    }

    private static List<List<Carta>> generarCombinaciones(List<Carta> cartas, int tam) {
        List<List<Carta>> combinaciones = new ArrayList<>();
        generarCombinacionesAux(cartas, tam, 0, new ArrayList<>(), combinaciones);
        return combinaciones;
    }
    
    private static void generarCombinacionesAux(List<Carta> cartas, int tam, int inicio, List<Carta> combinacionActual, List<List<Carta>> combinaciones) {
        if (tam == 0) {
            combinaciones.add(new ArrayList<>(combinacionActual));
            return;
        }
    
        for (int i = inicio; i < cartas.size(); i++) {
            combinacionActual.add(cartas.get(i));
            generarCombinacionesAux(cartas, tam - 1, i + 1, combinacionActual, combinaciones);
            combinacionActual.remove(combinacionActual.size() - 1);
        }
    }
    
}