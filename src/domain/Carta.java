package domain;

public class Carta {
    private int numero;
    private Palo palo;

    public Carta(int n, Palo s) {
        //PRE: 1<=n<=10
        //POST: Crea una instancia de tipo carta con los valores indicados, si el número no es correcto se crea el AS.
        if (n <= 10 && n >= 1) {
            this.numero = n;
        } else {
            System.out.println("Número incorrecto. Creamos el AS");
            this.numero = 1;
        }
        this.palo = s;

    }

    public int getnumero() {
        //PRE: --
        //POST: It returns an integer that is the numeric value of the Carta.
        return this.numero;
    }

    public Palo getPalo() {
        //PRE: --
        //POST: It returns the palo of the Carta.
        return this.palo;
    }

    public void setnumero(int n) {
        //PRE: 1<=n<=12
        //POST: It sets n as the Carta numero.
        this.numero = n;
    }

    public void setpalo(Palo s) {
        //PRE: --
        //POST: It sets s as the palo of the Carta.
        this.palo = s;
    }

    public void show() {
        //PRE: We have an initialized Carta.
        //POST: It shows the Carta with numero 8 as Jack (10), 9 as Knight (11) and 10 as King (12) and palo.
        System.out.print(this.toString());
    }

    public String toString() {
        //PRE: --
        //POST: It returns a string with the Carta information. numero 8 as Jack, 9 as Knight and 10 as King.
        String s = "";
        if (this.numero <= 7 && this.numero != 1) {
            s = s + this.numero;
        }
        switch (this.numero) {
            case 1:
                s = "A";
                break;
            case 8:
                s = s + "10";
                break;
            case 9:
                s = s + "11";
                break;
            case 10:
                s = s + "12";
                break;
        }
        switch (this.palo) {
            case Trebol:
                s = s + "♣";
                break;
            case Diamante:
                s = s + "♦";
                break;
            case Corazon:
                s = s + "♥";
                break;
            case Pica:
                s = s + "♠";
                break;

        }
        return s;

    }

    public boolean equals(Object anObject) {
        //PRE: --
        //POST: It returns TRUE if the given object is a Carta and the numero and palo are the same as the initial Carta.
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
