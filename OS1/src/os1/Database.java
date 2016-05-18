/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.concurrent.Semaphore;

/**
 *
 * @author Avinoam
 */
public class Database {

    public int search(int x) {
        /*
        search in DB
        if found, add to TempDataList and return ans to socket
         */
        return -1;
    }

    public int generate(int x) {
        // generate new response for x, and write it to DB (Thread W)
        // might be good idea to update DB while doing this

        //choose random number ?
        return 1;
    }
}

class DatabaseReader implements Runnable {

    private S_Thread thread;
    private int query, answer;
    private Semaphore semaphore;

    public DatabaseReader(S_Thread thread) {
        this.thread = thread;
        this.semaphore = thread.getSemaphore();
        query = thread.getQuery();
    }

    @Override
    public void run() {
        answer = Server.getDatabase().search(query);
        if (answer != -1) {
            thread.setAnswer(answer);
            semaphore.release();
        } else {
            thread.setAnswer(Server.getDatabase().generate(query));
            semaphore.release();
        }
    }
}
