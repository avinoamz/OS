/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os1;

/**
 *
 * @author Avinoam
 */
public class OS1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Thread(new Server()).start();
        new Thread(new Client()).start();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        System.exit(0);
    }

}
