/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Avinoam
 */
public class Server implements Runnable {

    private ServerSocket serverSocket;
    private int S, C, M, L, Y;
    private static ThreadPool S_Pool;
    private ArrayList<Socket> clients = new ArrayList();
    private final ReentrantLock lock = new ReentrantLock(true);

    public Server(int S, int C, int M, int L, int Y) {
        this.S = S;
        this.C = C;
        this.M = M;
        this.L = L;
        this.Y = Y;
        initServer();
    }

    private void initServer() {
        try {
            serverSocket = new ServerSocket(45000);

            new Thread(new socketsReader(clients)).start();

            S_Pool = new ThreadPool(S);

        } catch (Exception e) {
            System.out.println("Error initiating server");
        }
    }

    @Override
    public void run() {

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                //
                // need lock?
                clients.add(clientSocket);
            } catch (Exception e) {
                System.out.println("Error accepting socket");
            }
        }
    }

    public static ThreadPool getPool(int type) {
        switch (type) {
            case 1:
                return S_Pool;
            default:
                return null;
        }
    }
}

class socketsReader implements Runnable {

    private ArrayList<Socket> clients;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private int msg;

    public socketsReader(ArrayList clients) {
        this.clients = clients;
    }

    @Override
    public void run() {
        while (true) {
            for (int i = 0; i < clients.size(); i++) {
                socket = clients.get(i);
                // more params ?
                Thread readData = new Thread(new socketDataReader(socket));
                readData.start();
                try {
                    // set const waiting time ?
                    readData.join(100);
                    if (readData.isAlive()) {
                        readData.interrupt();
                    }
                } catch (Exception e) {
                    System.out.println("Join error");
                }
            }
        }
    }
}
