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
import java.util.ArrayList;
import java.util.List;

public class SQLmod {
    String DB_URL;
    String USER;
    String PASS;
    String tableStat="device_stat_";
    String adrtable="deviceloc";
    String statustable="devicestat";
    String eventtable="event";
    
    public SQLmod(String DB_URL, String USER, String PASS){ //class constructor for setting url, user, password
        this.DB_URL=DB_URL;
        this.USER=USER;
        this.PASS=PASS;
    }
    
    public void event(String address, int zone, int occ) throws SQLException{
        Connection con= null;
        String status;
        try{
          if(occ==0x01){status="OCCUPIED";}else{status="UNOCCUPIED";}
          con = DriverManager.getConnection(DB_URL, USER, PASS);
          Statement stmt1 = con.createStatement();
          ResultSet loc= stmt1.executeQuery("SELECT grup , location FROM "+adrtable+" WHERE address = '"+address+"'");
          Statement stmt2 = con.createStatement();
          ResultSet rs = stmt2.executeQuery("SELECT occ FROM "+statustable+" WHERE address = '"+address+"' AND zone = "+zone+" "); 
          while(loc.next()){ 
            while(rs.next()){
                if(!rs.getString("occ").equals(status)){
                  String ins = "INSERT INTO "+eventtable+
                  " VALUES (default,default,'"+address+"',"+zone+",'"+loc.getString("location")+"','"+loc.getString("grup")+"','"+status+"')";
                  Statement stmt3 = con.createStatement();
                  stmt3.executeUpdate(ins);
                  System.out.println("Success in insert to "+eventtable);
                }
            }
          }
        }
        catch(SQLException e){
            System.out.println(e);
        }
        finally{
            if(con!=null){ //closing connection
            con.close();
            }  
        }
       
    }
    public void updateStatus(String address, int zone, int iocc, float lux, float setpoint, int ilamp, int imode ) throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        String mode; String occ; String lamp;
        if(imode==0x01){ mode="AUTO";} else{ mode="MANUAL";}
		if(iocc==0x01){ occ="OCCUPIED";} else{occ="UNOCCUPIED";}
		if(ilamp==0x01){ lamp="ON";} else{ lamp="OFF";}
        Connection con= null;
        try {
            con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT address FROM "+adrtable+" WHERE address = '"+address+"'");
            //searching address already available in table or not
            if(rs.next()==true){ //if yes, update value in selected row
                System.out.println("Adress Found : "+address);
                rs.beforeFirst();
                while(rs.next()){
                    Statement stmt1 = con.createStatement();
                    ResultSet stat = stmt1.executeQuery("SELECT zone FROM "+statustable+" WHERE address = '"+address+"'");
                    if(stat.next()==true){ //if yes, update value in selected row
                            System.out.println("Zone Found : zone "+zone);
                            stat.beforeFirst();
                            while(stat.next()){
                                    String upd = "UPDATE "+statustable+" SET address = '"+address+"' , mode = '"+mode+"' , occ = '"+occ+"' , lux = "+lux+" , setpoint = "+setpoint+" , lamp = '"+lamp+"' WHERE zone = "+zone+" ";
                                    Statement stmt2 = con.createStatement();
                                    stmt2.executeUpdate(upd);
                                    System.out.println("Success in update "+zone+" in "+address+", ");
                            }
                    }
                    else{ // if not insert new row
                            String upd = "INSERT INTO "+statustable+
                            " VALUES (default,"+zone+",'"+address+"','"+mode+"','"+occ+"',"+lux+","+setpoint+",'"+lamp+"',default)";
                            Statement stmt2 = con.createStatement();
                            stmt2.executeUpdate(upd);
                            System.out.println("Success in insert to"+statustable+" in "+address+", ");
                    }
                }
            }
            else{ // if not insert new row
                String ins = "INSERT INTO "+adrtable+
                        " VALUES ('"+address+"','"+address+"',default)";
                Statement stmt1 = con.createStatement();
                stmt1.executeUpdate(ins);
                System.out.println("Success in insert to"+adrtable);

                String upd = "INSERT INTO "+statustable+
                            " VALUES (default,"+zone+",'"+address+"','"+mode+"','"+occ+"',"+lux+","+setpoint+",'"+lamp+"',default)";
                Statement stmt2 = con.createStatement();
                stmt2.executeUpdate(upd);
                System.out.println("Success in insert to"+statustable+" in "+address+", ");
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
        finally{
            if(con!=null){ //closing connection
            con.close();
            }  
        }
    }
    
    public QContainer[] identifyWrite() throws SQLException, ClassNotFoundException{ //method for identify which device in manual mode
        List<QContainer> xdata= new ArrayList<>();
        Connection con= null;
        try {
            Class.forName("com.mysql.jdbc.Driver"); 
            con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = con.createStatement();
            //searching device in write mode
            ResultSet wq = stmt.executeQuery("SELECT adress, zone, mode, setpoint, lamp FROM "+statustable+" WHERE stat = 'W'"); //create query
            while(wq.next()){
                xdata.add(new QContainer(wq.getString("address"),wq.getString("mode"),wq.getInt("zone"),wq.getFloat("setpoint"),wq.getString("lamp"))); //save query data to new variable type : QContainer
            //turn write status into read
            }
            String update="UPDATE "+statustable+" SET stat = 'R' "+"WHERE stat = 'W'"; //set row mode back to read
            Statement stmt1 = con.createStatement();
            stmt1.executeUpdate(update);
	}
        catch(SQLException e){
            System.out.println(e);
        }
        finally{
            if(con!=null){
            con.close();
            }  
        }
        QContainer[] valAdd =new QContainer[xdata.size()];
        valAdd = xdata.toArray(valAdd);
        return valAdd; //return device addresses in which in manual mode
    } //method for identify which device commanded to be manual
}
