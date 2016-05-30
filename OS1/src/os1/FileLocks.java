/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class FileLocks {

    private HashMap<String, ReadWriteLock> map;
    private final ReentrantLock lock = new ReentrantLock(true);

    public FileLocks() {
        map = new HashMap();
    }

    //delete this ?
    private void createLock(String str) {
        lock.lock();
        try {
            ReadWriteLock readWriteLock = new ReadWriteLock();
            map.put(str, readWriteLock);
        } finally {
            lock.unlock();
        }
    }

    public ReadWriteLock get(String str) {
        lock.lock();
        try {
            if (map.get(str) == null) {
                createLock(str);
            }
            return map.get(str);
        } finally {
            lock.unlock();
        }
    }

}
