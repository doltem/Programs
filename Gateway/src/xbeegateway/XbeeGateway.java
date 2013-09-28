/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xbeegateway;


/**
 *
 * @author A.R. Dzulqarnain
 * need to be developed in this coding :
 * 
 */
public class XbeeGateway {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        updateValueTes();
    }
    
    public static void tesGatewat() throws Exception {
              String dburl="jdbc:mysql://localhost:3306/otomasi"; String dbuser="root" ; String dbpass="";
        XbeeSR xbeedata=new XbeeSR ("COM9", 9600); //create connection to xbee com port
        xbeedata.setDB(dburl,dbuser,dbpass); //connect xbee object to database
        SQLmod dbase=new SQLmod(dburl,dbuser,dbpass); //create connection to MySQL 
        
        String addr; int occ; float lux; float setpoint; int lamp; int mode;
        
        while(true){
            xbeedata.parseResponse();
            if(xbeedata.isDataAvail()){
                xbeedata.parseResponse();
                addr=xbeedata.getRemoteAddr();
                lamp=xbeedata.getData(10);
                occ=xbeedata.getData(14);
                lux=xbeedata.getLight(18,19);
                setpoint=xbeedata.getLight(23,24);
                mode=xbeedata.getData(8);
                //dbase.updateLight(addr, occ, lux, setpoint, lamp,mode);
            }
            xbeedata.sendDataLight();
        }
    }
    
    public static void receiveTes() throws Exception{
        //Xbee Receive Test
        XbeeSR xbeedata=new XbeeSR ("COM9", 9600);
        while(true){
            xbeedata.parseResponse();
            System.out.println("Adress of Sender:" +xbeedata.getRemoteAddr());
            for(int i=0; i<22; i++){
                System.out.format("%02X",xbeedata.getData(i));
                System.out.print(" ");
            }
            System.out.println("Value of Occ Sensor:" +xbeedata.getData(14));
            System.out.println("Value of Light Sensor:" +xbeedata.getData16(18,19));
            System.out.println("Value of Light Setpoint:" +xbeedata.getData16(23,24));
            System.out.println("Value of Lamp:" +xbeedata.getData(10));
        }
    }
    
    public static void updateValueTes() throws Exception{
        //SQL Test
        QContainer[] a=null;
        SQLmod dbase=new SQLmod("jdbc:mysql://localhost:3306/otomasi","root","");
        dbase.updateStatus("192 168 0 1",1,0,200,100,0,1); 
        dbase.updateStatus("192 168 0 2",2,0,200,100,0,0);
        dbase.event("192 168 0 2",2,1);
        dbase.updateStatus("192 168 0 2",2,1,200,100,1,1);
    }

    public static void tesBitPlay(){
        Bitplay tes=new Bitplay();
        int a=65000;
        int lsb=tes.getLSB(a, 16);
        int msb=tes.getMSB(a,16);
        System.out.println("Number Value is : "+a);
        System.out.println("MSB Value is : "+msb);      
        System.out.println("LSB Value is : "+lsb);
        System.out.println("Joined Value is : "+tes.joinBit(msb, lsb, 16));
    }
}