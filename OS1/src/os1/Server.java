/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

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
    private static ThreadPool S_Pool, Cache_Pool, Readers_Pool;
    private static Cache cache;
    private static Database database;
    private static TempDataList tempDataList = new TempDataList();
    private static ArrayList<Streams> clients = new ArrayList();
    private final ReentrantLock lock = new ReentrantLock(true);
    public static final int Type_S_Pool = 1;
    public static final int Type_Cache_Pool = 2;
    public static final int Type_Readers_Pool = 3;

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
            //cache pool size?
            Cache_Pool = new ThreadPool(1);
            Readers_Pool = new ThreadPool(Y);

            cache = new Cache(C, M);
            database = new Database();

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
                Streams stream = new Streams(clientSocket);
                clients.add(stream);
            } catch (Exception e) {
                System.out.println("Error accepting socket");
            }
        }
    }

    public static ThreadPool getPool(int type) {
        switch (type) {
            case 1:
                return S_Pool;
            case 2:
                return Cache_Pool;
            case 3:
                return Readers_Pool;
            default:
                return null;
        }
    }

    public static int getClientsSize() {
        return clients.size();
    }

    public static void addToTempDataList(int x) {
        tempDataList.add(x);
    }

    public static TempDataList getTempDataList() {
        return tempDataList;
    }

    public static Cache getCache() {
        return cache;
    }

    public static Database getDatabase() {
        return database;
    }
}

class socketsReader implements Runnable {

    private ArrayList<Streams> clients;
    private Streams stream;

    public socketsReader(ArrayList clients) {
        this.clients = clients;
    }

    @Override
    public void run() {

        while (true) {
            while (clients.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }
            for (int i = 0; i < clients.size(); i++) {
                stream = clients.get(i);
                // more params ?
                Thread readData = new Thread(new socketDataReader(stream));
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
