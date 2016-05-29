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
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Class that represents Socket's Input and Output Streams.
 */
public class Streams {

    private final ReentrantLock lock = new ReentrantLock(true);
    private PrintWriter out;
    private BufferedReader in;

    public Streams(Socket socket) {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Error creating Streams");
        }
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void send(int msg) {
        lock.lock();
        try {
            out.println(msg);
        } finally {
            lock.unlock();
        }
    }
    
    
}
