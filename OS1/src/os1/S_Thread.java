/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;

/**
 * S_Thread. Recieves a query and finds an answer by searching the cache first,
 * and then the database.
 */
public class S_Thread implements Runnable {

    private int msg, answer;
    private Semaphore semaphore;
    private ObjectOutputStream out;

    public S_Thread(Streams stream, int msg) {
        this.msg = msg;
        semaphore = new Semaphore(0);
        try {
            out = stream.getOut();
            out.flush();
        } catch (Exception e) {
            System.err.println("Error starting S_Thread");
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("S_Thread");

        try {
            Server.getPool(Server.Type_Cache_Pool).execute(new CacheSearcher(this));
            // waits until cache search is completed.
            semaphore.acquire();
            if (answer != -1) {
                out.writeObject(answer);
                out.flush();
            } else {
                Server.getPool(Server.Type_Readers_Pool).execute(new DatabaseReader(this));
                // waits until database search is completed.
                semaphore.acquire();
                // in case no answer was found or generated.
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
