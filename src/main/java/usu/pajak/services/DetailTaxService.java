package usu.pajak.services;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import usu.pajak.model.DetailTax;
import usu.pajak.model.TarifPajak;
import usu.pajak.model.UserPajak;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class DetailTaxService {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_server")); //connect to mongodb
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_server");
    private Logger logger;
    private String salaryId;
    private DetailTax detailTax;

    public DetailTaxService(Logger logger, String salaryId){
        this.logger = logger;
        this.salaryId = salaryId;
        setDetailTax(salaryId);
    }

    public DetailTax getDetailTax() {
        if(detailTax != null)
            return detailTax;
        else
            return new DetailTax();
    }

    private void setDetailTax(String salaryId) {
        UserPajak userPajak = datastore.createQuery(UserPajak.class).disableValidation().filter("pendapatan_tetap.salary_id", salaryId)
                .get();
        if(userPajak != null) {
            BasicDBList listTetap = userPajak.getPendapatan_tetap();
            for (int i = 0; i < listTetap.size(); i++) {
                BasicDBObject obj = (BasicDBObject) listTetap.get(i);
                if (obj.getString("salary_id").equalsIgnoreCase(salaryId)) {
                    detailTax = new DetailTax();
                    BigDecimal nettoPendapatanSetahun = new BigDecimal(obj.getString("netto_pendapatan")).multiply(new BigDecimal("12"));
                    BigDecimal potonganJabatan = new BigDecimal(obj.getString("pot_jabatan"));
                    BigDecimal potonganPensiun = new BigDecimal(obj.getString("pot_pensiun"));
                    BigDecimal nettoPendapatanSekarang = new BigDecimal(obj.getString("netto_pendapatan"));
                    BigDecimal brutoPendapatanSekarang = nettoPendapatanSekarang.add(potonganJabatan).add(potonganPensiun);
                    BigDecimal ptkpSetahun = new BigDecimal(userPajak.getPtkp_setahun());

                    BigDecimal pkpSetahun, sisaPtkpSetahun;
                    if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                        pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                        sisaPtkpSetahun = new BigDecimal("0.00");
                    } else {
                        pkpSetahun = new BigDecimal("0.00");
                        sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                    }

                    TarifPajak t = new TarifPajak();
                    if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                        if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                            t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                        else
                            t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                    } else {
                        t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                    }

                    detailTax.setBrutoPendapatanSekarang(brutoPendapatanSekarang.doubleValue());
                    detailTax.setPotonganJabatan(potonganJabatan.doubleValue());
                    detailTax.setPotonganPensiun(potonganPensiun.doubleValue());
                    detailTax.setNettoPendapatanSekarang(nettoPendapatanSekarang.doubleValue());
                    detailTax.setNettoPendapatanSekarangDisetahunkan(nettoPendapatanSetahun.doubleValue());
                    detailTax.setPtkp(ptkpSetahun.doubleValue());
                    detailTax.setPph21(t.getListPph21());

                    break;
                }
            }
        }

        userPajak = datastore.createQuery(UserPajak.class).disableValidation().filter("pendapatan_tdk_tetap.salary_id", salaryId)
                .get();
        if(userPajak != null) {
            BasicDBList list = userPajak.getPendapatan_tdk_tetap();
            for (int i = 0; i < list.size(); i++) {
                BasicDBObject obj = (BasicDBObject) list.get(i);
                if (obj.getString("salary_id").equalsIgnoreCase(salaryId)) {
                    detailTax = new DetailTax();
                    if (userPajak.getPendapatan_tetap() != null) {
                        BasicDBObject pTetap = (BasicDBObject) userPajak.getPendapatan_tetap().get(0);
                        BigDecimal nettoPendapatanSetahun = new BigDecimal(pTetap.getString("netto_pendapatan")).multiply(new BigDecimal("12"));
                        if (i > 0) {
                            int x = i - 1;
                            for (int j = x; j >= 0; j--) {
                                BasicDBObject pTdkTetap = (BasicDBObject) list.get(j);
                                BigDecimal pendapatanLalu = new BigDecimal(pTdkTetap.getString("netto_pendapatan"));
                                nettoPendapatanSetahun = nettoPendapatanSetahun.add(pendapatanLalu);
                            }
                        }

                        BigDecimal potonganJabatan = new BigDecimal(obj.getString("pot_jabatan"));
                        BigDecimal potonganPensiun = new BigDecimal(obj.getString("pot_pensiun"));
                        BigDecimal nettoPendapatanSekarang = new BigDecimal(obj.getString("netto_pendapatan"));
                        BigDecimal brutoPendapatanSekarang = nettoPendapatanSekarang.add(potonganJabatan).add(potonganPensiun);

                        BigDecimal ptkpSetahun = new BigDecimal(userPajak.getPtkp_setahun());

                        BigDecimal pkpSetahun, sisaPtkpSetahun;
                        if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                            pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                            sisaPtkpSetahun = new BigDecimal("0.00");
                        } else {
                            pkpSetahun = new BigDecimal("0.00");
                            sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                        }
                        System.out.println("Sisa_PTKP_Sebelumnya : "+sisaPtkpSetahun);

                        TarifPajak t = new TarifPajak();
                        if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                            if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                            else
                                t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        } else {
                            t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        }

                        detailTax.setNettoPendapatanSebelumnya(nettoPendapatanSetahun.doubleValue());
                        detailTax.setBrutoPendapatanSekarang(brutoPendapatanSekarang.doubleValue());
                        detailTax.setPotonganJabatan(potonganJabatan.doubleValue());
                        detailTax.setPotonganPensiun(potonganPensiun.doubleValue());
                        detailTax.setNettoPendapatanSekarang(nettoPendapatanSekarang.doubleValue());
                        detailTax.setPtkp(ptkpSetahun.doubleValue());
                        detailTax.setPph21Sebelumnya(t.getListPph21());

                        if (nettoPendapatanSekarang.compareTo(sisaPtkpSetahun) >= 0) {
                            pkpSetahun = nettoPendapatanSekarang.subtract(sisaPtkpSetahun);
                            sisaPtkpSetahun = new BigDecimal("0.00");
                        } else {
                            pkpSetahun = new BigDecimal("0.00");
                            sisaPtkpSetahun = sisaPtkpSetahun.subtract(nettoPendapatanSekarang);
                        }
                        System.out.println("Sisa_PTKP_Sesudahnya : "+sisaPtkpSetahun);
                        TarifPajak p = new TarifPajak();
                        if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                            if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                p.hitungPajak(t.getReminderPajak(), pkpSetahun, t.getIndex(), TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                            else
                                p.hitungPajak(t.getReminderPajak(), pkpSetahun, t.getIndex(), TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        } else {
                            p.hitungPajak(t.getReminderPajak(), pkpSetahun, t.getIndex(), TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        }
                        detailTax.setPph21(p.getListPph21());

                    } else {
                        BigDecimal nettoPendapatanSetahun = new BigDecimal("0.00");
                        if (i > 0) {
                            int x = i - 1;
                            for (int j = x; j <= 0; j--) {
                                BasicDBObject pTdkTetap = (BasicDBObject) list.get(j);
                                BigDecimal pendapatanLalu = new BigDecimal(pTdkTetap.getString("netto_pendapatan"));
                                nettoPendapatanSetahun = nettoPendapatanSetahun.add(pendapatanLalu);
                            }
                        }
                        BigDecimal potonganJabatan = new BigDecimal(obj.getString("pot_jabatan"));
                        BigDecimal potonganPensiun = new BigDecimal(obj.getString("pot_pensiun"));
                        BigDecimal nettoPendapatanSekarang = new BigDecimal(obj.getString("netto_pendapatan"));
                        BigDecimal brutoPendapatanSekarang = nettoPendapatanSekarang.add(potonganJabatan).add(potonganPensiun);
                        BigDecimal ptkpSetahun = new BigDecimal(userPajak.getPtkp_setahun());

                        BigDecimal pkpSetahun, sisaPtkpSetahun;
                        if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                            pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                            sisaPtkpSetahun = new BigDecimal("0.00");
                        } else {
                            pkpSetahun = new BigDecimal("0.00");
                            sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                        }

                        TarifPajak t = new TarifPajak();
                        if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                            if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                            else
                                t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        } else {
                            t.hitungPajak(new BigDecimal("50000000.00"), pkpSetahun, 0, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        }

                        detailTax.setNettoPendapatanSebelumnya(nettoPendapatanSetahun.doubleValue());
                        detailTax.setBrutoPendapatanSekarang(brutoPendapatanSekarang.doubleValue());
                        detailTax.setPotonganJabatan(potonganJabatan.doubleValue());
                        detailTax.setPotonganPensiun(potonganPensiun.doubleValue());
                        detailTax.setNettoPendapatanSekarang(nettoPendapatanSekarang.doubleValue());
                        detailTax.setPtkp(ptkpSetahun.doubleValue());
                        detailTax.setPph21Sebelumnya(t.getListPph21());

                        if (nettoPendapatanSekarang.compareTo(sisaPtkpSetahun) >= 0) {
                            pkpSetahun = nettoPendapatanSekarang.subtract(sisaPtkpSetahun);
                            sisaPtkpSetahun = new BigDecimal("0.00");
                        } else {
                            pkpSetahun = new BigDecimal("0.00");
                            sisaPtkpSetahun = sisaPtkpSetahun.subtract(nettoPendapatanSekarang);
                        }
                        TarifPajak p = new TarifPajak();
                        if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                            if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                p.hitungPajak(t.getReminderPajak(), pkpSetahun, t.getIndex(), TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                            else
                                p.hitungPajak(t.getReminderPajak(), pkpSetahun, t.getIndex(), TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        } else {
                            p.hitungPajak(t.getReminderPajak(), pkpSetahun, t.getIndex(), TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                        }
                        detailTax.setPph21(p.getListPph21());
                    }

                    break;
                }
            }
        }
    }
}
