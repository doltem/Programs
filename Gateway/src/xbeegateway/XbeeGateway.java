/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xbeegateway;
//import java.util.Date;


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
        tesGateway();
    }
    
    public static void tesGateway() throws Exception {
        XbeeSR xbeedata=new XbeeSR ("COM11", 9600); //create connection to xbee com port
        //xbeedata.setDB(dburl,dbuser,dbpass); //connect xbee object to database
        SQLmod dbase=new SQLmod("jdbc:mysql://localhost:3306/indisbuilding_db","root",""); //create connection to MySQL 
        
        String addr="a"; int occ=0; double lux=0; double setpoint=0; int lamp=0; int mode=0; int zone=0;
        int i=0;

        while(true){
            xbeedata.parseResponse();
            if(xbeedata.isDataAvail()){
                System.out.println("Data masuk");
                zone=xbeedata.getData(0);
                addr=xbeedata.getRemoteAddr();
                lamp=xbeedata.getData(10);
                occ=xbeedata.getData(14);
                lux=xbeedata.getLight(18,19);
                setpoint=xbeedata.getLight(23,24);
                mode=xbeedata.getData(28);
                
                System.out.println("Sender Adress : "+addr);
                System.out.println("Operation Zone : "+zone);
                if(lamp==1){
                    System.out.println("1.Lamp Status : ON");
                }
                else{
                    System.out.println("1.Lamp Status : OFF");
                }
                if(occ==1){
                    System.out.println("2.Occupancy Status : Occupied");
                }
                else{
                    System.out.println("2.Occupancy Status : Unoccupied");
                }
                System.out.print("3.Lighting Level : "+lux);
                System.out.println(" lux");
                System.out.print("4.Lighting Level Setpoint : "+setpoint);
                System.out.println(" lux");
                if(mode==1){
                    System.out.println("5.Operation Mode : AUTO");
                }
                else{
                    System.out.println("5.Operation Mode : MANUAL");
                }
                //dbase.updateStatus(addr, zone, occ, lux, setpoint, lamp,mode);
            System.out.println("Iterasi ke-"+i+",");
            System.out.println("");
            i++;
            }
            //dbase.updateStatus(addr, zone, occ, lux, setpoint, lamp,mode);

        }
    }
}