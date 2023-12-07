package services;

import java.util.*;
import java.util.concurrent.*;

import domain.*;

public class ReglasPoker {
    public static List<Jugador> determinarGanador(Mesa mesa) {
        // Evaluar las manos de los jugadores y determinar al ganador
        try {
            HashMap<Jugador,ManoPoker> manos = new HashMap<>();
            List<Jugador> jugadores = mesa.getJugadoresVivos();
            ExecutorService pool = Executors.newFixedThreadPool(jugadores.size());
            final CyclicBarrier count = new CyclicBarrier(jugadores.size()+1);
            for (Jugador j : jugadores) {
                pool.execute(new Runnable() {
                    public void run() {
                        try{
                        manos.put(j,evaluarMano(j, mesa.getCartasEnMesa()));
                        count.await();
                        } catch (InterruptedException | BrokenBarrierException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            count.await();
            List<Jugador> jugadoresGanadores = new ArrayList<>();
            jugadoresGanadores.add(jugadores.get(0));
            ManoPoker manoGanadora = manos.get(jugadores.get(0));
            for (int i = 1; i < manos.size(); i++) {
                Jugador j = jugadores.get(i);
                ManoPoker m = manos.get(j);
                if (manoGanadora.getValor() < m.getValor()) {
                    manoGanadora = m;
                    jugadoresGanadores.clear();
                    jugadoresGanadores.add(j);
                } else if (manoGanadora.getValor() == m.getValor()) {
                    List<Carta> cartasGanador = manoGanadora.getCartas();
                    List<Carta> cartas = m.getCartas();
                    boolean nuevoGanador = false;
                    boolean mismoGanador = false;
                    int l = 0;
                    while (l < cartas.size() && !nuevoGanador && !mismoGanador) {
                        if (cartas.get(l).getNumero() > cartasGanador.get(l).getNumero()) {
                            nuevoGanador = true;
                        } else if (cartas.get(l).getNumero() < cartasGanador.get(l).getNumero()) {
                            mismoGanador = true;
                        }
                        l++;
                    }
                    if (nuevoGanador) {
                        manoGanadora = m;
                        jugadoresGanadores.clear();
                        jugadoresGanadores.add(j);
                    } else if (!nuevoGanador && !mismoGanador) {
                        jugadoresGanadores.add(j);
                    }
                }
            }
            System.out.println(manoGanadora.getDescripcion()+": "+manoGanadora.getCartas());
            pool.shutdown();
            return jugadoresGanadores;
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ManoPoker evaluarMano(Jugador jugador, List<Carta> cartasEnMesa) {
        // la lista de cartas contiene la lista de 5 cartas de la mano ordenadas según
        // la jugada (las cartas que acompaña se ordenan por orden desciendente)
        Carta[] cartasJugador = jugador.getCartas();
        List<Carta> cartasTotales = new ArrayList<>();
        cartasTotales.addAll(Arrays.asList(cartasJugador));
        cartasTotales.addAll(cartasEnMesa);
        List<List<Carta>> combinaciones = generarCombinaciones(cartasTotales, 5);
        // Con esto tenemos todas las combinaciones de cartas de 5 en 5.

        ExecutorService pool = Executors.newFixedThreadPool(combinaciones.size());
        List<Future<ManoPoker>> futures = new ArrayList<>();

        for (List<Carta> combinacion : combinaciones) {
            futures.add(pool.submit(new Callable<ManoPoker>() {
                public ManoPoker call() {
                    return obtenerMano(combinacion);
                }
            }));
        }

        List<ManoPoker> manos = new ArrayList<>();
        try {
            for (Future<ManoPoker> future : futures) {
                ManoPoker mano = future.get();
                manos.add(mano);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        ManoPoker mejorMano = manos.get(0);
        for (ManoPoker m : manos) {
            if (m.getValor() > mejorMano.getValor()) {
                mejorMano = m;
            } else if (m.getValor() == mejorMano.getValor()) {
                boolean mismaMano = false;
                boolean nuevaMano = false;
                int i=0;
                int length = mejorMano.getCartas().size();
                while(i<length && !mismaMano && !nuevaMano){
                    if(mejorMano.getCartas().get(i).getNumero()<m.getCartas().get(i).getNumero()){
                        mejorMano = m;
                    }
                    i++;
                }
            }
        }

        pool.shutdown();
        return mejorMano;
    }

    // Combinación es una lista de 5 cartas.
    private static ManoPoker obtenerMano(List<Carta> combinacion) {
        combinacion = ordenarCartas(combinacion);

        ManoPoker mano = null;
        if (esEscaleraReal(combinacion)) {
            return new ManoPoker(Mano.escaleraReal, combinacion);
        } else {
            mano = esEscaleraColor(combinacion);
            if (mano != null) {
                return mano;
            } else {
                mano = esPoker(combinacion);
                if (mano != null) {
                    return mano;
                } else {
                    mano = esFull(combinacion);
                    if (mano != null) {
                        return mano;
                    } else if (esColor(combinacion)) {
                        return new ManoPoker(Mano.color, combinacion);
                    } else {
                        mano = esEscalera(combinacion);
                        if (mano != null) {
                            return new ManoPoker(Mano.escalera, combinacion);
                        } else {
                            mano = esTrio(combinacion);
                            if (mano != null) {
                                return mano;
                            } else {
                                mano = esDoblePareja(combinacion);
                                if (mano != null) {
                                    return mano;
                                } else {
                                    mano = esPareja(combinacion);
                                    if (mano != null) {
                                        return mano;
                                    } else {
                                        return new ManoPoker(Mano.cartaAlta, combinacion);
                                    }
                                }
                            }
                        }
                    }
                }
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

    private static ManoPoker esEscaleraColor(List<Carta> combinacion) {
        // Verificar si todas las cartas son del mismo palo
        Palo palo = combinacion.get(0).getPalo();
        boolean mismoPalo = true;

        for (Carta carta : combinacion) {
            if (carta.getPalo() != palo) {
                mismoPalo = false;
                break;
            }
        }
        // Si no son del mismo palo, no puede ser una escalera de color
        if (!mismoPalo) {
            return null;
        }

        // Si todas las cartas son del mismo palo, verifica si hay una escalera
        ManoPoker escalera = esEscalera(combinacion);

        if (escalera != null) {
            return new ManoPoker(Mano.escaleraColor, combinacion);
        }

        return null;
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
                    List<Carta> list = new ArrayList<>();
                    for (int j = 1; j <= 4; j++) {
                        list.add(combinacion.get(i));
                    }
                    if (!combinacion.get(0).equals(combinacion.get(i))) {
                        list.add(combinacion.get(0));
                    } else {
                        list.add(combinacion.get(4));
                    }
                    return new ManoPoker(Mano.poker, list); // Hay un poker
                }
            } else {
                contador = 1;
                valorAnterior = valorActual;
            }
        }

        return null;
    }

    private static ManoPoker esFull(List<Carta> combinacion) {
        // Verificar si es XXYYY o AAABB
        Carta primeraCarta = combinacion.get(0);
        Carta segundaCarta = combinacion.get(1);
        Carta terceraCarta = combinacion.get(2);
        Carta cuartoCarta = combinacion.get(3);
        Carta quintaCarta = combinacion.get(4);

        if (primeraCarta.getNumero() == segundaCarta.getNumero() && terceraCarta.getNumero() == cuartoCarta.getNumero()
                && cuartoCarta.getNumero() == quintaCarta.getNumero()) {
            List<Carta> cartas = new ArrayList<>();
            cartas.add(terceraCarta);
            cartas.add(cuartoCarta);
            cartas.add(quintaCarta);
            cartas.add(primeraCarta);
            cartas.add(segundaCarta);
            return new ManoPoker(Mano.full, cartas);
        } else if (primeraCarta.getNumero() == segundaCarta.getNumero()
                && segundaCarta.getNumero() == terceraCarta.getNumero()
                && cuartoCarta.getNumero() == quintaCarta.getNumero()) {
            return new ManoPoker(Mano.full, combinacion);
        } else {
            return null;
        }
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

    private static ManoPoker esEscalera(List<Carta> combinacion) {
        // Ordena las cartas en orden ascendente
        combinacion = ordenarCartas(combinacion);

        // Verificar si las cartas forman una escalera considerando el AS como 14
        int valorPrimeraCarta = combinacion.get(0).getNumero();
        int valorSegundaCarta = combinacion.get(1).getNumero();
        int valorUltimaCarta = combinacion.get(combinacion.size() - 1).getNumero();

        // Escalera con AS como 1
        if (!(valorSegundaCarta - valorPrimeraCarta == combinacion.size() - 1)) {
            if (valorUltimaCarta - valorPrimeraCarta == combinacion.size() - 1) {
                return new ManoPoker(Mano.escalera, combinacion);
            } else {
                return null;
            }

        } else {
            List<Carta> list = new ArrayList<>();
            list.add(combinacion.get(2));
            list.add(combinacion.get(3));
            list.add(combinacion.get(4));
            list.add(combinacion.get(5));
            list.add(combinacion.get(1));
            return new ManoPoker(Mano.escalera, list);
        }
    }

    private static ManoPoker esTrio(List<Carta> combinacion) {
        // Verificar si hay tres cartas con el mismo valor
        Carta primeraCarta = combinacion.get(0);
        Carta segundaCarta = combinacion.get(1);
        Carta terceraCarta = combinacion.get(2);
        Carta cuartoCarta = combinacion.get(3);
        Carta quintaCarta = combinacion.get(4);
        List<Carta> cartas = new ArrayList<>();
        if (primeraCarta.getNumero() == segundaCarta.getNumero()
                && segundaCarta.getNumero() == terceraCarta.getNumero()) {
            return new ManoPoker(Mano.trio, combinacion);
        } else if (segundaCarta.getNumero() == terceraCarta.getNumero()
                && terceraCarta.getNumero() == cuartoCarta.getNumero()) {
            cartas.add(segundaCarta);
            cartas.add(terceraCarta);
            cartas.add(cuartoCarta);
            cartas.add(primeraCarta);
            cartas.add(quintaCarta);
            return new ManoPoker(Mano.trio, cartas);
        } else if (terceraCarta.getNumero() == cuartoCarta.getNumero()
                && cuartoCarta.getNumero() == quintaCarta.getNumero()) {
            cartas.add(terceraCarta);
            cartas.add(cuartoCarta);
            cartas.add(quintaCarta);
            cartas.add(primeraCarta);
            cartas.add(segundaCarta);
            return new ManoPoker(Mano.trio, cartas);
        }
        return null;
    }

    private static ManoPoker esDoblePareja(List<Carta> combinacion) {
        // Verificar si hay dos pares de cartas con el mismo valor
        Carta primeraCarta = combinacion.get(0);
        Carta segundaCarta = combinacion.get(1);
        Carta terceraCarta = combinacion.get(2);
        Carta cuartoCarta = combinacion.get(3);
        Carta quintaCarta = combinacion.get(4);
        List<Carta> list = new ArrayList<>();
        if (primeraCarta.getNumero() == segundaCarta.getNumero()
                && terceraCarta.getNumero() == cuartoCarta.getNumero()) {
            return new ManoPoker(Mano.doblePareja, combinacion);
        } else if (primeraCarta.getNumero() == segundaCarta.getNumero()
                && cuartoCarta.getNumero() == quintaCarta.getNumero()) {
            list.add(primeraCarta);
            list.add(segundaCarta);
            list.add(cuartoCarta);
            list.add(quintaCarta);
            list.add(terceraCarta);
            return new ManoPoker(Mano.doblePareja, list);
        } else if (segundaCarta.getNumero() == terceraCarta.getNumero()
                && cuartoCarta.getNumero() == quintaCarta.getNumero()) {
            list.add(segundaCarta);
            list.add(terceraCarta);
            list.add(cuartoCarta);
            list.add(quintaCarta);
            list.add(primeraCarta);
            return new ManoPoker(Mano.doblePareja, list);
        }
        return null;
    }

    private static ManoPoker esPareja(List<Carta> combinacion) {
        // Verificar si hay dos cartas con el mismo valor
        Carta primeraCarta = combinacion.get(0);
        Carta segundaCarta = combinacion.get(1);
        Carta terceraCarta = combinacion.get(2);
        Carta cuartoCarta = combinacion.get(3);
        Carta quintaCarta = combinacion.get(4);
        List<Carta> list = new ArrayList<>();
        if (primeraCarta.getNumero() == segundaCarta.getNumero()) {
            return new ManoPoker(Mano.pareja, combinacion);
        } else if (segundaCarta.getNumero() == terceraCarta.getNumero()) {
            list.add(segundaCarta);
            list.add(terceraCarta);
            list.add(primeraCarta);
            list.add(cuartoCarta);
            list.add(quintaCarta);
            return new ManoPoker(Mano.pareja, list);
        } else if (terceraCarta.getNumero() == cuartoCarta.getNumero()) {
            list.add(terceraCarta);
            list.add(cuartoCarta);
            list.add(primeraCarta);
            list.add(segundaCarta);
            list.add(quintaCarta);
            return new ManoPoker(Mano.pareja, list);
        } else if (cuartoCarta.getNumero() == quintaCarta.getNumero()) {
            list.add(cuartoCarta);
            list.add(quintaCarta);
            list.add(primeraCarta);
            list.add(segundaCarta);
            list.add(terceraCarta);
            return new ManoPoker(Mano.pareja, list);
        }

        return null;
    }

    private static List<Carta> ordenarCartas(List<Carta> combinacion) {
        Collections.sort(combinacion, new Comparator<Carta>() {
            @Override
            public int compare(Carta carta1, Carta carta2) {
                // Compara las cartas por su valor en orden descendente
                return carta2.getNumero() - carta1.getNumero();
            }
        });
        return combinacion;
    }

    private static List<List<Carta>> generarCombinaciones(List<Carta> cartas, int tam) {
        List<List<Carta>> combinaciones = new ArrayList<>();
        generarCombinacionesAux(cartas, tam, 0, new ArrayList<>(), combinaciones);
        return combinaciones;
    }

    private static void generarCombinacionesAux(List<Carta> cartas, int tam, int inicio, List<Carta> combinacionActual,
            List<List<Carta>> combinaciones) {
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