/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.concurrent.Semaphore;

/**
 * ReadWriteLocks. Used in order to synchronize reading and writing to the
 * Database. While someone is reading, no one can write, and vice-versa.
 */
public class ReadWriteLock {

    private int writers = 0;
    private int readers = 0;
    Semaphore Rmutex = new Semaphore(1, true);
    Semaphore Wmutex = new Semaphore(1, true);
    Semaphore Mutex2 = new Semaphore(1, true);
    Semaphore Rdb = new Semaphore(1, true);
    Semaphore Wdb = new Semaphore(1, true);

    public void ReadLock() {
        try {
            Mutex2.acquire();
            Rdb.acquire();
            Rmutex.acquire();
            readers += 1;
            if (readers == 1) {
                Wdb.acquire();
            }
            Rmutex.release();
            Rdb.release();
            Mutex2.release();
        } catch (InterruptedException ex) {
            System.err.println("Error locking read.");
        }
    }

    public void ReadUnlock() {
        try {
            Rmutex.acquire();
            readers -= 1;
            if (readers == 0) {
                Wdb.release();
            }
            Rmutex.release();
        } catch (InterruptedException ex) {
            System.err.println("Error unlocking read.");
        }
    }

    public void WriteLock() {
        try {
            Wmutex.acquire();
            writers += 1;
            if (writers == 1) {
                Rdb.acquire();
            }
            Wmutex.release();
            Wdb.acquire();
        } catch (InterruptedException ex) {
            System.err.println("Error locking write");
        }
    }

    public void WriteUnlock() {
        try {
            Wdb.release();
            Wmutex.acquire();
            writers -= 1;
            if (writers == 0) {
                Rdb.release();
            }
            Wmutex.release();
        } catch (InterruptedException ex) {
            System.err.println("Error unlocking write");
        }
    }
}
