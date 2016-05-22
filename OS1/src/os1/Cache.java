/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Avinoam
 */
public class Cache {

    private int C, M, Min_Z, cacheSize;
    private static HashMap<Integer, Data> memory;
    private final ReentrantLock lock = new ReentrantLock(true);

    public Cache(int C, int M) {
        this.C = C;
        cacheSize = C;
        this.M = M;
        Min_Z = M;
        memory = new HashMap<>(C);
        memory.put(5, new Data(5, 2, 1));
    }

    // need lock?
    public int search(int x) {
        lock.lock();
        try {
            Data data = memory.get(x);
            if (data != null) {
                data.updateZ();
                return data.getY();
            }
            return -1;
        } finally {
            checkForUpdates();
            lock.unlock();
        }
    }

    // Update cache
    // update DB ?
    private void checkForUpdates() {
        if (Server.getDatabase().isCacheUpdateNeeded()) {
            HashMap<Integer, Data> updates = Server.getDatabase().getCacheUpdates();
            memory.putAll(updates);

            List<Data> values = new ArrayList(memory.values());
            Collections.sort(values, new Comparator<Data>() {
                @Override
                public int compare(Data o1, Data o2) {
                    return ((Integer) o2.getZ()).compareTo((Integer) o1.getZ());
                }
            });

            //
            // check indexs
            if (values.size() > cacheSize) {
                for (int i = cacheSize; i < values.size(); i++) {
                    memory.remove(values.get(i).getX());
                }
                setMin_Z(values.get(cacheSize - 1).getZ());
            } else {
                setMin_Z(values.get(values.size()).getZ());
            }

            Server.getDatabase().clearCacheUpdates();
            Server.getDatabase().setCacheUpdateNeeded(false);
        }
    }

    public int getMin_Z() {
        return Min_Z;
    }

    public void setMin_Z(int Min_Z) {
        this.Min_Z = Min_Z;
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
