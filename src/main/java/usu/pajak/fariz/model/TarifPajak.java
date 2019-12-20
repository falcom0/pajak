package usu.pajak.fariz.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.math.BigDecimal;
import java.util.ArrayList;

public class TarifPajak {

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
                bTarif.put("_tarif",tarif[index]);

                if(rutin) {
                    bTarif.put("_pkp", reminderPajak.divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP));
                    bTarif.put("_hasil", reminderPajak.multiply(tarif[index]).divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP));
                }else{
                    bTarif.put("_pkp", reminderPajak);
                    bTarif.put("_hasil", reminderPajak.multiply(tarif[index]));
                }

                listPph21.add(bTarif);

                reminderPajak = layer[index + 1];

                hitungPajak(reminderPajak,pkp,index + 1,layerIndex,tarifIndex,rutin);
            }else{
                this.index = index;
                this.reminderPajak = new BigDecimal("0.00");

                BasicDBObject bTarif = new BasicDBObject();
                bTarif.put("_tarif",tarif[index]);
                bTarif.put("_pkp",pkp);
                bTarif.put("_hasil", pkp.multiply(tarif[index]));

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
            bTarif.put("_tarif",tarif[index]);

            if(rutin) {
                bTarif.put("_pkp", pkp.divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP));
                bTarif.put("_hasil", pkp.multiply(tarif[index]).divide(new BigDecimal("12.00"), 2, BigDecimal.ROUND_HALF_UP));
            }else{
                bTarif.put("_pkp", pkp);
                bTarif.put("_hasil", pkp.multiply(tarif[index]));
            }

            listPph21.add(bTarif);
        }
    }

    public BasicDBList getListPph21() {
        return listPph21;
    }

    public Integer getIndex() {
        return index;
    }
}
