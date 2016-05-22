/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Avinoam
 */
public class socketDataReader implements Runnable {

    private ObjectInputStream in;
    private Streams stream;
    private int msg;

    public socketDataReader(Streams stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        try {
            in = stream.getIn();
            msg = (int) in.readObject();
            // 
            // S_Thread param?
            Thread query = new Thread(new S_Thread(stream, msg));
            Server.getPool(Server.Type_S_Pool).execute(query);

        } catch (Exception e) {
            System.out.println("Error reading from socket");
        }
    }
}
