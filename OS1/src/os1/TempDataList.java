/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Avinoam
 */
public class TempDataList {

    private ArrayList<Integer> data = new ArrayList();
    private final ReentrantLock lock = new ReentrantLock(true);

    public void add(int num) {
        lock.lock();
        try {
            data.add(num);
        } finally {
            lock.unlock();
        }
    }

    public int get(int index) {
        lock.lock();
        try {
            return data.get(index);
        } finally {
            lock.unlock();
        }
    }

    public int remove(int index) {
        lock.lock();
        try {
            return data.remove(index);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return data.size();
        } finally {
            lock.unlock();
        }
    }
}
