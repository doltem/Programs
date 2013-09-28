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
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;
import java.math.*;
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
    
    //variable for creating database
    String url; String user; String pass;
    //variable
    String gatePort;
    int baudRate;
    boolean avail=false;
    int[] data=new int[20];
    String remoteAddr;
    int[] intAddr;
    public XbeeSR(String sPort, int baud){
        gatePort=sPort;
        baudRate=baud;
    }
    
    public void parseResponse() throws Exception{ //method for parsing incoming Xbee data
        XBee xbee = new XBee();
        try {
            xbee.open(gatePort, baudRate);
            XBeeResponse response = xbee.getResponse();
            if (response.getApiId() == ApiId.ZNET_RX_RESPONSE){
                ZNetRxResponse rx = (ZNetRxResponse) response;
                remoteAddr=ByteUtils.toBase16(rx.getRemoteAddress64().getAddress());
                intAddr=rx.getRemoteAddress64().getAddress();
                data=rx.getData();
                avail=true;
            }   
        }
        catch (Exception e){
            
        }
        finally{
            if(xbee.isConnected()){
                xbee.close();
            }
        }
    }
   
    public int getData(int pos){
        return data[pos];
    }
    
    public int getData16(int pos1, int pos2){ //method for getting16bit data from parsed packet
        Bitplay bitplay=new Bitplay();
        int value=bitplay.joinBit(getData(pos1),getData(pos2),16);
        return value;
    }
    
    public int getLight(int pos1, int pos2){ //method for getting16bit data from parsed packet
        Bitplay bitplay=new Bitplay();
        int value=bitplay.joinBit(getData(pos1),getData(pos2),16);
        value=10^((value-1)/10000);
        return value;
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
    
    public void setDB(String url, String user, String pass){
        this.url=url;
        this.user=user;
        this.pass=pass;
    }
    
    public void sendDataLight()throws Exception{
        XBee xbee = new XBee();
	QContainer[] qdata=null;
        SQLmod dbase=new SQLmod(url,user,pass);
        //get data of adress with write status from database
        qdata=dbase.identifyWrite();
        try {
            xbee.open(gatePort, baudRate);
            for(int i=0;i<qdata.length;i++){
                //olah data setpoint jadi LSB dan MSB
                Bitplay olah=new Bitplay();
                double LightSet=10000*Math.log10(qdata[i].setpoint);
                int LightSetMSB=olah.getMSB((int)LightSet, 16);
                int LightSetLSB=olah.getLSB((int)LightSet, 16);
                //target address
                XBeeAddress64 addr64 = new XBeeAddress64(qdata[i].address);
                // create an array of arbitrary data to send
                int[] payload = new int[] { EndGateway, qdata[i].zone, MCustomCluster, LCustomCluster, FCClient2Server, Trans, CWriteAttribute, 0x00,LMode, DT8Bitmap, qdata[i].mode, 0x00, LLamp, DT8Bitmap, qdata[i].lamp, 0x00, LSetPoint, DT16Uint, LightSetMSB, LightSetLSB};
                // first request we just send 64-bit address.  we get 16-bit network address with status response
                ZNetTxRequest request = new ZNetTxRequest(addr64, payload);
                try {
                    ZNetTxStatusResponse response = (ZNetTxStatusResponse) xbee.sendSynchronous(request, 10000);
                    // update frame id for next request
                    request.setFrameId(xbee.getNextFrameId());
                    if (response.getDeliveryStatus() == ZNetTxStatusResponse.DeliveryStatus.SUCCESS) {
                       System.out.println("Packet delivery success");
                    } else {
                       System.out.println("Packet delivery failed");
                    }				
                } 
                catch (XBeeTimeoutException e) {
                    System.out.println(e);
                }
            }
        }
        catch (Exception e){
           System.out.println(e);
        }
        finally{
            if(xbee.isConnected()){
                xbee.close();
            }
        }
    }

}