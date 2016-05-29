/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the Cache.
 */
public class Cache {

    private int C, M, Min_Z, cacheSize;
    private static HashMap<Integer, Data> memory;
    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * Cache constructor.
     *
     * @param C Cache size
     * @param M Minimum Z to enter the cache
     */
    public Cache(int C, int M) {
        this.C = C;
        cacheSize = C;
        this.M = M;
        Min_Z = M;
        memory = new HashMap<>();
    }

    /**
     * Searching for x in the Cache HashMap. if an answer is found in the cache,
     * increase Z and return the answer to the S_Thread. After the search is
     * completed, check if cache update is needed.
     *
     * @param x query
     * @return the answer - y
     */
    public int search(int x) {
        lock.lock();
        try {
            Data data = memory.get(x);
            if (data != null) {
                Server.getDatabase().getDatabaseUpdates().put(data);
                System.out.println("Cache response: " + data.toString());
                return data.getY();
            }
            return -1;
        } finally {
            checkForUpdates();
            lock.unlock();
        }
    }

    /**
     * Checks if cache update is needed. if so, merge the cache hashmap and the
     * updates hashmap, sort them by Z, and take the highest queries. also
     * update minimum Z (by taking the least Z in the hashmap).
     *
     * @param memory The Cache HashMap.
     */
    private void checkForUpdates() {
        if (Server.getDatabase().isUpdateNeeded()) {
            new Thread(new CacheUpdater()).start();
        }
    }

    public void update() {
        lock.lock();
        try {
            HashMap<Integer, Data> updates = Server.getDatabase().getCacheUpdates().getAll();
            memory.putAll(updates);
            if (memory.size() > cacheSize) {
                List<Data> values = new ArrayList(memory.values());
                Collections.sort(values);
                for (int i = cacheSize; i < values.size(); i++) {
                    memory.remove(values.get(i).getX());
                }
                setMin_Z(values.get(cacheSize - 1).getZ());
            }
            Server.getDatabase().clearCacheUpdates();
            Server.getDatabase().setUpdateNeeded(false);
        } finally {
            lock.unlock();
        }
    }

    public int getMin_Z() {
        lock.lock();
        try {
            return Min_Z;
        } finally {
            lock.unlock();
        }
    }

    public void setMin_Z(int Min_Z) {
        lock.lock();
        try {
            this.Min_Z = Min_Z;
        } finally {
            lock.unlock();
        }
    }

}

/**
 * C Thread. The only thread that access the cache. Searching for answers, and
 * updating the cache.
 */
class CacheSearcher implements Runnable {

    private final S_Thread thread;
    private final int query;
    private final Semaphore semaphore;

    /**
     * @param thread The S_Thread that called this thread.
     * @param query The x that we are looking for.
     * @param semaphore The blocking sempahore, used to make the S_Thread wait
     * for the cache search to finish.
     */
    public CacheSearcher(S_Thread thread) {
        this.thread = thread;
        semaphore = thread.getSemaphore();
        query = thread.getQuery();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("CacheSearcher");
        thread.setAnswer(Server.getCache().search(query));
        semaphore.release();
    }
}

class CacheUpdater implements Runnable {

    @Override
    public void run() {
        Server.getCache().update();
    }
}
