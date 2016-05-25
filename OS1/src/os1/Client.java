/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Avinoam
 */
public class Client implements Runnable {

    private static int userNumberPool = 0;
    private static final ReentrantLock lock = new ReentrantLock(true);
    private int[] probability = new int[1000];
    private int R1, R2;
    String filename;
    private int userNum;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int num;
    private int intResponse;
    private boolean keepRunning = true;

    public Client(int R1, int R2, String filename) {
        this.R1 = R1;
        this.R2 = R2;
        this.filename = filename;
        String content;
        String[] contentArr;
        // lock?
        userNum = getNumber();

        try {
            content = new String(Files.readAllBytes(Paths.get(filename)));
            contentArr = content.split(",");
            int location = 0;
            int currNumber = Integer.parseInt(contentArr[0]);
//            if (currNumber != R1) {
//                keepRunning = false;
//                throw new Exception("R1 is not equal to the file first number");
//            }
            for (int i = 2; i < contentArr.length; i++) {
                int amount = (int) (Double.parseDouble(contentArr[i]) * 1000);
                for (int j = 0; j < amount; j++) {
                    probability[location++] = currNumber;
                }
                currNumber++;
            }
        } catch (Exception e) {
            System.out.println("Error reading file.");
            e.printStackTrace();
        }
    }

    private int getNumber() {
        lock.lock();
        try {
            return userNumberPool++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 45000);
            Streams stream = new Streams(socket);
            out = stream.getOut();
            in = stream.getIn();

            while (keepRunning) {
                num = probability[(int) (Math.random() * 1000)];
                System.out.println("User: " + userNum + ": sending " + num);

                out.writeObject(num);
                out.flush();

                intResponse = (int) in.readObject();
                System.out.println("User: " + userNum + ": got reply: " + intResponse + " for query " + num);
            }
        } catch (Exception e) {
            System.out.println("Client Socket Error.");
            e.printStackTrace();
        }
    }

}
