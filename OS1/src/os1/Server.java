/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Avinoam
 */
public class Server implements Runnable {

    private ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(45000);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // need to implement a ThreadPool instead of opening a thread for each client.
                Thread listener = new Thread(new ClientListener(clientSocket));
                listener.start();
            }
        } catch (Exception e) {
            System.out.println("ServerSocker Error.");
        }
    }

}
