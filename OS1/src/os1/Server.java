/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Server. Initialize the Thread Pools, and listen to new client
 * connections.
 *
 */
public class Server implements Runnable {
    
    private ServerSocket serverSocket;
    private final int S, C, M, L, Y;
    private static int randomRange;
    private static ThreadPool S_Pool, Cache_Pool, Readers_Pool, Writer_Pool, Socket_Pool;
    private static Cache cache;
    private static Database database;
    private static ReadWriteLock readWriteLock = new ReadWriteLock();
    private static final SyncedHashMap tempDataList = new SyncedHashMap();
    private static final ArrayList<Streams> clients = new ArrayList();
    private static final ArrayList<Streams> clients2 = new ArrayList();
    private static final ArrayList<Streams> clients3 = new ArrayList();
    public static final int Type_S_Pool = 1;
    public static final int Type_Cache_Pool = 2;
    public static final int Type_Readers_Pool = 3;
    public static final int Type_Writer_Pool = 4;
    public static final int Type_Socket_Pool = 5;
    
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
            serverSocket = new ServerSocket(45001);
            
            S_Pool = new ThreadPool(S);
            Readers_Pool = new ThreadPool(Y);
            Cache_Pool = new ThreadPool(1);
            Writer_Pool = new ThreadPool(1);

            // A Thread that listens to client Sockets.
            new Thread(new socketsReader(clients)).start();
            new Thread(new socketsReader(clients2)).start();
            new Thread(new socketsReader(clients3)).start();
            
            cache = new Cache(C, M);
            database = new Database(L, C);
            
        } catch (Exception e) {
            e.printStackTrace();
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
        int clientNumber = 0;
        int location;
        
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Streams stream = new Streams(clientSocket);
                location = (clientNumber++) % 3;
                switch (location) {
                    case 0:
                        clients.add(stream);
                        break;
                    case 1:
                        clients2.add(stream);
                        break;
                    default:
                        clients3.add(stream);
                        break;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
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
            case 5:
                return Socket_Pool;
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
                //  Server.getPool(Server.Type_Socket_Pool).execute(new Thread(new socketDataReader(stream)));
                Thread readData = new Thread(new socketDataReader(stream));
                readData.start();
                
                try {
                    while (readData.isAlive()) {
                        Thread.sleep((long) 0.1);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(socketsReader.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        }
    }
}
