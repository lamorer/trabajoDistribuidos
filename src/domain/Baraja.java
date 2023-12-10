package domain;

import java.util.Random;

public class Baraja {
    private static final int NUM_CARTAS = 52;
    private Carta[] baraja = new Carta[NUM_CARTAS];
    private int size;

    // PRE: --
    // POST: Crea una instancia de la clase Baraja con todas las cartas
    // inicializadas.
    public Baraja() {
        this.size = NUM_CARTAS;
        Palo[] palos = Palo.values();
        int index = 0;

        for (Palo palo : palos) {
            for (int numero = 2; numero <= 14; numero++) {
                baraja[index] = new Carta(numero, palo);
                index++;
            }
        }
    }

    // PRE: --
    // POST: Baraja las cartas en la baraja de forma aleatoria.
    public void barajar() {
        Random random = new Random();
        for (int i = NUM_CARTAS - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Carta temp = baraja[i];
            baraja[i] = baraja[j];
            baraja[j] = temp;
        }
    }

    // PRE: Existen cartas en la baraja
    // POST: devuelve la última carta de la baraja y disminuye el tamaño de la baraja
    // en 1.
    // Si no hay cartas disponibles, imprime un mensaje y devuelve null.
    public Carta repartirCarta() {
        if (size > 0) {
            size--;
            return baraja[size];
        } else {
            System.out.println("No hay cartas para repartir.");
            return null;
        }
    }

    // PRE: --
    // POST: devuelve el tamaño actual de la baraja.
    public int getSize() {
        return size;
    }

    // PRE: --
    // POST: Muestra las cartas de la baraja en la consola.
    public void showBaraja() {
        for (int i = 0; i < baraja.length; i++) {
            System.out.println(baraja[i].toString());
        }
    }
}