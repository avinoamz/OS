/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avinoam
 */
public class Database {

    private TempDataList databaseUpdates, cacheUpdates;
    private final ReentrantLock lock = new ReentrantLock(true);
    private int updatesSize = 5; // what size?
    private boolean updateNeeded = false;
    private boolean databaseUpdateNeeded = false;
    private final int fileSize = 1000; // size?
    private final int readSize = Integer.BYTES * 3;
    private final int randomRange;

    //temp locks
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public Database(int range) {
        databaseUpdates = new TempDataList();
        cacheUpdates = new TempDataList();
        this.randomRange = range;
    }

    public int search(int x) {
        // readLock.lock();
        lock.lock();
        try {
            RandomAccessFile file = new RandomAccessFile(getFile(x), "r");
            file.seek(getPosition(x));
            Data data = new Data(file.readInt(), file.readInt(), file.readInt());
            if (data.getX() != 0 || ((x == 0) && data.getX() == 0)) {
                System.out.println("success read from db " + data.getX() + " " + data.getY() + " " + data.getZ());
                databaseUpdates.put(data);
                if (data.getZ() >= Server.getCache().getMin_Z()) {
                    cacheUpdates.put(data);
                }
                // depends on size, or times accessed?
                if (databaseUpdates.size() > updatesSize) {
                    callWriter();
                }
                file.close();
                return data.getY();
            } else {
                return generate(x);
            }
        } catch (IOException ex) {
            return generate(x);
        } finally {
            lock.unlock();
        }
    }

    private int generate(int x) {
        lock.lock();
        try {
            Data data;
            if ((data = databaseUpdates.get(x)) != null) {
                data.updateZ();
                return data.getY();
            }
            int y = (int) (Math.random() * randomRange) + 1;
            data = new Data(x, y, 1);
            databaseUpdates.put(data);
            if (databaseUpdates.size() > updatesSize) {
                callWriter();
            }
            return y;
        } finally {
            lock.unlock();
        }
    }

    private void callWriter() {
        Semaphore semaphore = new Semaphore(0, true);
        setUpdateNeeded(true); //setUpdate before or after DB Writer?
        Server.getPool(Server.Type_Writer_Pool).execute(new DatabaseWriter(semaphore));
//        try {
//            semaphore.acquire();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void update() throws FileNotFoundException, IOException {

        lock.lock();
        // writeLock.lock();
        try {
            List<Data> list = databaseUpdates.getValues();
            for (int i = 0; i < list.size(); i++) {
                Data data = databaseUpdates.get(list.get(i).getX());

                RandomAccessFile file = new RandomAccessFile(getFile(data.getX()), "rw");

                file.seek(getPosition(data.getX()));
                file.writeInt(data.getX());
                file.writeInt(data.getY());
                file.writeInt(data.getZ());
                file.close();
            }
            databaseUpdates.clear();
        } finally {
            lock.unlock();
            // writeLock.unlock();
        }
    }

    // lock functions?
    
    private String getFile(int x) {
        return "file_" + (x / fileSize) * fileSize;
    }

    private int getPosition(int x) {
        return Math.abs(x % (fileSize)) * readSize;
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public void setUpdateNeeded(boolean updateNeeded) {
        this.updateNeeded = updateNeeded;
    }

    public TempDataList getCacheUpdates() {
        return cacheUpdates;
    }

    public void setCacheUpdates(TempDataList cacheUpdates) {
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
        thread.setAnswer(Server.getDatabase().search(query));
        semaphore.release();
    }
}

class DatabaseWriter implements Runnable {

    private Semaphore semaphore;
    private TempDataList updates;
    private Lock writeLock;

    public DatabaseWriter(Semaphore semaphore) {
        this.semaphore = semaphore;
        updates = Server.getDatabase().getDatabaseUpdates();
        writeLock = Server.getDatabase().getWriteLock();
    }

    @Override
    public void run() {

        try {
            Server.getDatabase().update();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatabaseWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DatabaseWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        semaphore.release();
    }

}
