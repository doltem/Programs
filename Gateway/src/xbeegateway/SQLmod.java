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
import java.util.Calendar;

public class SQLmod {
    private static final int EndGateway = 0x80;
    private static final int MCustomCluster = 0x10;
    private static final int LCustomCluster = 0x00;
    private static final int FCClient2Server = 0x48;
    private static final int Trans = 0x00;
    private static final int CWriteAttribute = 0x02;
    private static final int DT8Bitmap = 0x18;
    private static final int DT16Uint = 0x21;
    private static final int LMode = 0x04;
    private static final int LLamp = 0x01;
    private static final int LSetPoint = 0x03;
    Connection con;
    
    String DB_URL;
    String USER;
    String PASS;
    String tableStat="device_stat_";
    String adrtable="devicelist";
    String statustable="devicestat";
    String eventtable="event";
    String commandtable="command";
    String schedtable="schedule";
    
    public SQLmod(String DB_URL, String USER, String PASS) throws SQLException{ //class constructor for setting url, user, password
        this.DB_URL=DB_URL;
        this.USER=USER;
        this.PASS=PASS;
        con = DriverManager.getConnection(DB_URL, USER, PASS);
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
    
    public void updateStatus(String address, int zone, int occ, double lux, double setpoint, double eband, int lamp, int mode ) throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        //Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        String upd=null;
        String alias=null;
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
                                    System.out.print("nilai lampu: ");
                                    System.out.println(lamp);
                                    while(stat.next()){
                                            upd = "UPDATE "+statustable+" SET address = '"+address+"' , mode = '"+mode+"' , occ = "+occ+" , lux = "+lux+" , setpoint = "+setpoint+" , lamp = "+lamp+", errorband="+eband+" WHERE zone = "+zone+" ";
                                            insupd.executeUpdate(upd);
                                            System.out.println("Berhasil memasukkan data ke database dari zona "+zone+" di alamat "+address+", ");
                                    }
                            }
                            else{ // if "zone not found" not insert new row
                                    alias = "Zona"+zone;
                                    upd = "INSERT INTO "+statustable+
                                    " VALUES (default,"+zone+",'"+alias+"','"+address+"',"+mode+","+occ+","+lux+","+setpoint+","+eband+","+lamp+")";
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
                                " VALUES (default,"+zone+",'"+address+"',"+mode+","+occ+","+lux+","+setpoint+","+eband+","+lamp+")";
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
    
    public boolean checkCommand() throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        System.out.println("tes");
        boolean val=false;
        try (Statement allfind=con.createStatement()) {
            ResultSet allset = allfind.executeQuery("SELECT zone, address, id FROM "+commandtable+" ");
            if(allset.next()){
                val=true;
            }
            else{
                val=false;
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
        return val;
    }
    
    public QContainer getCommand() throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        ArrayList packet=new ArrayList();
        String upd=null; int type=0;
        Bitplay olah=new Bitplay();
        String address=null;
        
        try (Statement cmdquery=con.createStatement(); Statement delrecord=con.createStatement()) {
            ResultSet query = cmdquery.executeQuery("SELECT id , zone, address, mode, setpoint, errorband, lamp FROM "+commandtable+"");
            if(query.next()){
                address=query.getString("address");
                packet.add(EndGateway);
                packet.add(query.getInt("zone")); //zone
                packet.add(MCustomCluster); packet.add(LCustomCluster); packet.add(FCClient2Server); packet.add(Trans); packet.add(CWriteAttribute);
                packet.add(0x00); packet.add(LMode); packet.add(DT8Bitmap); packet.add(query.getInt("mode")); //mode value
                packet.add(0x00); packet.add(LLamp); packet.add(DT8Bitmap); packet.add(query.getInt("lamp")); //lamp value
                packet.add(0x00); packet.add(LSetPoint); packet.add(DT16Uint); packet.add(olah.getMSB((int)(10000*Math.log10(query.getFloat("setpoint"))), 16)); packet.add(olah.getLSB((int)(10000*Math.log10(query.getFloat("setpoint"))), 16));//setpoint value
                packet.add(0x00); packet.add(LSetPoint); packet.add(DT16Uint); packet.add(olah.getMSB((int)(10000*Math.log10(query.getFloat("errorband"))), 16)); packet.add(olah.getLSB((int)(10000*Math.log10(query.getFloat("errorband"))), 16)); //errorband value
                delrecord.executeUpdate("DELETE FROM "+commandtable+" WHERE id = '"+query.getInt("id")+"' ");
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }
        int[] vessel=new int[packet.size()];
        for(int i = 0;i < vessel.length;i++){
            vessel[i] = (int) packet.get(i);
        }
        QContainer box=new QContainer(address,vessel);
        return box;
    }
    
    public void getSchedule() throws SQLException{ // update Status table with data format [address, occ, light, lamp]
        String cmd=null;
        //get current date&time
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK); //sunday is 1
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        //schedule variable
        int dstart=0;
        int dend=0;
        java.sql.Time tstart=null;
        java.sql.Time tend=null;
        //operator type
        boolean opand;
        boolean scheck;
        
        int index=0;
        try (Statement qinactive=con.createStatement(); Statement qactive=con.createStatement(); Statement getstatus=con.createStatement();Statement inscmd=con.createStatement()) {
            //search for inactive schedule
            ResultSet rsinactive = qinactive.executeQuery("SELECT id,dstart , dend, tstart, tend, address, zone, lamp, mode, active FROM "+schedtable+" WHERE active = 0");
            while(rsinactive.next()){
                dstart=rsinactive.getInt("dstart");
                dend=rsinactive.getInt("dend");
                tstart=rsinactive.getTime("tstart");
                tend=rsinactive.getTime("tend");
                //check the schedule must be activated or not
                if( checkSchedule(day, hour,minute,dstart,dend,tstart,tend)){
                    System.out.println("inactive schedule found");
                    int id=rsinactive.getInt("id");
                    String address=rsinactive.getString("address");
                    int zone=rsinactive.getInt("zone");
                    ResultSet status = getstatus.executeQuery("SELECT setpoint , errorband FROM "+statustable+" WHERE address = '"+address+"' AND zone = "+zone+"");
                    if(status.next()){
                        cmd = "INSERT INTO "+commandtable+
                                    " VALUES (default,"+zone+",'"+address+"',"+rsinactive.getInt("mode")+","+status.getFloat("setpoint")+","+status.getFloat("errorband")+","+rsinactive.getInt("lamp")+")";
                        inscmd.executeUpdate(cmd);
                        cmd = "UPDATE "+schedtable+" SET active = 1  WHERE id = "+id+" ";
                        inscmd.executeUpdate(cmd);
                        
                    }                    
                }            
            }
            //search for active schedule
            ResultSet rsactive = qactive.executeQuery("SELECT id,dstart , dend, tstart, tend, address, zone, lamp, mode, active FROM "+schedtable+" WHERE active = 1");
            while(rsactive.next()){
                dstart=rsactive.getInt("dstart");
                dend=rsactive.getInt("dend");
                tstart=rsactive.getTime("tstart");
                tend=rsactive.getTime("tend");
                //check the schedule must be activated or not
                if(!checkSchedule(day, hour,minute,dstart,dend,tstart,tend)){
                    int id=rsactive.getInt("id");
                    String address=rsactive.getString("address");
                    int zone=rsactive.getInt("zone");
                    ResultSet status = getstatus.executeQuery("SELECT setpoint , errorband FROM "+statustable+" WHERE address = '"+address+"' AND zone = "+zone+"");
                    if(status.next()){
                        cmd = "INSERT INTO "+commandtable+
                                    " VALUES (default,"+zone+",'"+address+"',1,"+status.getFloat("setpoint")+","+status.getFloat("errorband")+","+rsactive.getInt("lamp")+")";
                        inscmd.executeUpdate(cmd);
                        cmd = "UPDATE "+schedtable+" SET active = 0  WHERE id = "+id+" ";
                        inscmd.executeUpdate(cmd);
                        
                    }                    
                }
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    
    public boolean checkSchedule(int day, int hour, int minute, int dstart, int dend,java.sql.Time tstart,java.sql.Time tend){
        boolean scheck;
        //check schedule day
        if (dstart <= dend) {  
            scheck = (day >= dstart && day <= dend);
        } else {
            scheck = (day >= dstart || day <= dend);
        }
        //check the hour && minute
        //System.out.println(tstart.getHours());
        //System.out.println(tend.getHours());
        if (tstart.getHours() < tend.getHours()) {
            if (hour == tstart.getHours()) {
                scheck = scheck && (minute > tstart.getMinutes());
            } else if (hour == tend.getHours()) {
                scheck = scheck && (minute < tend.getMinutes());
            } else {
                scheck = scheck && (hour > tstart.getHours() && hour < tend.getHours());
            }
        } else if (tstart.getHours() > tend.getHours()) {
            if (hour == tstart.getHours()) {
                scheck = scheck && (minute > tstart.getMinutes());
            } else if (hour == tend.getHours()) {
                scheck = scheck && (minute < tend.getMinutes());
            } else {
                System.out.println("tes");
                scheck = scheck && (hour > tstart.getHours() || hour < tend.getHours());
            }
        } else { //if the start hour and end hour same
            if (hour == tstart.getHours()) {
                if (tstart.getMinutes() < tend.getMinutes()) {
                    scheck = scheck && (minute >= tstart.getMinutes() && minute <= tend.getMinutes());
                } else if (tstart.getMinutes() > tend.getMinutes()) {

                    scheck = scheck && (minute >= tstart.getMinutes() || minute <= tend.getMinutes());
                }
            } else {
                scheck = scheck && false;
            }
        }
  
        return scheck;
    }
}

