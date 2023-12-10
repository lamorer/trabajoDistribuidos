package domain;

public class Jugador {
    private String nombre;
    private Carta carta1;
    private Carta carta2;
    private int fichas;
    private int apuesta;

    // PRE: El nombre no es nulo
    // POST: Crea una instancia de Jugador con el nombre proporcionado, sin cartas,
    // 1500 fichas y sin apuesta.
    public Jugador(String nombre) {
        this.nombre = nombre;
        this.carta1 = null;
        this.carta2 = null;
        this.fichas = 1500;
        this.apuesta = 0;
    }

    // PRE: --
    // POST: devuelve el nombre del jugador.
    public String getNombre() {
        return nombre;
    }

    // PRE: --
    // POST: devuelve un vector con las dos cartas del jugador.
    public Carta[] getCartas() {
        Carta[] mano = new Carta[] { carta1, carta2 };
        return mano;
    }

    // PRE: --
    // POST: devuelve la cantidad de fichas del jugador.
    public int getFichas() {
        return fichas;
    }

    // PRE: fichas >= 0
    // POST: Establece la cantidad de fichas del jugador.
    public void setFichas(int fichas) {
        this.fichas = fichas;
    }

    // PRE: apuesta >= 0
    // POST: Establece la apuesta actual del jugador.
    public void setApuesta(int apuesta) {
        this.apuesta = apuesta;
    }

    // PRE: --
    // POST: devuelve la apuesta actual del jugador.
    public int getApuesta() {
        return this.apuesta;
    }

    // PRE: carta1 y carta2 no son nulas
    // POST: Asigna las dos cartas iniciales al jugador.
    public void recibirCartas(Carta carta1, Carta carta2) {
        this.carta1 = carta1;
        this.carta2 = carta2;
    }

    // PRE: --
    // POST: Muestra las cartas del jugador en la consola.
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
