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
public class ClientListener implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientListener(Socket sock) {
        try {
            socket = sock;
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (Exception e) {
            System.out.println("ClientListener Socket Error.");
        }
    }

    @Override
    public void run() {
        String msg;
        try {
            while ((msg = in.readLine()) != null) {

                /* ask the cache thread if x is there
                   if not, 
                   ask a reader thread from a threadpool to check in db
                   else,
                   generate new one
                
                -- also need to update cache/db at a certain time
                
                 */
                out.println(Integer.parseInt(msg) + 1);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("ClientListener disconnection");
        }
    }

}
