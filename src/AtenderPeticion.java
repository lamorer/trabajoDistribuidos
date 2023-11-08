import java.net.*;

public class AtenderPeticion implements Runnable{
    private Socket cliente;

    public AtenderPeticion(Socket cliente){
        this.cliente=cliente;
    }
    @Override
    public void run() {
        System.out.println("hola");
    }

}
