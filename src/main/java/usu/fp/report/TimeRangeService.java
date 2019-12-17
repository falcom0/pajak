package usu.fp.report;

import com.google.gson.Gson;
import usu.fp.model.FpLog;
import usu.fp.model.FpLogTotal;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimeRangeService {
    private String unitId;
    private String startTime;
    private String endTime;
    private Connection connectionSimSdm;
    private Connection connectionPeople;

    public TimeRangeService(String unitId, String startTime, String endTime){
        this.unitId = unitId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public TimeRangeService(){

    }

    public void connect() throws SQLException {
        connectionSimSdm = DriverManager.getConnection("jdbc:mariadb://simsdm.usu.ac.id/simsdm?zeroDateTimeBehavior=convertToNull&user=simsdm&password=5Vw5PKrHbg5Tsg");
        connectionPeople = DriverManager.getConnection("jdbc:mariadb://simsdm.usu.ac.id/people?user=simsdm&password=5Vw5PKrHbg5Tsg");
    }

    public void disconnect() throws SQLException {
        connectionSimSdm.close();
        connectionPeople.close();
    }

    public Connection getConnectionSimSdm() {
        return connectionSimSdm;
    }

    public Connection getConnectionPeople() {
        return connectionPeople;
    }

    public String getResult(){
        try {
            connect();
            Statement statementPeople = connectionPeople.createStatement();
            ResultSet rsPeople = statementPeople.executeQuery("select * from ref_group;");
            HashMap<Integer,String> listType = new HashMap<>();
            while(rsPeople.next()){
                listType.put(rsPeople.getInt("id"),rsPeople.getString("title"));
                System.out.println(rsPeople.getInt("id")+" = "+rsPeople.getString("title"));
            }

            if(unitId != null) {
                Statement statement = connectionSimSdm.createStatement();
                String sqlQuery = "select f.id,f.unit_id,f.user,f.timestamp, l.type from FP_LOG f " +
                        "left join lecturer l on f.user = l.lecturer_id " +
                        "where f.unit_id = " + unitId + " and f.timestamp between '" + startTime + "' and '" + endTime + "';";
                ResultSet rs = statement.executeQuery(sqlQuery);
                List<FpLog> listFpLog = new ArrayList<>();
                while (rs.next()) {
                    FpLog fpLog = new FpLog();
                    fpLog.setId(rs.getInt("id"));
                    fpLog.setUnit_id(rs.getInt("unit_id"));
                    fpLog.setUser_id(rs.getInt("user"));
                    fpLog.setTimestamp(rs.getTimestamp("timestamp").toString());
                    fpLog.setType_id(rs.getInt("type"));
                    fpLog.setType_title(listType.get(rs.getInt("type")));
                    listFpLog.add(fpLog);
                }
                disconnect();
                return new Gson().toJson(listFpLog);
            }else {
                List<FpLog> listPnsHadir = new ArrayList<>();
                List<FpLog> listNonPnsHadir = new ArrayList<>();
                List<FpLog> listPnsTdkHadir = new ArrayList<>();
                List<FpLog> listNonPnsTdkHadir = new ArrayList<>();

                String sql = "select f.id,f.unit_id,f.user,f.timestamp, l.type from FP_LOG f " +
                        "left join lecturer l on f.user = l.lecturer_id " +
                        "where l.type in (0,1,6) and f.timestamp between '" + startTime + "' and '" + endTime + "' group by f.user ;";
                ResultSet rs = connectionSimSdm.createStatement().executeQuery(sql);
                while (rs.next()) {
                    FpLog fpLog = new FpLog();
                    fpLog.setId(rs.getInt("id"));
                    fpLog.setUnit_id(rs.getInt("unit_id"));
                    fpLog.setUser_id(rs.getInt("user"));
                    fpLog.setTimestamp(rs.getTimestamp("timestamp").toString());
                    fpLog.setType_id(rs.getInt("type"));
                    fpLog.setType_title(listType.get(rs.getInt("type")));
                    listPnsHadir.add(fpLog);
                }

                sql = "select f.id,f.unit_id,f.user,f.timestamp, l.type from FP_LOG f " +
                        "left join lecturer l on f.user = l.lecturer_id " +
                        "where l.type not in (0,1,6) and f.timestamp between '" + startTime + "' and '" + endTime + "' group by f.user ;";
                rs = connectionSimSdm.createStatement().executeQuery(sql);
                while (rs.next()) {
                    FpLog fpLog = new FpLog();
                    fpLog.setId(rs.getInt("id"));
                    fpLog.setUnit_id(rs.getInt("unit_id"));
                    fpLog.setUser_id(rs.getInt("user"));
                    fpLog.setTimestamp(rs.getTimestamp("timestamp").toString());
                    fpLog.setType_id(rs.getInt("type"));
                    fpLog.setType_title(listType.get(rs.getInt("type")));
                    listNonPnsHadir.add(fpLog);
                }

                sql = "select le.lecturer_id,le.status from lecturer le where le.lecturer_id not in (" +
                        "select f.user from FP_LOG f " +
                        "where f.timestamp between '" + startTime + "' and '" + endTime + "' " +
                        "group by f.user) and (le.status = 'Aktif' or le.status = 'Izin Belajar') and le.type in (0,1,6);";
                rs = connectionSimSdm.createStatement().executeQuery(sql);
                while (rs.next()) {
                    FpLog fpLog = new FpLog();
                    fpLog.setUser_id(rs.getInt("lecturer_id"));
                    fpLog.setTimestamp(rs.getString("status"));
                    listPnsTdkHadir.add(fpLog);
                }

                sql = "select le.lecturer_id,le.status from lecturer le where le.lecturer_id not in (" +
                        "select f.user from FP_LOG f " +
                        "where f.timestamp between '" + startTime + "' and '" + endTime + "' " +
                        "group by f.user) and (le.status = 'Aktif' or le.status = 'Izin Belajar') and le.type not in (0,1,6);";
                rs = connectionSimSdm.createStatement().executeQuery(sql);
                while (rs.next()) {
                    FpLog fpLog = new FpLog();
                    fpLog.setUser_id(rs.getInt("lecturer_id"));
                    fpLog.setTimestamp(rs.getString("status"));
                    listNonPnsTdkHadir.add(fpLog);
                }

                FpLogTotal fpLogTotal = new FpLogTotal();
                fpLogTotal.setListPnsHadir(listPnsHadir);
                fpLogTotal.setListNonPnsHadir(listNonPnsHadir);
                fpLogTotal.setListPnsTdkHadir(listPnsTdkHadir);
                fpLogTotal.setListNonPnsTdkHadir(listNonPnsTdkHadir);

                System.out.println("PNS HADIR : "+listPnsHadir.size());
                System.out.println("NON PNS HADIR : "+listNonPnsHadir.size());
                System.out.println("PNS TIDAK HADIR : "+listPnsTdkHadir.size());
                System.out.println("NON PNS TIDAK HADIR : "+listNonPnsTdkHadir.size());

                disconnect();
                return new Gson().toJson(fpLogTotal);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
