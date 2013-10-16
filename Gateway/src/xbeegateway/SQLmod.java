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
    String tableStat="device_stat_";
    String adrtable="deviceloc";
    String statustable="devicestat";
    String eventtable="event";
    
    public SQLmod(String DB_URL, String USER, String PASS) throws SQLException{ //class constructor for setting url, user, password
        this.DB_URL=DB_URL;
        this.USER=USER;
        this.PASS=PASS;
    }
    
    public void event(String address, int zone, int occ) throws SQLException{
        String status;
        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt1=con.createStatement();Statement stmt2=con.createStatement()){
          if(occ==0x01){status="OCCUPIED";}else{status="UNOCCUPIED";}
          ResultSet stat= stmt1.executeQuery("SELECT grup , location FROM "+adrtable+" WHERE address = '"+address+"'");
          ResultSet rs = stmt2.executeQuery("SELECT occ FROM "+statustable+" WHERE address = '"+address+"' AND zone = "+zone+" "); 
          while(stat.next()){ 
            System.out.println("Address Found");
            while(rs.next()){
                System.out.println("Zone Found");
                if(!rs.getString("occ").equals(status)){
                  String ins = "INSERT INTO "+eventtable+
                  " VALUES (default,default,'"+address+"',"+zone+",'"+stat.getString("location")+"','"+stat.getString("grup")+"','"+status+"')";
                  con.createStatement().executeUpdate(ins);
                  System.out.println("Success in insert to "+eventtable);
                }
            }
          }
        }
        catch(SQLException e){
            System.out.println(e);
        }  
    }
    
    public void updateStatus(String address, int zone, int iocc, double lux, double setpoint, int ilamp, int imode ) throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        //Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        String mode=null; String occ=null; String lamp=null;
        String upd=null;
        if(imode==0x01){ mode="AUTO";} else{ mode="MANUAL";}
		if(iocc==0x01){ occ="OCCUPIED";} else{occ="UNOCCUPIED";}
		if(ilamp==0x01){ lamp="ON";} else{ lamp="OFF";}
        
        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS)) {           
            //searching address already available in table or not
            try(Statement stmt1=con.createStatement();Statement insupd=con.createStatement()){
                ResultSet rs = stmt1.executeQuery("SELECT address FROM "+adrtable+" WHERE address = '"+address+"'");
                if(rs.next()==true){ //if "address found", update value in selected row
                    //System.out.println("Adress Found : "+address);
                    rs.beforeFirst();
                    while(rs.next()){
                       try (Statement stmt2 = con.createStatement()){
                           //searching zone already in table or not
                           ResultSet stat = stmt2.executeQuery("SELECT zone FROM "+statustable+" WHERE address = '"+address+"'");
                           if(stat.next()==true){ //if "zone found", update value in selected row
                                    //System.out.println("Zone Found : zone "+zone);
                                    stat.beforeFirst();
                                    while(stat.next()){
                                            upd = "UPDATE "+statustable+" SET address = '"+address+"' , mode = '"+mode+"' , occ = '"+occ+"' , lux = "+lux+" , setpoint = "+setpoint+" , lamp = '"+lamp+"' WHERE zone = "+zone+" ";
                                            insupd.executeUpdate(upd);
                                            System.out.println("Berhasil memasukkan data ke database dari zona "+zone+" di alamat "+address+", ");
                                    }
                            }
                            else{ // if "zone not found" not insert new row
                                    upd = "INSERT INTO "+statustable+
                                    " VALUES (default,"+zone+",'"+address+"','"+mode+"','"+occ+"',"+lux+","+setpoint+",'"+lamp+"',default)";
                                    insupd.executeUpdate(upd);
                                    System.out.println("Success in insert to"+statustable+" in "+address+", ");
                            }
                       }
                       catch(SQLException e){
                           System.out.println(e);
                       }
                    }
                }
                else{ // if "address not found" insert new row
                    upd = "INSERT INTO "+adrtable+
                            " VALUES ('"+address+"','"+address+"',default)";
                    insupd.executeUpdate(upd);
                    System.out.println("Success in insert to"+adrtable);

                    upd = "INSERT INTO "+statustable+
                                " VALUES (default,"+zone+",'"+address+"','"+mode+"','"+occ+"',"+lux+","+setpoint+",'"+lamp+"',default)";
                    insupd.executeUpdate(upd);
                    System.out.println("Success in insert to"+statustable+" in "+address+", ");
                }
            }
            catch(SQLException e){
                System.out.println(e);
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
    }
}
