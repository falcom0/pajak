package usu.pajak.services;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import usu.pajak.model.UserPajak;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserPajakService {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_server")); //connect to mongodb
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_server");
    private Logger logger;
    private String userId;
    private UserPajak userPajak;
    private List<UserPajak> listUserPajak;

    public UserPajakService(Logger logger, String userId){
        this.logger = logger;
        this.userId = userId;
        setUserPajak(userId);
    }

    public UserPajakService(){

    }

    public UserPajak getUserPajak() {
        return userPajak;
    }

    private void setUserPajak(String userId){
        UserPajak userPajak = datastore.createQuery(UserPajak.class).filter("id_user", userId).get();
        BasicDBList list = userPajak.getPendapatan_tetap();
        list.forEach(row -> {
            BasicDBObject obj = (BasicDBObject) row;
            if(!obj.getString("activity_id").equalsIgnoreCase("apbn")){
                userPajak.setPendapatan_tetap(null);
            }
        });
        userPajak.setPendapatan_tdk_tetap(null);
        userPajak.setReminder_pajak(null);
        userPajak.setIndex_layer_pajak(null);
        userPajak.setTotal_pph21_pribadi(null);
        this.userPajak = userPajak;
    }

    public List<UserPajak> getListUserPajak(String month, String unitId, boolean apbn,boolean pegawai_luar, String sumberDana) {
        this.listUserPajak = null;
        if(unitId != null) {
            Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
            if(!apbn) {
                query.or(
                        query.and(
                                query.criteria("pendapatan_tetap.activity_id").notEqual("apbn"),
                                query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(month),
                                query.criteria("pendapatan_tetap.unit_id").equalIgnoreCase(unitId)
                        ),
                        query.and(
                                query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(month),
                                query.criteria("pendapatan_tdk_tetap.unit_id").equalIgnoreCase(unitId)
                        )
                );
            }else if(!pegawai_luar){
                query.or(
                        query.and(
                                query.criteria("id_user").not().containsIgnoreCase("TA-"),
                                query.criteria("id_user").not().containsIgnoreCase("MWA-"),
                                query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(month),
                                query.criteria("pendapatan_tetap.unit_id").equalIgnoreCase(unitId),
                                query.criteria("pendapatan_tetap.source_of_fund").equalIgnoreCase(sumberDana)
                        ),
                        query.and(
                                query.criteria("id_user").not().containsIgnoreCase("TA-"),
                                query.criteria("id_user").not().containsIgnoreCase("MWA-"),
                                query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(month),
                                query.criteria("pendapatan_tdk_tetap.unit_id").equalIgnoreCase(unitId),
                                query.criteria("pendapatan_tdk_tetap.source_of_fund").equalIgnoreCase(sumberDana)
                        )
                );
            }else if(pegawai_luar){
                query.or(
                        query.and(
//                                query.or(
//                                    query.criteria("id_user").containsIgnoreCase("TA-"),
                                    query.criteria("id_user").containsIgnoreCase("-"),
//                                ),
                                query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(month),
                                query.criteria("pendapatan_tetap.unit_id").equalIgnoreCase(unitId),
                                query.criteria("pendapatan_tetap.source_of_fund").equalIgnoreCase(sumberDana)
                        ),
                        query.and(
//                                query.or(
//                                    query.criteria("id_user").containsIgnoreCase("TA-"),
                                    query.criteria("id_user").containsIgnoreCase("-"),
//                                ),
                                query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(month),
                                query.criteria("pendapatan_tdk_tetap.unit_id").equalIgnoreCase(unitId),
                                query.criteria("pendapatan_tdk_tetap.source_of_fund").equalIgnoreCase(sumberDana)
                        )
                );
            }

            List<UserPajak> list = query.asList();
            for (UserPajak up:list) {
                if(up.getPendapatan_tetap()!=null){
                    BasicDBList listTetap = up.getPendapatan_tetap().stream().filter(e -> {
                        BasicDBObject obj = (BasicDBObject) e;
                        if(obj.getString("activity_id").equalsIgnoreCase("apbn"))
                            return false;
                        else{
                            Integer bulan = Integer.parseInt(obj.getString("bulan"));
                            if(bulan != Integer.parseInt(month)){
                                return false;
                            }else{
                                if(!obj.getString("unit_id").equalsIgnoreCase(unitId)){
                                    return false;
                                }else{
                                    if(!obj.getString("source_of_fund").equalsIgnoreCase(sumberDana)){
                                        return false;
                                    }else{
                                        return true;
                                    }
                                }
                            }
                        }
                    }).collect(Collectors.toCollection(BasicDBList::new));

                    up.setPendapatan_tetap(listTetap);
                }

                if(up.getPendapatan_tdk_tetap()!=null){
                    BasicDBList listTdkTetap = up.getPendapatan_tdk_tetap().stream().filter(e -> {
                        BasicDBObject obj = (BasicDBObject) e;
                        if(obj.getString("activity_id").equalsIgnoreCase("apbn"))
                            return false;
                        else{
                            Integer bulan = Integer.parseInt(obj.getString("bulan"));
                            if(bulan != Integer.parseInt(month)){
                                return false;
                            }else{
                                if(!obj.getString("unit_id").equalsIgnoreCase(unitId)){
                                    return false;
                                }else{

                                    if(!obj.getString("source_of_fund").equalsIgnoreCase(sumberDana)){
                                        return false;
                                    }else{
                                        return true;
                                    }
                                }
                            }
                        }
                    }).collect(Collectors.toCollection(BasicDBList::new));

                    up.setPendapatan_tdk_tetap(listTdkTetap);
                }


                /*BasicDBList listTetap = up.getPendapatan_tetap();
                if (listTetap != null) {
                    Iterator iterate = listTetap.iterator();
                    while(iterate.hasNext()){
                        BasicDBObject bdo = (BasicDBObject) iterate.next();
                        if(bdo.getString("activity_id").equalsIgnoreCase("apbn")){
                            iterate.remove();
                        }else{
                            Integer bulan = Integer.parseInt(bdo.getString("bulan"));
                            if(bulan != Integer.parseInt(month)){
                                iterate.remove();
                            }else{
                                if(!bdo.getString("unit_id").equalsIgnoreCase(unitId)){
                                    iterate.remove();
                                }
                            }
                        }
                    }
                }

                BasicDBList listTdkTetap = up.getPendapatan_tdk_tetap();
                if(listTdkTetap != null) {
                    Iterator iterate = listTdkTetap.iterator();
                    while (iterate.hasNext()){
                        BasicDBObject bdo = (BasicDBObject) iterate.next();
                        Integer bulan = Integer.parseInt(bdo.getString("bulan"));
                        if (bulan != Integer.parseInt(month)) {
                            iterate.remove();
                        }else{
                            if(!bdo.getString("unit_id").equalsIgnoreCase(unitId)){
                                iterate.remove();
                            }
                        }
                    }
                }*/
            }
            return list;
        }else {
            System.out.println("Here SALAH");
            Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
            if(!apbn) {
                query.or(
                        query.and(
                                query.criteria("pendapatan_tetap.activity_id").notEqual("apbn"),
                                query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(month)
                        ),
                        query.and(
                                query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(month)
                        )
                );
            }else {
                query.or(
                        query.and(
                                query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(month)
                        ),
                        query.and(
                                query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(month)
                        )
                );
            }

            List<UserPajak> list = query.asList();

            for (UserPajak up:list) {
                BasicDBList listTetap = up.getPendapatan_tetap();
                if (listTetap != null) {
                    Iterator iterate = listTetap.iterator();
                    while(iterate.hasNext()){
                        BasicDBObject bdo = (BasicDBObject) iterate.next();
                        if(bdo.getString("activity_id").equalsIgnoreCase("apbn")){
                            iterate.remove();
                        }else{
                            Integer bulan = Integer.parseInt(bdo.getString("bulan"));
                            if(bulan != Integer.parseInt(month)){
                                iterate.remove();
                            }
                        }
                    }
                }

                BasicDBList listTdkTetap = up.getPendapatan_tdk_tetap();
                if(listTdkTetap != null) {
                    Iterator iterate = listTdkTetap.iterator();
                    while (iterate.hasNext()){
                        BasicDBObject bdo = (BasicDBObject) iterate.next();
                        Integer bulan = Integer.parseInt(bdo.getString("bulan"));
                        if (bulan != Integer.parseInt(month)) {
                            iterate.remove();
                        }
                    }
                }
            }
            return list;
        }
    }

    public String addSourceOfFund(Logger logger){
        try {
            new ApiRka(logger).addSourceOfFund();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void cekRequest(Logger logger){
        new ApiRka(logger).hitungUlangPajak();
    }

    public void mergeUser(Logger logger,String oldUserId, String newUserId){
        new ApiRka(logger).moveUserId(oldUserId,newUserId);
    }

    public void movePendapatan(Logger logger, String targetUserId, String t_salaryId, String destUserId){
        new ApiRka(logger).movePendapatan(targetUserId,t_salaryId,destUserId);
    }
}
