/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Database.
 *
 * @databaseUpdates - Synchronized HashMap that saves future database updates.
 * @cacheUpdates - Synchronized HashMap that saves future cache updates.
 * @updateSize - Defines the amount of queries until update triggers.
 * @fileSize - Defines the size of each file in database.
 * @readSize - Define the jumps in database (int * 3) = (x,y,z).
 * @randomRange - The Database range for drawing new responds for queries.
 */
public class Database {

    private SyncedHashMap databaseUpdates, cacheUpdates;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final int updatesSize = 50; // what size?
    private boolean updateNeeded = false;
    private final int fileSize = 1000; // size?
    private final int readSize = Integer.BYTES * 3;
    private final int randomRange;
    private static final ReadWriteLock locks = Server.getReadWriteLock();

    public Database(int range) {
        databaseUpdates = new SyncedHashMap();
        cacheUpdates = new SyncedHashMap();
        this.randomRange = range;
    }

    /**
     * Search for x in the Database. Access the database files using
     * RandomAccessFile. find the file name and position using getFile and
     * getPosition functions, and then read the data. If an answer is found,
     * return it to the S_Thread, else, generate new answer. Also checks if
     * update is needed.
     *
     * @param x query
     * @return y
     */
    public int search(int x) {
        try {

            locks.ReadLock();

            RandomAccessFile file = new RandomAccessFile(getFile(x), "rw");
            file.seek(getPosition(x));
            Data data = new Data(file.readInt(), file.readInt(), file.readInt());
            if (data.getX() != 0 || ((x == 0) && data.getX() == 0)) {
                System.out.println("db response: " + data.toString());
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
        } catch (Exception ex) {
            return generate(x);
        } finally {
            locks.ReadUnlock();
        }
    }

    /**
     * Generate new answers if not found in the Database. Generate a new
     * response, and returns the answer to the S_Thread. Also adds the answer to
     * databaseUpdates. P.S - before generating a new response, check if it
     * already exist in databaseUpdates.
     *
     * @param x the query
     * @return y
     */
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

    // Activate the Database Writer. also activates cache updates.
    private void callWriter() {
        setUpdateNeeded(true);
        Server.getPool(Server.Type_Writer_Pool).execute(new DatabaseWriter());
    }

    /**
     * Updates the Database. Iterates over databaseUpdates, and write it to the
     * Database files using RandomAccessFile.
     */
    public void update() {
        try {

            locks.WriteLock();

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
        } catch (Exception e) {
            System.err.println("Error updating database");
        } finally {
            locks.WriteUnlock();
        }
    }

    // Returns the file name that holds x.
    private String getFile(int x) {
        if (x < 0) {
            return "C:\\Users\\Avinoam\\Documents\\OS1\\OS1\\OS1\\database/file_" + ((x / fileSize) - 1) * fileSize;
        }
        return "C:\\Users\\Avinoam\\Documents\\OS1\\OS1\\OS1\\database/file_" + (x / fileSize) * fileSize;
    }

    // Returns the location of x inside the file.
    private int getPosition(int x) {
        return Math.abs(x % (fileSize)) * readSize;
    }

    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    public void setUpdateNeeded(boolean updateNeeded) {
        this.updateNeeded = updateNeeded;
    }

    public SyncedHashMap getCacheUpdates() {
        return cacheUpdates;
    }

    public void setCacheUpdates(SyncedHashMap cacheUpdates) {
        this.cacheUpdates = cacheUpdates;
    }

    public void clearCacheUpdates() {
        cacheUpdates.clear();
    }

    public SyncedHashMap getDatabaseUpdates() {
        return databaseUpdates;
    }

    public void setDatabaseUpdates(SyncedHashMap databaseUpdates) {
        this.databaseUpdates = databaseUpdates;
    }
}

/**
 * Y Reader. The Thread that search in the Database by calling the search
 * method. Semaphore is used to update the S_Thread that the search is
 * completed.
 */
class DatabaseReader implements Runnable {

    private final S_Thread thread;
    private final int query;
    private final Semaphore semaphore;

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

/**
 * W Thread. The Thread that updates the Database by calling the update method.
 * while update is running, all reading from Database is blocked.
 */
class DatabaseWriter implements Runnable {

    @Override
    public void run() {
        Server.getDatabase().update();
    }
}
