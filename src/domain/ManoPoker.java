package domain;

import java.util.*;

public class ManoPoker {
    private int valor;
    private Mano descripcion;
    private List<Carta> cartas;

    private static final Map<Mano, Integer> valorManos;

    static {
        valorManos = new HashMap<>();
        valorManos.put(Mano.escaleraReal, 10);
        valorManos.put(Mano.escaleraColor, 9);
        valorManos.put(Mano.poker, 8);
        valorManos.put(Mano.full, 7);
        valorManos.put(Mano.color, 6);
        valorManos.put(Mano.escalera, 5);
        valorManos.put(Mano.trio, 4);
        valorManos.put(Mano.doblePareja, 3);
        valorManos.put(Mano.pareja, 2);
        valorManos.put(Mano.cartaAlta, 1);
    }

    // PRE: descripcion es una Mano válida (no nula) y cartas es una lista de 5
    // cartas no nula
    // POST: Crea una instancia de ManoPoker con la descripción y cartas
    // proporcionadas,
    // asigna un valor basado en la descripción según la tabla de valorManos.
    public ManoPoker(Mano descripcion, List<Carta> cartas) {
        this.descripcion = descripcion;
        this.cartas = cartas;
        this.valor = valorManos.getOrDefault(descripcion, 1); // Valor por defecto: Carta Alta
    }

    // PRE: --
    // POST: devuelve el valor de la mano.
    public int getValor() {
        return valor;
    }

    // PRE: --
    // POST: devuelve la descripción de la mano.
    public Mano getDescripcion() {
        return descripcion;
    }

    // PRE: --
    // POST: devuelve la lista de cartas de la mano.
    public List<Carta> getCartas() {
        return cartas;
    }

    // PRE: --
    // POST: devuelve una representación en cadena de la mano, incluyendo su
    // descripción y cartas.
    public String toString() {
        String s = this.descripcion.name();
        for (Carta c : this.cartas) {
            s += " " + c.toString();
        }
        return s;
    }
}