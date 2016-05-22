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
public class Database {

    private TempDataList databaseUpdates;
    private HashMap<Integer, Data> cacheUpdates;
    boolean tempFoundAns = false; // need to delete this
    private final ReentrantLock lock = new ReentrantLock(true);
    private int updatesSize = 10; // what size?
    private boolean cacheUpdateNeeded = false;

    public Database() {
        databaseUpdates = new TempDataList();
        cacheUpdates = new HashMap<>(updatesSize); // what size?
    }

    public int search(int x) {
        /*
        search in DB
        if found, add to TempDataList and return ans to socket
         */

        //if found ans
        if (tempFoundAns) {
            // data - the ans found
            Data data = new Data();
            if (data.getZ() >= Server.getCache().getMin_Z()) {
                addUpdateCandidate(data);
            }
            databaseUpdates.add(data.getX());
        }
        return -1;
    }

    public int generate(int x) {
        // generate new response for x, and write it to DB (Thread W)
        // might be good idea to update DB while doing this
        // also update Cache?
        // cache update candidate?

        //choose random number ?
        return 1;
    }

    public void addUpdateCandidate(Data data) {
        lock.lock();
        try {
            Data value = cacheUpdates.get(data.getX());
            if (value != null) {
                data.setZ(value.getZ() + 1);
            } else {
                data.updateZ();
            }
            cacheUpdates.put(data.getX(), data);
            if (cacheUpdates.size() >= updatesSize) {
                setCacheUpdateNeeded(true);
                // also update DB ?
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isCacheUpdateNeeded() {
        return cacheUpdateNeeded;
    }

    public void setCacheUpdateNeeded(boolean cacheUpdateNeeded) {
        this.cacheUpdateNeeded = cacheUpdateNeeded;
    }

    public HashMap<Integer, Data> getCacheUpdates() {
        return cacheUpdates;
    }

    public void setCacheUpdates(HashMap<Integer, Data> cacheUpdates) {
        this.cacheUpdates = cacheUpdates;
    }

    public void clearCacheUpdates() {
        cacheUpdates.clear();
    }

    public TempDataList getDatabaseUpdates() {
        return databaseUpdates;
    }

    public void setDatabaseUpdates(TempDataList databaseUpdates) {
        this.databaseUpdates = databaseUpdates;
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
