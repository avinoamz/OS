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

    private Streams stream;
    private int msg, answer;
    private Semaphore semaphore;
    private ObjectOutputStream out;

    public S_Thread(Streams stream, int msg) {
        this.stream = stream;
        this.msg = msg;
        semaphore = new Semaphore(0);
        try {
            out = stream.getOut();
            out.flush();
        } catch (Exception e) {
        }
    }

    @Override
    public void run() {

        try {
            Server.getPool(Server.Type_Cache_Pool).execute(new CacheSearcher(this));
            semaphore.acquire();
            if (answer != -1) {
                out.writeObject(answer);
                out.flush();
            } else {
                Server.getPool(Server.Type_Readers_Pool).execute(new DatabaseReader(this));
                semaphore.acquire();
                // in case no answer was found or generated
                if (answer == -1) {
                    System.out.println("Error generating response");
                }
                out.writeObject(answer);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("S_Thread error while finding answer");
            e.printStackTrace();
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
