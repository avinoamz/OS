/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

/**
 * S_Thread. Recieves a query and finds an answer by searching the cache first,
 * and then the database.
 */
public class S_Thread implements Runnable {

    private Streams stream;
    private int msg, answer;
    private Semaphore semaphore;
    private PrintWriter out;

    public S_Thread(Streams stream, int msg) {
        this.msg = msg;
        semaphore = new Semaphore(0);
        this.stream = stream;
        try {
            out = stream.getOut();
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
                out.println(answer);
            } else {
                Server.getPool(Server.Type_Readers_Pool).execute(new DatabaseReader(this));
                // waits until database search is completed.
                semaphore.acquire();
                // in case no answer was found or generated.
                if (answer == -1) {
                    System.out.println("Error generating response");
                }
                out.println(answer);
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
