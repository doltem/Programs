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
                
                System.out.println("Alamat Pengirim : "+addr);
                System.out.println("Zona Operasi : "+zone);
                System.out.println("1.Status Lampu : "+lamp);
                System.out.println("2.Status Okupansi : "+occ);
                System.out.println("3.Tingkat Pencahayaan : "+lux);
                System.out.println("4.Setpoint Pencahayaan : "+setpoint);
                System.out.println("5.Mode Operasi : "+mode);
                dbase.updateStatus(addr, zone, occ, lux, setpoint, lamp,mode);
            System.out.println("Iterasi ke-"+i+",");
            System.out.println("");
            i++;
            }
            //dbase.updateStatus(addr, zone, occ, lux, setpoint, lamp,mode);

        }
    }
}