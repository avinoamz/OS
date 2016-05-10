/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Avinoam
 */
public class Client implements Runnable {

    private static int userNumberPool = 0;

    private int R1, R2;
    String filename;
    private int userNum;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int num;
    private String strResponse;
    private int intResponse;
    private boolean keepRunning = true;

    public Client(int R1, int R2, String filename) {
        this.R1 = R1;
        this.R2 = R2;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 45000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            userNum = userNumberPool++;

            while (keepRunning) {
                num = (int) (Math.random() * 100);
                System.out.println("User: " + userNum + ": sending " + num);
                out.println("" + num);
                out.flush();
                strResponse = in.readLine();
                intResponse = Integer.parseInt(strResponse);
                System.out.println("User: " + userNum + ": got reply: " + intResponse + " for query " + num);
            }
        } catch (Exception e) {
            System.out.println("Client Socket Error.");
            e.printStackTrace();
        }
    }

}
