/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Synchronized HashMap. Used for temporary data sturctures.
 */
public class SyncedHashMap {

    private final HashMap<Integer, Data> map = new HashMap();
    private final ReentrantLock lock = new ReentrantLock(true);

    // Adds a query to the HashMap, and increase it by 1.
    public void put(Data num) {
        lock.lock();
        try {
            Data value = map.get(num.getX());
            if (value == null) {
                num.updateZ();
            } else {
                num.setZ(value.getZ() + 1);
            }
            map.put(num.getX(), num);
        } finally {
            lock.unlock();
        }
    }

    public Data get(int key) {
        lock.lock();
        try {
            return map.get(key);
        } finally {
            lock.unlock();
        }
    }

    public HashMap getAll() {
        lock.lock();
        try {
            return map;
        } finally {
            lock.unlock();
        }
    }

    public Data remove(int key) {
        lock.lock();
        try {
            return map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            map.clear();
        } finally {
            lock.unlock();
        }
    }

    public List<Data> getValues() {
        lock.lock();
        try {
            List<Data> values = new ArrayList(map.values());
            return values;
        } finally {
            lock.unlock();
        }
    }
}
