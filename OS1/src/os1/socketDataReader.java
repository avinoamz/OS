/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.ObjectInputStream;

/**
 * Thread that reads data from Sockets. Listen to a Socket, and starts an
 * S_Thread based on queries.
 */
public class socketDataReader implements Runnable {

    private ObjectInputStream in;
    private final Streams stream;
    private int msg;

    public socketDataReader(Streams stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("SocketDataReader");
        try {
            in = stream.getIn();
            msg = (int) in.readObject();
            Thread query = new Thread(new S_Thread(stream, msg));
            Server.getPool(Server.Type_S_Pool).execute(query);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading from socket");
        }
    }
}
