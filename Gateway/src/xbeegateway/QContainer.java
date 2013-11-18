/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xbeegateway;

/**
 *
 * @author A.R. Dzulqarnain
 * Container for sending data query from SQL table
 */
public class QContainer {
    private int[] vessel;
    private String address;
    public QContainer(String address, int[] vessel) {
        this.address=address;
        this.vessel=vessel;
    }
    
    public String getAddress(){
        return address;
    }
    
    public int[] getPacket(){
        return vessel;
    }
}
