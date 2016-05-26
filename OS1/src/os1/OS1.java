/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.File;

/**
 *
 * @author Avinoam
 */
public class OS1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File folder = new File("C:\\Users\\Avinoam\\Documents\\OS1\\OS1\\OS1\\database");
        final File[] files = folder.listFiles();
        for (File f : files) {
            f.delete();
        }
        // Server args: S_Threads, Cache size, MinZ, randomRange, databaseReaders
        new Thread(new Server(10, 50, 5, 100, 10)).start();
        new Thread(new Client(-5, 4, "1.txt")).start();
        new Thread(new Client(-5, 4, "1.txt")).start();
        new Thread(new Client(-5, 4, "1.txt")).start();
        new Thread(new Client(-5, 4, "1.txt")).start();
        new Thread(new Client(-5, 4, "1.txt")).start();
   //     new Thread(new Client(-5, 4, "3.txt")).start();


    }

}
