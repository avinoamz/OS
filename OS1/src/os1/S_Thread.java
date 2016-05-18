/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Avinoam
 */
public class S_Thread implements Runnable {

    private Socket socket;
    private int msg, answer;
    private Semaphore semaphore;

    public S_Thread(Socket socket, int msg) {
        this.socket = socket;
        this.msg = msg;
        semaphore = new Semaphore(0);
    }

    @Override
    public void run() {

        Server.getPool(Server.Type_Cache_Pool).execute(new CacheSearcher(this));
        try {
            semaphore.acquire();
            if (answer != -1) {
                new ObjectOutputStream(socket.getOutputStream()).writeObject(answer);
            } else {
                // ask DB (also make sure the DB generate new ans if not found)
            }
        } catch (Exception e) {
            System.out.println("Error while waiting for CachePool Semaphore");
        }


        /*
        ask the cache
        ask the db
        generate new ans
        
        return the answer, and add the query to tempDataBase
         */
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public int getQuery() {
        return msg;
    }

    public void setAnswer(int y) {
        answer = y;
    }
}
