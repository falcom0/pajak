package usu.pajak.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TarifPajak {

/*    public static void main(String[] args) throws IOException {
//        new TarifPajak().test(50000000.00);
        TarifPajak tp = new TarifPajak();
        BasicDBList list = new BasicDBList();
        Double first = 0.00;
        Double res = tp.recursive(first,0);
//        list.add(tp.bTarif);
        System.out.println("hasil akhir 1:"+dec.format(res));
        if(tp.index <= 3) {
            Double pengurang = tp.reminder;
            Double nilai = 10000000.00;
            if(pengurang < nilai){
                res = tp.recursive(tp.reminder, tp.index);
//                list.add(tp.bTarif);
                System.out.println("hasil akhir 2:"+dec.format(res));
//                System.out.println("nilai n:"+dec.format(tp.n)+" index:"+tp.index);
                Double tambah = nilai - pengurang;
                res = tp.recursive(tambah, tp.index+1);
//                list.add(tp.bTarif);
                System.out.println("hasil akhir 2:"+dec.format(res));
            }else{
                res = tp.recursive(nilai, tp.index);
//                list.add(tp.bTarif);
                System.out.println("hasil akhir 2:"+dec.format(res));
            }
        }
        System.out.println("");
    }*/

//    public void test(Double t){
//        Double res = 0.00;
//        if(t > LAYER_1_BULAN){
//            t = t - LAYER_1_BULAN;
//            res = LIMA_PERSEN * LAYER_1_BULAN;
//            System.out.println("hasil if "+res.toString());
//            if(t > LAYER_2_BULAN){
//                t = t - LAYER_2_BULAN;
//                res = LIMA_BELAS_PERSEN * LAYER_2_BULAN;
//                System.out.println("hasil if "+res.toString());
//                if(t > LAYER_3_BULAN) {
//                    t = t - LAYER_3_BULAN;
//                    res = DUA_LIMA_PERSEN * LAYER_3_BULAN;
//                    System.out.println("hasil if "+res.toString());
//                    if(t < 0){
//
//                    }else {
//                        res = TIGA_PULUH_PERSEN * t;
//                        System.out.println("hasil else " + res.toString());
//                    }
//                }else{
//                    res = DUA_LIMA_PERSEN * t;
//                    System.out.println("hasil else "+res.toString());
//                }
//            }else{
//                res = LIMA_BELAS_PERSEN * t;
//                System.out.println("hasil else "+res.toString());
//            }
//        }else{
//            res = LIMA_PERSEN * t;
//            System.out.println("hasil else "+res.toString());
//        }
//    }

    private ArrayList<BigDecimal[]> listLayer = new ArrayList<>();
    private ArrayList<BigDecimal[]> listTarif = new ArrayList<>();
    private Integer index;
    private BigDecimal reminderPajak;
    private BasicDBList listPph21 = new BasicDBList();

    public static final Integer LAYER_SEBULAN = 0;
    public static final Integer LAYER_SETAHUN = 1;
    public static final Integer TARIF_NPWP = 0;
    public static final Integer TARIF_NON_NPWP = 1;

    public BigDecimal getReminderPajak() {
        return reminderPajak;
    }

    public TarifPajak(){
        listLayer.add(new BigDecimal[]{new BigDecimal("4166666.667"),new BigDecimal("16666666.667"),new BigDecimal("20833333.333")});
        listLayer.add(new BigDecimal[]{new BigDecimal("50000000.000"),new BigDecimal("200000000.000"),new BigDecimal("250000000.000"),new BigDecimal("0.00")});
        listTarif.add(new BigDecimal[]{new BigDecimal("0.05"),new BigDecimal("0.15"),new BigDecimal("0.25"), new BigDecimal("0.30")});
        listTarif.add(new BigDecimal[]{new BigDecimal("0.06"),new BigDecimal("0.18"),new BigDecimal("0.30"), new BigDecimal("0.36")});
    }

    public void hitungPajak(BigDecimal reminderPajak, BigDecimal pkp, int index, int layerIndex, int tarifIndex, boolean rutin){
        BigDecimal[] layer = listLayer.get(layerIndex);
        BigDecimal[] tarif = listTarif.get(tarifIndex);
        if(pkp.compareTo(reminderPajak) > 0){
            if(index < 3){
                pkp = pkp.subtract(reminderPajak);

                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("_tarif",tarif[index].toString());

                if(rutin) {
                    bTarif.put("_pkp", reminderPajak.divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP).toString());
                    bTarif.put("_hasil", reminderPajak.multiply(tarif[index]).divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP).toString());
                }else{
                    bTarif.put("_pkp", reminderPajak.toString());
                    bTarif.put("_hasil", reminderPajak.multiply(tarif[index]).toString());
                }

                listPph21.add(bTarif);

                reminderPajak = layer[index + 1];

                hitungPajak(reminderPajak,pkp,index + 1,layerIndex,tarifIndex,rutin);
            }else{
                this.index = index;
                this.reminderPajak = new BigDecimal("0.00");

                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("_tarif",tarif[index].toString());
                bTarif.put("_pkp",pkp.toString());
                bTarif.put("_hasil", pkp.multiply(tarif[index]).toString());

                listPph21.add(bTarif);
            }
        }else{
            this.reminderPajak = reminderPajak.subtract(pkp);
            this.index = index;

            if(this.reminderPajak.compareTo(BigDecimal.ZERO) == 0 && index < 3) {
                this.index = index + 1;
                this.reminderPajak = tarif[index + 1];
            }

            BasicDBObject bTarif = new BasicDBObject();
            bTarif.put("_tarif",tarif[index].toString());

            if(rutin) {
                bTarif.put("_pkp", pkp.divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP).toString());
                bTarif.put("_hasil", pkp.multiply(tarif[index]).divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP).toString());
            }else{
                bTarif.put("_pkp", pkp.toString());
                bTarif.put("_hasil", pkp.multiply(tarif[index]).toString());
            }

            listPph21.add(bTarif);
        }
    }

/*    public void hitungPajak(BigDecimal value, int index, int layerIndex, int tarifIndex){
        BigDecimal[] layer = listLayer.get(layerIndex);
        BigDecimal[] tarif = listTarif.get(tarifIndex);
        if(value.compareTo(layer[index]) > 0){
            if(index < 3){
                value = value.subtract(layer[index]);
                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("tarif",tarif[index].toString());
//                bTarif.put("pkp_setahun",layer[index].toString());
//                bTarif.put("hasil_setahun", layer[index].multiply(tarif[index]).toString());
                bTarif.put("pkp_sebulan",layer[index].divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).toString());
                bTarif.put("hasil_sebulan", layer[index].multiply(tarif[index]).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).toString());
                listPph21.add(bTarif);
                hitungPajak(value,index + 1,layerIndex,tarifIndex);
            }else{
                this.index = index;
                this.reminderPajak = value.subtract(layer[index]);
                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("tarif",tarif[index].toString());
//                bTarif.put("pkp_setahun",value.toString());
//                bTarif.put("hasil_setahun", v  alue.multiply(tarif[index]).toString());
                bTarif.put("pkp_sebulan",value.divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).toString());
                bTarif.put("hasil_sebulan", value.multiply(tarif[index]).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).toString());
                listPph21.add(bTarif);
            }
        }else{
            this.index = index;
            this.reminderPajak = layer[index].subtract(value);
            BasicDBObject bTarif = new BasicDBObject();
            bTarif.put("tarif",tarif[index].toString());
//            bTarif.put("pkp_setahun",value.toString());
//            bTarif.put("hasil_setahun", value.multiply(tarif[index]).toString());
            bTarif.put("pkp_sebulan",value.divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).toString());
            bTarif.put("hasil_sebulan", value.multiply(tarif[index]).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).toString());
            listPph21.add(bTarif);
        }
    }*/

    public BasicDBList getListPph21() {
        return listPph21;
    }

    public Integer getIndex() {
        return index;
    }
/*
    public Double recursiveTdkNpwp(Double n, int index){
        if(n > layer[index]){
            if(index < 3) {
                n = n-layer[index];
//                System.out.println(Double.valueOf(tarif[index] * layer[index]).toString());
                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("tarif",tarifTdkNpwp[index].toString());
                bTarif.put("pkp",dec.format(layer[index]));
                bTarif.put("hasil",dec.format(tarifTdkNpwp[index] * layer[index]));
                listPph21.add(bTarif);
                return (tarifTdkNpwp[index] * layer[index]) + recursive(n, index + 1);
            }else {
                this.index = index;
                this.reminder = n - layer[index];
//                System.out.println(Double.valueOf(tarif[index] * n).toString());
                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("tarif",tarifTdkNpwp[index].toString());
                bTarif.put("pkp",dec.format(n));
                bTarif.put("hasil",dec.format(tarifTdkNpwp[index] * n));
                listPph21.add(bTarif);
                return Double.valueOf(tarifTdkNpwp[index] * n);
            }
        }else{
            this.index = index;
            this.reminder = layer[index] - n;
//            System.out.println(Double.valueOf(tarif[index] * n).toString());
            BasicDBObject bTarif = new BasicDBObject();
            bTarif.put("tarif",tarifTdkNpwp[index].toString());
            if(n == 0) {
                bTarif.put("pkp", "0.00");
                bTarif.put("hasil", "0.00");
            }else {
                bTarif.put("pkp",dec.format(n));
                bTarif.put("hasil", dec.format(tarifTdkNpwp[index] * n));
            }
            listPph21.add(bTarif);
            return Double.valueOf(tarifTdkNpwp[index] * n);
        }
    }*/


//    public Double recursive(Double n, int index){
//        if(n > layer[index]){
//            if(index < 3) {
//                n = n-layer[index];
////                System.out.println(Double.valueOf(tarif[index] * layer[index]).toString());
//                BasicDBObject bTarif = new BasicDBObject();
//                bTarif.put("tarif",tarif[index].toString());
//                bTarif.put("pkp",dec.format(layer[index]));
//                bTarif.put("hasil",dec.format(tarif[index] * layer[index]));
//                listPph21.add(bTarif);
//                return (tarif[index] * layer[index]) + recursive(n, index + 1);
//            }else {
//                this.index = index;
//                this.reminder = n - layer[index];
////                System.out.println(Double.valueOf(tarif[index] * n).toString());
//                BasicDBObject bTarif = new BasicDBObject();
//                bTarif.put("tarif",tarif[index].toString());
//                bTarif.put("pkp",dec.format(n));
//                bTarif.put("hasil",dec.format(tarif[index] * n));
//                listPph21.add(bTarif);
//                return Double.valueOf(tarif[index] * n);
//            }
//        }else{
//            this.index = index;
//            this.reminder = layer[index] - n;
////            System.out.println(Double.valueOf(tarif[index] * n).toString());
//            BasicDBObject bTarif = new BasicDBObject();
//            bTarif.put("tarif",tarif[index].toString());
//            if(n == 0) {
//                bTarif.put("pkp", "0.00");
//                bTarif.put("hasil", "0.00");
//            }else {
//                bTarif.put("pkp",dec.format(n));
//                bTarif.put("hasil", dec.format(tarif[index] * n));
//            }
//            listPph21.add(bTarif);
//            return Double.valueOf(tarif[index] * n);
//        }
//    }
}
