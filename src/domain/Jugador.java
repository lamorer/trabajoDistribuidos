package domain;

public class Jugador {
    private String nombre;
    private Carta carta1;
    private Carta carta2;
    private int fichas;
    private int apuesta;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.carta1 = null;
        this.carta2 = null;
        this.fichas = 1500;
        this.apuesta = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public Carta[] getCartas() {
        Carta[] mano = new Carta[] { carta1, carta2 };
        return mano;
    }

    public int getFichas() {
        return fichas;
    }

    public void setFichas(int fichas) {
        this.fichas = fichas;
    }

    public void setApuesta(int apuesta) {
        this.apuesta = apuesta;
    }

    public int getApuesta(){
        return this.apuesta;
    }

    public void recibirCartas(Carta carta1, Carta carta2) {
        this.carta1 = carta1;
        this.carta2 = carta2;
    }

    public void mostrarCartas() {
        System.out.println(nombre + " tiene las siguientes cartas:");
        if (carta1 != null) {
            System.out.println("Carta 1: " + carta1.toString());
        }
        if (carta2 != null) {
            System.out.println("Carta 2: " + carta2.toString());
        }
    }
}
