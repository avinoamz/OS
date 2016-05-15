/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

import java.io.PrintWriter;
import java.util.HashMap;

/**
 *
 * @author Avinoam
 */
public class Cache implements Runnable {

    private int C, M;
    private static HashMap<Integer, Data> memory;

    public Cache(int C, int M) {
        this.C = C;
        this.M = M;
        memory = new HashMap<>(C);
    }

    @Override
    public void run() {
    }

}
