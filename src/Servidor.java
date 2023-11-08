import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Servidor {
    private static final int PORT = 6000;
    public static List<Socket> clients = new ArrayList<>();

    public static void main(String[] args) {
        ExecutorService pool=Executors.newCachedThreadPool();
        try (ServerSocket servidor = new ServerSocket(6000)) {
            System.out.println("Servidor de poker en funcionamiento en el puerto " + PORT);
            while(true) {
                try {
                    Socket cliente=servidor.accept();
                    AtenderPeticion p=new AtenderPeticion(cliente);
                    pool.execute(p);
                } catch(IOException e) {
                    e.printStackTrace();
                } 
            }
        } catch(IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }
}