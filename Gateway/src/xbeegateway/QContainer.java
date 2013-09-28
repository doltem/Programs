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
    public String address;
    public byte mode;
    public float setpoint;
    public byte lamp;
    public byte zone;
    

    public QContainer(String address, String mode, int zone, float setpoint, String lamp) {
        this.address=address;
        this.zone=(byte) zone;
        if(mode.equals("AUTO")){this.mode=0x01;}else{this.mode=0x00;}
        this.setpoint=setpoint;
        if(lamp.equals("ON")){this.lamp=0x01;}else{this.lamp=0x00;}
    }
}
