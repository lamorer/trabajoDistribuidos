package server;

import java.io.*;

class JugadorInfo {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public JugadorInfo(ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}