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
        new Thread(new Server(5, 10, 5, 100, 5)).start();
        new Thread(new Client(1, 10, "DB1")).start();
        try {
            Thread.sleep(300);
        } catch (Exception e) {
        }
        System.exit(0);
    }

}
