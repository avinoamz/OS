/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that represents (X,Y,Z).
 */
public class Data {

    private int x, y, z;
    private final ReentrantLock lock = new ReentrantLock(true);

    public Data() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Data(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        lock.lock();
        try {
            return x;
        } finally {
            lock.unlock();
        }
    }

    public void setX(int x) {
        lock.lock();
        try {
            this.x = x;
        } finally {
            lock.unlock();
        }
    }

    public int getY() {
        lock.lock();
        try {
            return y;
        } finally {
            lock.unlock();
        }
    }

    public void setY(int y) {
        lock.lock();
        try {
            this.y = y;
        } finally {
            lock.unlock();
        }
    }

    public int getZ() {
        lock.lock();
        try {
            return z;
        } finally {
            lock.unlock();
        }
    }

    public void setZ(int z) {
        lock.lock();
        try {
            this.z = z;
        } finally {
            lock.unlock();
        }
    }

    // Increase Z.
    public void updateZ() {
        lock.lock();
        try {
            this.z++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

}
