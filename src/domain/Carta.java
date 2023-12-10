package domain;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 111L;
    private int numero;
    private Palo palo;

    // PRE: 1<=n<=10
    // POST: Crea una instancia de tipo carta con los valores indicados, si el
    // número no es correcto se crea el AS. Inicialmente, establecemos el valor de
    // AS como 14, pero también puede tener valor 1.
    public Carta(int n, Palo s) {
        if (n <= 14 && n >= 1) {
            this.numero = n;
        } else {
            System.out.println("Número incorrecto. Creamos el AS");
            this.numero = 14;
        }
        this.palo = s;
    }

    // PRE: --
    // POST: Devuelve un entero que es el valor numérico de la Carta.
    public int getNumero() {
        return this.numero;
    }

    // PRE: --
    // POST: Devuelve el palo de la Carta.
    public Palo getPalo() {
        return this.palo;
    }

    // PRE: 1<=n<=12
    // POST: Establece n como el número de la Carta.
    public void setNumero(int n) {
        this.numero = n;
    }

    // PRE: --
    // POST: Establece s como el palo de la Carta.
    public void setPalo(Palo s) {
        this.palo = s;
    }

    // PRE: Tenemos una Carta inicializada.
    // POST: Muestra la información de la Carta.
    public void show() {
        System.out.print(this.toString());
    }

    // PRE: --
    // POST: Devuelve una cadena con la información de la Carta. 11 como J, 12 como
    // Q, 13 como
    // K y 14 como AS.
    public String toString() {
        String s = "";
        if (this.numero <= 10 && this.numero != 1) {
            s = s + this.numero;
        } else if (this.numero == 14) {
            s = "A";
        } else if (this.numero == 11) {
            s = "J";
        } else if (this.numero == 12) {
            s = "Q";
        } else if (this.numero == 13) {
            s = "K";
        }
        switch (this.palo) {
            case Trebol:
                s = s + "T";
                break;
            case Diamante:
                s = s + "D";
                break;
            case Corazon:
                s = s + "C";
                break;
            case Pica:
                s = s + "P";
                break;
        }
        return s;
    }

    // PRE: --
    // POST: Devuelve TRUE si el objeto dado es una Carta y el número y el palo
    // son los mismos que los de la Carta inicial.
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Carta) {
            Carta c = (Carta) anObject;
            boolean aux = false;
            if (this.numero == c.numero && this.palo == c.palo) {
                aux = true;
            }
            return aux;
        } else {
            return false;
        }
    }
}
