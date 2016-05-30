/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A queue that blocks pulling from it when its empty.
 */
public class BlockingQueue {

    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition hasItems = lock.newCondition();
    private List queue = new LinkedList();

    //Add item
    public void enqueue(Object obj) {
        lock.lock();
        try {
            this.queue.add(obj);
            hasItems.signal();
        } finally {
            lock.unlock();
        }
    }

    //Remove item
    public Object dequeue() throws InterruptedException {
        lock.lock();
        try {
            while (this.queue.isEmpty()) {
                hasItems.await();
            }
            return this.queue.remove(0);
        } finally {
            lock.unlock();
        }
    }

    //Check if queue is empty
    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
