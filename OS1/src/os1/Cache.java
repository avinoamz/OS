/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Avinoam
 */
public class Cache {

    private int C, M;
    private static HashMap<Integer, Data> memory;
    private final ReentrantLock lock = new ReentrantLock(true);

    public Cache(int C, int M) {
        this.C = C;
        this.M = M;
        memory = new HashMap<>(C);
        memory.put(5, new Data(2, 1));
    }

    public int search(int x) {
        lock.lock();
        // null pointer?
        try {
            Data data = memory.get(x);
            if (data != null) {
                Server.addToTempDataList(x);
                return data.getY();
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }
}

class CacheSearcher implements Runnable {

    private S_Thread thread;
    private int query;
    private Semaphore semaphore;

    public CacheSearcher(S_Thread thread) {
        this.thread = thread;
        semaphore = thread.getSemaphore();
        query = thread.getQuery();
    }

    @Override
    public void run() {
        thread.setAnswer(Server.getCache().search(query));
        semaphore.release();
    }
}
