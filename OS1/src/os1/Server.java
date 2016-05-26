/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Main Server. Initialize the Thread Pools, and listen to new client
 * connections.
 *
 */
public class Server implements Runnable {

    private ServerSocket serverSocket;
    private final int S, C, M, L, Y;
    private static int randomRange;
    private static ThreadPool S_Pool, Cache_Pool, Readers_Pool, Writer_Pool;
    private static Cache cache;
    private static Database database;
    private static ReadWriteLock readWriteLock = new ReadWriteLock();
    private static final SyncedHashMap tempDataList = new SyncedHashMap();
    private static final ArrayList<Streams> clients = new ArrayList();
    public static final int Type_S_Pool = 1;
    public static final int Type_Cache_Pool = 2;
    public static final int Type_Readers_Pool = 3;
    public static final int Type_Writer_Pool = 4;

    public Server(int S, int C, int M, int L, int Y) {
        this.S = S;
        this.C = C;
        this.M = M;
        this.L = L;
        randomRange = L;
        this.Y = Y;
        initServer();
    }

    private void initServer() {
        try {
            serverSocket = new ServerSocket(45000);

            // A Thread that listens to client Sockets.
            new Thread(new socketsReader(clients)).start();
            new Thread(new socketsReader(clients)).start();
            
            S_Pool = new ThreadPool(S);
            Readers_Pool = new ThreadPool(Y);
            Cache_Pool = new ThreadPool(1);
            Writer_Pool = new ThreadPool(1);

            cache = new Cache(C, M);
            database = new Database(L, C);

        } catch (Exception e) {
            System.out.println("Error initiating server");
        }
    }

    /**
     * Listens for new Clients connections. Adds each new Socket connection to
     * ArrayList of Streams.
     */
    @Override
    public void run() {
        Thread.currentThread().setName("Server");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                //
                // need lock?
                Streams stream = new Streams(clientSocket);
                clients.add(stream);
            } catch (Exception e) {
                System.out.println("Error accepting socket");
            }
        }
    }

    // Returns a Thread Pool based on type.
    public static ThreadPool getPool(int type) {
        switch (type) {
            case 1:
                return S_Pool;
            case 2:
                return Cache_Pool;
            case 3:
                return Readers_Pool;
            case 4:
                return Writer_Pool;
            default:
                return null;
        }
    }

    public static int getrandomRange() {
        return randomRange;
    }

    public static int getClientsSize() {
        return clients.size();
    }

    public static Cache getCache() {
        return cache;
    }

    public static Database getDatabase() {
        return database;
    }

    public static ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public static void setReadWriteLock(ReadWriteLock readWriteLock) {
        Server.readWriteLock = readWriteLock;
    }
}

/**
 * A Thread that listens to the Clients Sockets. Iterates over the Clients
 * Sockets, and read queries.
 */
class socketsReader implements Runnable {

    private final ArrayList<Streams> clients;
    private Streams stream;

    public socketsReader(ArrayList clients) {
        this.clients = clients;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("SocketsReader");

        while (true) {
            //sleep while there are no clients connected.
            while (clients.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }
            for (int i = 0; i < clients.size(); i++) {
                stream = clients.get(i);
                Thread readData = new Thread(new socketDataReader(stream));
                readData.start();
                try {
                    // in case a socket is stuck, move to the next socket after 1000ms
                    int time = 0;
                    while (readData.isAlive() && time < 10) {
                        Thread.sleep(100);
                        time++;
                    }
                    readData.interrupt();
                } catch (Exception e) {
                    System.out.println("Join error");
                }
            }
        }
    }
}
