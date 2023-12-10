package server;

import java.io.*;

//Clase auxiliar que almacena la información de cada Socket.
class JugadorInfo {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    // PRE: inputStream y outputStream no son nulos
    // POST: Crea una instancia de JugadorInfo con los flujos de entrada y salida
    // especificados.
    public JugadorInfo(ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    // PRE: --
    // POST: Devuelve el flujo de entrada asociado a la instancia.
    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    // PRE: --
    // POST: Devuelve el flujo de salida asociado a la instancia.
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}
