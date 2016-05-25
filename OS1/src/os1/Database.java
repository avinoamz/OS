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
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avinoam
 */
public class Database {

    private TempDataList databaseUpdates, cacheUpdates;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final int updatesSize = 50; // what size?
    private boolean updateNeeded = false;
    private final int fileSize = 1000; // size?
    private final int readSize = Integer.BYTES * 3;
    private final int randomRange;
    private int writers = 0;
    private int readers = 0;
    Semaphore Rmutex = new Semaphore(1, true);
    Semaphore Wmutex = new Semaphore(1, true);
    Semaphore Mutex2 = new Semaphore(1, true);
    Semaphore Rdb = new Semaphore(1, true);
    Semaphore Wdb = new Semaphore(1, true);

    public Database(int range) {
        databaseUpdates = new TempDataList();
        cacheUpdates = new TempDataList();
        this.randomRange = range;
    }

    public int search(int x) {
        //    lock.lock();
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
            try {
                // lock.unlock();
                Rmutex.acquire();
                readers -= 1;
                if (readers == 0) {
                    Wdb.release();
                }
                Rmutex.release();
            } catch (InterruptedException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private int generate(int x) {
        lock.lock();
        try {
            Data data;
            if ((data = databaseUpdates.get(x)) != null) {
                data.updateZ();
                System.err.println("TempDatabase response: ");
                return data.getY();
            }
            int y = (int) (Math.random() * randomRange) + 1;
            data = new Data(x, y, 1);
            databaseUpdates.put(data);
            if (databaseUpdates.size() > updatesSize) {
                callWriter();
            }
            System.err.println("Generated response: ");
            return y;
        } finally {
            lock.unlock();
        }
    }

    private void callWriter() {
        setUpdateNeeded(true); //setUpdate before or after DB Writer?
        Server.getPool(Server.Type_Writer_Pool).execute(new DatabaseWriter());
    }

    public void update() throws FileNotFoundException, IOException {
        try {
            Wmutex.acquire();
            writers += 1;
            if (writers == 1) {
                Rdb.acquire();
            }
            Wmutex.release();
            Wdb.acquire();

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
        } finally {
            try {
                Wdb.release();
                Wmutex.acquire();
                writers -= 1;
                if (writers == 0) {
                    Rdb.release();
                }
                Wmutex.release();
            } catch (InterruptedException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // lock functions?
    private String getFile(int x) {
        if (x < 0) {
            return "C:\\Users\\Avinoam\\Documents\\OS1\\OS1\\OS1\\database/file_" + ((x / fileSize) - 1) * fileSize;
        }
        return "C:\\Users\\Avinoam\\Documents\\OS1\\OS1\\OS1\\database/file_" + (x / fileSize) * fileSize;
    }

    private int getPosition(int x) {
        return Math.abs(x % (fileSize)) * readSize;
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

    @Override
    public void run() {

        try {
            Server.getDatabase().update();
        } catch (Exception e) {
            System.out.println("Error updating database");
        }
    }

}
