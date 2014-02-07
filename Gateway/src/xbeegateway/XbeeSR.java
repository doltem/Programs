/*This is Class for manipulating Xbee data
 * need to be developed in this coding :
 * -creating XBEE packet
 *      parameter :
 *      adress
 *      id,data (better placed in 2D array)
 * -sending XBEE packet
 * -evaluating getAdress, since method using 64bit adress #solved, change adress to String
 * -evaluating exception throw,catch
 */
package xbeegateway;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.ErrorResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.api.XBeeException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.*;
import java.sql.*;
/**
 *
 * @author A.R. Dzulqarnain
 */
public class XbeeSR {
    //constants
    private static final short EndGateway = 0x80;
    private static final short MCustomCluster = 0x10;
    private static final short LCustomCluster = 0x00;
    private static final short FCClient2Server = 0x48;
    private static final short Trans = 0x00;
    private static final short CWriteAttribute = 0x02;
    private static final short DT8Bitmap = 0x18;
    private static final short DT16Uint = 0x21;
    private static final short LMode = 0x04;
    private static final short LLamp = 0x01;
    private static final short LSetPoint = 0x03;
    
    private static final short ZONE = 0X01;
    private static final short MODE = 0X02;
    private static final short LAMP = 0X03;
    private static final short OCC = 0X04;
    private static final short LIGHT = 0X05;
    private static final short SPOINT = 0X06;
    private static final short EBAND = 0X07;
    
    //Xbee Class declaration
    Bitplay bitplay=new Bitplay();
    XBee xbee = new XBee();
    XBeeResponse response = null;
    ZNetRxResponse rx=null;
    ZNetTxStatusResponse tx=null;
    
    
    
    //data structure for sending to remote xbee
    QContainer[] qdata=null;
    
    BufferedWriter bw;
    
    //variable
    String gatePort;
    int baudRate;
    boolean avail=false;
    int[] data=new int[30];
    String remoteAddr;
    int[] intAddr;
    
    public XbeeSR(String sPort, int baud) throws XBeeException{
        gatePort=sPort;
        baudRate=baud;
        try {
            xbee.open(gatePort, baudRate);   
        }
        catch (XBeeException e){
            //System.out.println(e);
        }
        finally{
            if(xbee.isConnected()){
                //xbee.close();
            }
        }
    }
    
    public void parseResponse() throws XBeeException, ClassCastException{ //method for parsing incoming Xbee data
        try{
            response = xbee.getResponse();
            if (response.getApiId() == ApiId.ZNET_RX_RESPONSE){
                rx = (ZNetRxResponse) response;
                remoteAddr=ByteUtils.toBase16(rx.getRemoteAddress64().getAddress());
                intAddr=rx.getRemoteAddress64().getAddress();
                data=rx.getData();
                /*for(int i=0;i<data.length;i++){
                    System.out.print(data[i]);
                    System.out.print(" ");
                }
                System.out.println();*/
                
                avail=true;
            }
        }
        catch (XBeeException | ClassCastException e){
            System.out.println(e);
        }
        finally{
            if(xbee.isConnected()){
                //xbee.close();
            }
        }
    }
    
    
    public double getLight(int type){ //method for getting16bit data from parsed packet
        double val=0;
        switch(type){
            case LIGHT:{
                val= bitplay.joinBit(data[18],data[19],16);
                val= Math.pow(10,((val-1)/10000));
            }
            break;
                
            case SPOINT:{
                val= bitplay.joinBit(data[23],data[24],16);
                val= Math.pow(10,((val-1)/10000));
            }break;
            
            case EBAND:{
                val= bitplay.joinBit(data[28],data[29],16);
                val= Math.pow(10,((val-1)/10000));
            }break;
        }
        return val;
    }
    
    public int[] getFullData(){
        return data;
    }
    
    public int getRawData(int index){
        return data[index];
    }
    
    public int getData(int type){
        int val=0;
        switch(type){
            case ZONE:{
                val= data[0];
            }
            break;
            
            case MODE:{
                val= data[33];
            }
            break;
            
            case LAMP:{
               val= data[10];
            }
            break;
                
            case OCC:{
                val= data[14];
            }
            break;
        }
        return val;
    }
    
    public String getRemoteAddr(){ //method for getting sender Adress from parsed packets
        String vessel="";
        for(int i=0; i<8; i++){
            if(intAddr[i]==0){
                vessel=vessel+Integer.toHexString(intAddr[i])+"0 ";
            }
            else if(i!=7){
                vessel=vessel+Integer.toHexString(intAddr[i])+" ";
            }
            else{
               vessel=vessel+Integer.toHexString(intAddr[i]);
            }
        }
        return vessel;
    }
    
    public boolean isDataAvail(){ //method for evaluate whether xbee data is succesfully parsed or not
        boolean stat=avail;
        avail=false;
        return stat;
    }
    
    public void sendPacket(QContainer box) throws XBeeException{
        XBeeAddress64 addr64=new XBeeAddress64(box.getAddress());
        int[] payload=box.getPacket();
            System.out.println(box.getAddress());
            for(int i=0;i<payload.length;i++){
                System.out.print(Integer.toHexString(payload[i]));
                System.out.print(" ");
            }
        ZNetTxRequest request=new ZNetTxRequest(addr64, payload);
        try {
            tx = (ZNetTxStatusResponse) xbee.sendSynchronous(request, 10000);
            // update frame id for next request
            request.setFrameId(xbee.getNextFrameId());
            if (tx.getDeliveryStatus() == ZNetTxStatusResponse.DeliveryStatus.SUCCESS) {
               System.out.println("Packet delivery success");
            } else {
               System.out.println("Packet delivery failed");
            }
        }
        catch (XBeeTimeoutException e) {
                System.out.println(e);
        }
     }
    
    
    public void delayTest( int timeoutdelay) throws XBeeException, IOException, InterruptedException{
	XBeeAddress64 addr64=new XBeeAddress64("00 13 a2 00 40 8b 5e 9f");
        int[] payload={0x80,0x01,0x10,0x00,0x48,0x00,0x02,0x00,0x04,0x18,0x00,0x00,0x01,0x18,0x01,0x00,0x03,0x21,0x00,0x00,0x00,0x03,0x21,0x1b,0x4c};
	long delay=0;
        long start = 0;
        long end =0;
        boolean notdelivered=true;
        
        short counter=0;
        
        ZNetTxRequest request=new ZNetTxRequest(addr64, payload);
        int index=0;
        boolean noretries=true;
        while(index<=200){
            if(notdelivered){
                try {
                    if(noretries){
                        System.out.print("Ready to delivered in...5,");
                        Thread.sleep(1000);
                        System.out.print("4, ");
                        Thread.sleep(1000);
                        System.out.print("3, ");
                        Thread.sleep(1000);
                        System.out.print("2, ");
                        Thread.sleep(1000);
                        System.out.print("1, ");
                        Thread.sleep(1000);
                    }
                    tx = (ZNetTxStatusResponse) xbee.sendSynchronous(request, timeoutdelay);
                    if(noretries){
                        start=System.currentTimeMillis();
                    }
                    //System.out.println("Start : "+start);
                    // update frame id for next request
                    request.setFrameId(xbee.getNextFrameId());
                    if (tx.getDeliveryStatus() == ZNetTxStatusResponse.DeliveryStatus.SUCCESS) {
                       System.out.println("Packet delivery success");
                       //break;
                        notdelivered=false;
                        noretries=true;
                    } else {
                       System.out.println("Packet delivery failed");
                       noretries=false;
                    }
                }
                catch (XBeeTimeoutException e) {
                        System.out.println(e);
                        int i=5;
                        System.out.println("device : "+i);
                }
            }
            //}
            this.parseResponse();
            if (this.isDataAvail() && noretries){
                //get delay
                end=System.currentTimeMillis();
                delay=end-start;
                //get data
                /*for(int i=0;i<this.data.length;i++){
                    System.out.print(Integer.toHexString(data[i]));
                    System.out.print(" ");
                }*/
                //System.out.println();
                System.out.println(delay);
                Thread.sleep(2000);
                ++index;
                notdelivered=true;

                //break because data received
            }
        }
        System.out.println("completed");
     }

}