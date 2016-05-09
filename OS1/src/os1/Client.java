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
    private int userNum;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private double num;
    private String strResponse;
    private double doubleResponse;
    private boolean keepRunning = true;

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 45000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            userNum = userNumberPool++;

            while (keepRunning) {
                num = Math.random() * 10;
                num = Math.floor(num * 100) / 100;
                System.out.println("User: " + userNum + ": sending " + num);
                out.println("" + num);
                out.flush();
                strResponse = in.readLine();
                doubleResponse = Math.floor(Double.parseDouble(strResponse) * 100) / 100;
                System.out.println("User: " + userNum + ": got reply: " + doubleResponse + " for query " + num);
            }
        } catch (Exception e) {
            System.out.println("Client Socket Error.");
        }
    }

}
