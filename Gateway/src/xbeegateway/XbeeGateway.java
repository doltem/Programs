/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xbeegateway;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.Date;


/**
 *
 * @author A.R. Dzulqarnain
 * need to be developed in this coding :
 * 
 */
public class XbeeGateway {
    //for testing sending data : "UPDATE `devicestat` SET `mode`='MANUAL', `setpoint`='500',`lamp`='ON',`stat`='W' WHERE 1"
    /**
     * @param args the command line arguments
     */
    
    private static final short ZONE = 0X01;
    private static final short MODE = 0X02;
    private static final short LAMP = 0X03;
    private static final short OCC = 0X04;
    private static final short LIGHT = 0X05;
    private static final short SPOINT = 0X06;
    private static final short EBAND = 0X07;
    
    public static void main(String[] args) throws Exception {
        //tesSchedule();
        //relayStressTest();
        //tesEvent();
        //tesDelay();
        tesGateway();
        //tesCRC();
    }
     
    public static void tesEvent() throws Exception {
        while(true){
        String dburl="jdbc:mysql://localhost:3306/otomasi"; String dbuser="root" ; String dbpass="";
        SQLmod dbase=new SQLmod(dburl,dbuser,dbpass);
        dbase.event("00 13 a2 00 40 8b 5e 9f",1,0);
        }
    }
    
    public static void tesSchedule() throws Exception {
        String dburl="jdbc:mysql://localhost:3306/otomasi"; String dbuser="root" ; String dbpass="";
        SQLmod dbase=new SQLmod(dburl,dbuser,dbpass);
        dbase.getSchedule();
    }
    
    public static void tesGateway() throws Exception {
        String dburl="jdbc:mysql://localhost:3306/otomasi"; String dbuser="root" ; String dbpass="";
        XbeeSR xbeedata=new XbeeSR ("COM11", 9600); //create connection to xbee com port
        //xbeedata.setDB(dburl,dbuser,dbpass); //connect xbee object to database
        SQLmod dbase=new SQLmod(dburl,dbuser,dbpass); //create connection to MySQL 
        
        String addr="a"; int occ=0; double lux=0; double setpoint=0; double eband=0; int lamp=0; int mode=0; int zone=0;
        int i=0;
        int[] payload;

        while(true){
            System.out.println("loop");
            xbeedata.parseResponse();
            if(xbeedata.isDataAvail()){
                System.out.println("Data masuk");
                zone=xbeedata.getData(ZONE);
                addr=xbeedata.getRemoteAddr();
                lamp=xbeedata.getData(LAMP);
                occ=xbeedata.getData(OCC);
                lux=xbeedata.getLight(LIGHT);
                setpoint=xbeedata.getLight(SPOINT);
                eband=xbeedata.getLight(EBAND);
                mode=xbeedata.getData(MODE);
                System.out.print("Payload : ");
                payload=xbeedata.getFullData();
                for(int c=0;c<payload.length;++c){
                    System.out.print(Integer.toHexString(payload[c])+" ");
                }
                System.out.println("Alamat Pengirim : "+addr);
                System.out.println("Zona Operasi : "+zone);
                System.out.println("1.Status Lampu : "+lamp);
                System.out.println("2.Status Okupansi : "+occ);
                System.out.println("3.Tingkat Pencahayaan : "+lux+" lux");
                System.out.println("4.Setpoint Pencahayaan : "+setpoint+" lux");
                System.out.println("5.Error Band Pencahayaan : "+eband+" lux");
                System.out.println("5.Mode Operasi : "+mode);
                dbase.updateStatus(addr, zone, occ, lux, setpoint, eband, lamp,mode);
            System.out.println("Iterasi ke-"+i+",");
            System.out.println("");
            i++;
            }
            while(dbase.checkCommand()){
                System.out.println("Command Query Found");
                xbeedata.sendPacket(dbase.getCommand());
            }
            //dbase.updateStatus(addr, zone, occ, lux, setpoint, lamp,mode);

        }
    }
    
    public static void relayStressTest() throws Exception {
        XbeeSR xbeedata=new XbeeSR ("COM11", 9600); //create connection to xbee com port
        Bitplay bitplay=new Bitplay();
        
        int counter=0; int fail=0; int treshold=0; int lux=0;
        int i=0;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        

        while(true){
            xbeedata.parseResponse();
            if(xbeedata.isDataAvail()){
                date=new Date();
                System.out.print("Data in at : ");
                System.out.println(dateFormat.format(date));
                if(xbeedata.getRawData(0)==2){
                    lux=bitplay.joinBit(xbeedata.getRawData(1),xbeedata.getRawData(2),16);

                    System.out.println("Device is Active");
                    System.out.println("Lux : "+lux);
                    System.out.println("");
                }
                else if(xbeedata.getRawData(0)==3){
                    System.out.println("Device is Up & Running");
                    System.out.println("");
                }
                else{
                    counter+=xbeedata.getRawData(0);
               
                    if(xbeedata.getRawData(3)>=1){
                        System.out.println("Lamp is failed");
                    }
                    fail+=xbeedata.getRawData(3);
                    lux=bitplay.joinBit(xbeedata.getRawData(1),xbeedata.getRawData(2),16);

                    System.out.println("Counter : "+counter);
                    System.out.println("Lux : "+lux);
                    System.out.println("Fail attempt : "+fail);
                    System.out.println("Data ke-"+i+",");
                    System.out.println("");
                    i++;
                }
            }
        }
    }

    public static void tesDelay() throws Exception {
        XbeeSR xbeedata=new XbeeSR ("COM11", 9600); 
        xbeedata.delayTest(5000);
    }
    
    public static void tesCRC() throws Exception{
        CRC crc = new CRC();
        XbeeSR xbeedata=new XbeeSR ("COM11", 9600); //create connection to xbee com port
        int i=0;
        int[] payload;
        int success=0;
        int fail=0;

        while(true){
            xbeedata.parseResponse();
            if(xbeedata.isDataAvail()){
                System.out.print("Payload : ");
                payload=xbeedata.getFullData();
                for(int c=0;c<payload.length;++c){
                    System.out.print(Integer.toHexString(payload[c])+" ");
                }
                
                int crcvalue=crc.getCRC(payload, payload.length);
                if(crcvalue==0){
                    ++success;
                }
                else{
                    ++fail;
                }
            }
            System.out.println();
            System.out.println("Good Data : "+success);
            System.out.println("Bad Data : "+fail);
            System.out.println();

        }
    }
    
    
}