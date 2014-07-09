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
    String adrtable="status";
    String statustable="status";
    String commandtable="command";
    
    public SQLmod(String DB_URL, String USER, String PASS) throws SQLException{ //class constructor for setting url, user, password
        this.DB_URL=DB_URL;
        this.USER=USER;
        this.PASS=PASS;
        con = DriverManager.getConnection(DB_URL, USER, PASS);
    }
    
    public void updateStatus(String address, int amode, int lmode, int arelay, int lrelay, int occ, double temp, double hum, double lux, double aset, double aerror, double lset, double lerror) throws SQLException{ // update Status table with data format [address, occ, light, lamp]
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
                        upd = "UPDATE "+statustable+" SET zone = 'Ruang Utama' , amode = "+amode+" , lmode = "+lmode+" , arelay = "+arelay+" , lrelay = "+lrelay+" , occ = "+occ+" , temp = "+temp+" , hum = "+hum+" , lux = "+lux+" , aset = "+aset+" , aerror = "+aerror+" , lset = "+lset+" , lerror = "+lerror+" WHERE address = '"+address+"' ";
                        insupd.executeUpdate(upd);
                        System.out.println("Berhasil memasukkan data ke database di alamat "+address+", ");   
                    }
                }
                else{ // if "address not found" insert new row
                    System.out.println("address not found");
                    upd = "INSERT INTO "+statustable+
                                " VALUES ('"+address+"','Ruang Utama',"+amode+","+lmode+","+arelay+","+lrelay+","+occ+","+temp+","+hum+","+lux+","+aset+","+aerror+","+lset+","+lerror+")";
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
        boolean val=false;
        try (Statement allfind=con.createStatement()) {
            ResultSet allset = allfind.executeQuery("SELECT address FROM "+commandtable+" ");
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
            ResultSet query = cmdquery.executeQuery("SELECT id, address, amode, lmode, arelay, lrelay, aset, aerror, lset, lerror FROM "+commandtable+"");
            if(query.next()){
                address=query.getString("address");
                packet.add(EndGateway);
                packet.add(query.getInt("amode"));
                packet.add(query.getInt("lmode"));
                packet.add(query.getInt("arelay"));
                packet.add(query.getInt("lrelay"));
                
                packet.add(olah.getMSB((int)(10*query.getFloat("aset")), 16)); packet.add(olah.getLSB((int)(10*query.getFloat("aset")), 16));
                packet.add(olah.getMSB((int)(10*query.getFloat("aerror")), 16)); packet.add(olah.getLSB((int)(10*query.getFloat("aerror")), 16));
                packet.add(olah.getMSB((int)(10*query.getFloat("lset")), 16)); packet.add(olah.getLSB((int)(10*query.getFloat("lset")), 16));
                System.out.println("SETPOINT TEMPERATURE"+query.getFloat("lset"));
                packet.add(olah.getMSB((int)(10*query.getFloat("lerror")), 16)); packet.add(olah.getLSB((int)(10*query.getFloat("lerror")), 16));
                
                
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
    
}

