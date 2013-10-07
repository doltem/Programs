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
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.api.XBeeException;
/**
 *
 * @author A.R. Dzulqarnain
 */
public class XbeeSR {
    //Xbee Class declaration
    Bitplay bitplay=new Bitplay();
    XBee xbee = new XBee();
    XBeeResponse response = null;
    ZNetRxResponse rx=null;

    boolean avail=false;
    int[] data=new int[30];
    String remoteAddr;
    int[] intAddr=null;
    
    public XbeeSR(String sPort, int baud) throws XBeeException{
        String gatePort=sPort;
        int baudRate=baud;
        try {
            xbee.open(gatePort, baudRate);   
        }
        catch (XBeeException e){
            //System.out.println(e);
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
                avail=true;
            }
        }
        catch (XBeeException | ClassCastException e){
            System.out.println(e);
        }
    }
   
    public int getData(int pos){
        return data[pos];
    }
    
    public int getData16(int pos1, int pos2){ //method for getting16bit data from parsed packet
        int value=bitplay.joinBit(getData(pos1),getData(pos2),16);
        return value;
    }
    
    public double getLight(int pos1, int pos2){ //method for getting16bit data from parsed packet
        double value=getData16(pos1,pos2);
        value= Math.pow(10,((value-1)/10000));
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

}