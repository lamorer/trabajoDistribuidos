package domain;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 111L;
    private int numero;
    private Palo palo;

    public Carta(int n, Palo s) {
        // PRE: 1<=n<=10
        // POST: Crea una instancia de tipo carta con los valores indicados, si el
        // número no es correcto se crea el AS. Inicialmente, establecemos el valor de AS como 14, pero también puede tener valor 1. 
        if (n <= 14 && n >= 1) {
            this.numero = n;
        } else {
            System.out.println("Número incorrecto. Creamos el AS");
            this.numero = 14;
        }
        this.palo = s;

    }

    public int getNumero() {
        // PRE: --
        // POST: It returns an integer that is the numeric value of the Carta.
        return this.numero;
    }

    public Palo getPalo() {
        // PRE: --
        // POST: It returns the palo of the Carta.
        return this.palo;
    }

    public void setNumero(int n) {
        // PRE: 1<=n<=12
        // POST: It sets n as the Carta numero.
        this.numero = n;
    }

    public void setPalo(Palo s) {
        // PRE: --
        // POST: It sets s as the palo of the Carta.
        this.palo = s;
    }

    public void show() {
        // PRE: We have an initialized Carta.
        // POST: 
        System.out.print(this.toString());
    }

    public String toString() {
        // PRE: --
        // POST: It returns a string with the Carta information. 11 as J, 12 as Q, 13 as K and 14 as AS.
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

    public boolean equals(Object anObject) {
        // PRE: --
        // POST: It returns TRUE if the given object is a Carta and the numero and palo
        // are the same as the initial Carta.
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
