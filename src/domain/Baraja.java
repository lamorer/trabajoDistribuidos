package domain;

import java.util.Random;

public class Baraja {
     private static final int NUM_CARTAS = 52;
    private Carta[] baraja = new Carta[NUM_CARTAS];
    private int size;

    public Baraja() {
        this.size = NUM_CARTAS;
        Palo[] palos = Palo.values();
        int index = 0;

        for (Palo palo : palos) {
            for (int numero = 1; numero <= 13; numero++) {
                baraja[index] = new Carta(numero, palo);
                index++;
            }
        }
    }

    public void barajar() {
        Random random = new Random();
        for (int i = NUM_CARTAS - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Carta temp = baraja[i];
            baraja[i] = baraja[j];
            baraja[j] = temp;
        }
    }

    public Carta repartirCarta() {
        if (size > 0) {
            size--;
            return baraja[size];
        } else {
            System.out.println("No hay cartas para repartir.");
            return null;
        }
    }

    public int getSize() {
        return size;
    }

    public void showBaraja(){
        for(int i=0; i<baraja.length;i++){
            System.out.println(baraja[i].toString());
        }
    }
}
