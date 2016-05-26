/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread Pool. Holds a Task Queue, and a fixed amount of Threads that tries to
 * pull tasks from the queue and execute them.
 */
public class ThreadPool {

    private BlockingQueue taskQueue = new BlockingQueue();
    private List<TaskExecutingThread> threads = new ArrayList<>();
    private boolean alive = true;
    private final ReentrantLock lock = new ReentrantLock(true);

    public ThreadPool(int size) {
        for (int i = 0; i < size; i++) {
            threads.add(new TaskExecutingThread(taskQueue));
        }
        for (TaskExecutingThread thread : threads) {
            thread.start();
        }
    }

    //
    // family?
//    public ThreadPool(int size, String familyName) {
//        for (int i = 0; i < size; i++) {
//            threads.add(new TaskExecutingThread(taskQueue));
//        }
//        int familyId = 0;
//        for (TaskExecutingThread thread : threads) {
//            thread.setName(familyName + (familyId++));
//            thread.start();
//        }
//    }
    // Adds a Task to the Queue.
    public void execute(Runnable task) {
        lock.lock();
        try {
            if (!alive) {
                throw new IllegalStateException("ThreadPool is currently shutdown");
            }
            this.taskQueue.enqueue(task);
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            for (TaskExecutingThread thread : threads) {
                thread.stopRunning();
            }
            alive = false;
        } finally {
            lock.unlock();
        }
    }
}
