/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.concurrent.locks.ReentrantLock;

/**
 * The executable Thread of the Thread Pools. Runs in loop, trying to pull tasks
 * from the task queue, and execute them.
 */
public class TaskExecutingThread extends Thread {

    private final ReentrantLock lock = new ReentrantLock(true);
    private BlockingQueue taskQueue = null;
    private boolean keepRunning = true;

    public TaskExecutingThread(BlockingQueue queue) {
        taskQueue = queue;
    }

    @Override
    public void run() {
        while (keepRunning()) {
            try {
                Runnable runnable = (Runnable) taskQueue.dequeue();
                runnable.run();
            } catch (InterruptedException e) {
                System.out.println("TaskExecutingThread crashed");
            }
        }
    }

    public void stopRunning() {
        lock.lock();
        try {
            keepRunning = false;
            this.interrupt();
        } finally {
            lock.unlock();
        }
    }

    public boolean keepRunning() {
        lock.lock();
        try {
            return keepRunning;
        } finally {
            lock.unlock();
        }
    }
}
