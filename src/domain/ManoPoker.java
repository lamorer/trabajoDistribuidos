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

    public ManoPoker(Mano descripcion, List<Carta> cartas) {
        this.descripcion = descripcion;
        this.cartas = cartas;
        this.valor = valorManos.getOrDefault(descripcion, 1); // Valor por defecto: Carta Alta
    }

    public int getValor() {
        return valor;
    }

    public Mano getDescripcion() {
        return descripcion;
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    public String toString() {
        String s=this.descripcion.name();
        for(Carta c: this.cartas){
            s+=" "+c.toString();
        }
        return s;
    }
}
