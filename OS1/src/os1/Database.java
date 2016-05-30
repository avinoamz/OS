/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.filechooser.FileSystemView;

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
    private final int databaseUpdateSize; // what size?
    private int cacheUpdateSize;
    private boolean updateNeeded = false;
    private final int fileSize = 500; // size?
    private final int readSize = Integer.BYTES * 3;
    private final int randomRange;
    private final FileLocks locks;

    public Database(int range, int size) {
        this.locks = new FileLocks();
        databaseUpdates = new SyncedHashMap();
        cacheUpdates = new SyncedHashMap();
        this.randomRange = range;
        databaseUpdateSize = 300;
        cacheUpdateSize = 5;
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
        ReadWriteLock fileLock = locks.get(getFile(x));
        fileLock.ReadLock();
        try {

            RandomAccessFile file = new RandomAccessFile(getFile(x), "r");
            file.seek(getPosition(x));
            Data data = new Data(file.readInt(), file.readInt(), file.readInt());
            file.close();
            if (data.getX() != 0) {
                //   System.out.println("db response: " + data.toString());
                databaseUpdates.put(data);
                if (data.getZ() >= Server.getCache().getMin_Z()) {
                    // double up 
                    // double up temporary fix
                    // double up 
                    data.setZ(data.getZ() - 1);
                    cacheUpdates.put(data);
                    if (cacheUpdates.size() > cacheUpdateSize) {
                        setUpdateNeeded(true);
                        if (cacheUpdateSize < (databaseUpdateSize / 10)) {
                            cacheUpdateSize++;
                        }
                    }
                }
                // depends on size, or times accessed?
                if (databaseUpdates.size() > databaseUpdateSize) {
                    callWriter();
                }
                return data.getY();
            } else if (x == 0) {
                if (data.getY() != 0) {
                    return data.getY();
                } else {
                    return generate(x);
                }
            } else {
                return generate(x);
            }
        } catch (Exception ex) {
            //  ex.printStackTrace();
            return generate(x);
        } finally {
            fileLock.ReadUnlock();
            //   locks.get(getFile(x)).ReadUnlock();
            // locks.ReadUnlock();
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
        //  lock.lock();
        try {
            Data data;
            if ((data = databaseUpdates.get(x)) != null) {                
                data.updateZ();
                // System.err.println("TEMP DB response: " + data.toString());
                return data.getY();
            }
            int y = (int) (Math.random() * randomRange) + 1;
            data = new Data(x, y, 0);
            databaseUpdates.put(data);
            if (databaseUpdates.size() > databaseUpdateSize) {
                callWriter();
            }
            return y;
        } finally {
            //   lock.unlock();
        }
    }

    // Activate the Database Writer.
    private void callWriter() {
        Server.getPool(Server.Type_Writer_Pool).execute(new DatabaseWriter());
    }

    /**
     * Updates the Database. Iterates over databaseUpdates, and write it to the
     * Database files using RandomAccessFile.
     */
    public void update() {

        try {
            
          //  HashMap<Integer, Data> tempMap = databaseUpdates.cloneMap();
            
            List<Data> list = new ArrayList(databaseUpdates.getValues());
            Collections.sort(list, new Comparator<Data>() {
                @Override
                public int compare(Data o1, Data o2) {
                    return ((Integer) o1.getX()).compareTo((Integer) o2.getX());
                }
            });

            for (int i = 0; i < list.size(); i++) {

                ReadWriteLock fileLock = locks.get(getFile(list.get(i).getX()));
                fileLock.WriteLock();

                Data data = list.get(i);
                RandomAccessFile file = new RandomAccessFile(getFile(data.getX()), "rw");
                do {
                    data = list.get(i);
                    file.seek(getPosition(data.getX()));
                    file.writeInt(data.getX());
                    file.writeInt(data.getY());
                    file.writeInt(data.getZ());
                    i++;
                } while (i < list.size() && (getFile(list.get(i - 1).getX()).equals(getFile(list.get(i).getX()))));

                i--;
                file.close();
                fileLock.WriteUnlock();
            }
            databaseUpdates.clear();
        } catch (Exception e) {           
            e.printStackTrace();
            System.err.println("Error updating database");
        } finally {
            // unlock all locks ?
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
        lock.lock();
        try {
            return updateNeeded;
        } finally {
            lock.unlock();
        }
    }

    public void setUpdateNeeded(boolean updateNeeded) {
        lock.lock();
        try {
            this.updateNeeded = updateNeeded;
        } finally {
            lock.unlock();
        }
    }

    public SyncedHashMap getCacheUpdates() {
        lock.lock();
        try {
            return cacheUpdates;
        } finally {
            lock.unlock();
        }
    }

    public void setCacheUpdates(SyncedHashMap cacheUpdates) {
        lock.lock();
        try {
            this.cacheUpdates = cacheUpdates;
        } finally {
            lock.unlock();
        }
    }

    public void clearCacheUpdates() {
        lock.lock();
        try {
            cacheUpdates.clear();
        } finally {
            lock.unlock();
        }
    }

    public SyncedHashMap getDatabaseUpdates() {
        lock.lock();
        try {
            return databaseUpdates;
        } finally {
            lock.unlock();
        }
    }

    public void setDatabaseUpdates(SyncedHashMap databaseUpdates) {
        lock.lock();
        try {
            this.databaseUpdates = databaseUpdates;
        } finally {
            lock.unlock();
        }

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
        Thread.currentThread().setName("DatabaseReader");
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
        Thread.currentThread().setName("DatabaseWriter");
        Server.getDatabase().update();
    }
}
