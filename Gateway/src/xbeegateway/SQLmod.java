/*
 * This is Class for help Xbee data to communicate with MySQL database, using JDBC
 * things need to be developed:
 * 1. update for energy metering device
 * 2. reading the whole device table
 */
package xbeegateway;

/**
 *
 * @author A.R. Dzulqarnain
 */

import java.sql.*;

public class SQLmod {
    String DB_URL;
    String USER;
    String PASS;
    String statustable="bld_device_sensing";
    
    public SQLmod(String DB_URL, String USER, String PASS) throws SQLException{ //class constructor for setting url, user, password
        this.DB_URL=DB_URL;
        this.USER=USER;
        this.PASS=PASS;
    }
    

    
    public void updateStatus(String address, int zone, int iocc, double lux, double setpoint, int ilamp, int imode ) throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        //Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        String mode=null; String occ=null; String lamp=null;
        String upd=null;
        
        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);Statement insupd=con.createStatement()) {           
            //searching address already available in table or not
                upd = "UPDATE "+statustable+
                        " SET time_sensing = default , value = "+lux+" WHERE attached_sensor_id = '00 13 a2 00 40 8b 5e 9f-01'";
                insupd.executeUpdate(upd);

                upd = "UPDATE "+statustable+
                        " SET time_sensing = default ,value = "+iocc+" WHERE attached_sensor_id = '00 13 a2 00 40 8b 5e 9f-02'";
                insupd.executeUpdate(upd);
                
                upd = "UPDATE "+statustable+
                        " SET time_sensing = default ,value = "+ilamp+" WHERE attached_sensor_id = '00 13 a2 00 40 8b 5e 9f-03'";
                insupd.executeUpdate(upd);
                System.out.println("Success in update"+statustable);              
        }
        catch(SQLException e){
            System.out.println(e);
        }
    }
}
    
   
