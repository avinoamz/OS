/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.net.Socket;

/**
 *
 * @author Avinoam
 */
public class S_Thread implements Runnable {

    private Socket socket;
    private int msg;

    public S_Thread(Socket socket, int msg) {
        this.socket = socket;
        this.msg = msg;
    }

    @Override
    public void run() {

    }

}
