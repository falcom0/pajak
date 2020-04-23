package usu.pajak.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.query.Query;
import usu.pajak.fariz.model.*;
import usu.pajak.fariz.service.MongoDb;
import usu.pajak.fariz.service.StaticValue;
import usu.pajak.model.UserPajak;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserPajakService {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_server")); //connect to mongodb
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_server");
    private Datastore datastore2 = new Morphia().mapPackage("usu.pajak.fariz.model.UserPajak").createDatastore(new MongoClient(new MongoClientURI("mongodb://localhost:27017")),"r1_pajak2019");
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

    public static void main(String[] args) throws IOException {
//        new UserPajakService().star();
        new UserPajakService().test();
    }

    public void star(){
//        Query<RekapPph> queryRekap = datastore2.createQuery(RekapPph.class).disableValidation().filter("_id","1844");
////        queryRekap.criteria("_id").equalIgnoreCase("1515");
//        RekapPph rekap = queryRekap.get();
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase mongoDatabase = mongoClient.getDatabase("r1_pajak2019");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("TotalPPh21");
        FindIterable<Document> findIterable = mongoCollection.find();
        findIterable.filter(new BsonDocument().append("_id",new BsonString("1515")));
        findIterable.first();
        System.out.println("here");
    }

    private Integer[] idExpenseTetap = new Integer[]{1449,1450,1451,1452,1454,1455,1456,1457,1459,1460,1461,1462,1464,1465,1466,1467,1469,1470,1471,1472,1475,1476,1477,1479,1480,1481,1483,1484,1485,1486,1488,1489,1490,1491,1493,1494,1495,1496,1498,1499,1500,1501,1503,1504,1505,1506,1508,1509,1510,1511,1513,1514,1515,1516,1518,1519,1520,1521,1523,1524,1525,1526,1528,1529,1530,1531,1533,1534,1535,1536,1538,1539,1540,1541,1543,1544,1545,1546,1548,1549,1550,1551,2849,2850,2851,2852,2854,2855,2856,2857,2589,2590,2591,2592,2584,2585,2586,2587,2539,2540,2541,2542};
    private Integer[] idExpenseTdkTetap = new Integer[]{1555,1556,1557,1558,1560,1561,1562,1563,1565,1566,1567,1568,1570,1571,1572,1573,1575,1576,1577,1578,1580,1581,1582,1583,1585,1586,1587,1588,1590,1591,1592,1593,1595,1596,1597,1598,1600,1601,1602,1603,1605,1606,1607,1608,1610,1611,1612,1613,1615,1616,1617,1618,1620,1621,1622,1623,1625,1626,1627,1628,1630,1631,1632,1633,1635,1636,1637,1638,1640,1641,1642,1643,1645,1646,1647,1648,1650,1651,1652,1653,1655,1656,1657,1658,1660,1661,1662,1663,1665,1666,1667,1668,1670,1671,1672,1673,1675,1676,1677,1678,1680,1681,1682,1683,1685,1686,1687,1688,1690,1691,1692,1693,1695,1696,1697,1698,1700,1701,1702,1703,1705,1706,1707,1708,1710,1711,1712,1713,1715,1716,1717,1718,1720,1721,1722,1723,1725,1726,1727,1728,1730,1731,1732,1733,1735,1736,1737,1738,1740,1741,1742,1743,1745,1746,1747,1748,1750,1751,1752,1753,1755,1756,1757,1758,1760,1761,1762,1763,1765,1766,1767,1768,1770,1771,1772,1773,1775,1776,1777,1778,1780,1781,1782,1783,1785,1786,1787,1788,1790,1791,1792,1793,1795,1796,1797,1798,1800,1801,1802,1803,1805,1806,1807,1808,1811,1812,1813,1814,1816,1817,1818,1819,1821,1822,1823,1824,1826,1827,1828,1829,1831,1832,1833,1834,1836,1837,1838,1839,1841,1842,1843,1844,1846,1847,1848,1849,1851,1852,1853,1854,1856,1857,1858,1859,1861,1862,1863,1864,1866,1867,1868,1869,1871,1872,1873,1874,1877,1878,1879,1880,1882,1883,1884,1885,1888,1889,1890,1891,1893,1894,1895,1896,1898,1899,1900,1901,1904,1905,1906,1907,1909,1910,1911,1912,1914,1915,1916,1917,1919,1920,1921,1922,1924,1925,1926,1927,1929,1930,1931,1932,1934,1935,1936,1937,1939,1940,1941,1942,1553,2420, 2421,1948, 1972,2172 };
    private Integer[] idPns = new Integer[]{0,1,6};
//    private Integer[] idNonPns = new Integer[]{};
    private Integer[] idGroupOrgLuar = new Integer[]{3,6,8,9,10,11,13,14};

    public void test() throws IOException {
        long start = System.currentTimeMillis();
//        JsonArray data = new Gson().fromJson(ApiRka.callApiUsu("https://api.usu.ac.id/0.1/users/","GET"), JsonObject.class).getAsJsonArray("data");
//        List<JsonObject> listTetap = new ArrayList<>();
//        List<JsonObject> listTetapJasaMedis = new ArrayList<>();
//        List<JsonObject> listTdkTetap = new ArrayList<>();
//        List<JsonObject> listTdkTetapJasaMedis = new ArrayList<>();
//        List<JsonObject> listTdkJelas = new ArrayList<>();
//        List<JsonObject> listOrgLuar = new ArrayList<>();
        HashMap<Integer, String> listNoBuktiPotong = new HashMap<>();
        Query<usu.pajak.fariz.model.UserPajak> query = datastore2.createQuery(usu.pajak.fariz.model.UserPajak.class).disableValidation();
        int counter = 1;
        for(int month = 1;month <= 12; month++) {
            JsonArray data = new Gson().fromJson(ApiRka.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?month="+month+"&year=2019&status=1", "GET"), JsonObject.class).getAsJsonObject("response").getAsJsonArray("salary_receivers");
            System.out.println(data.size());
            for (int i = 0; i < data.size(); i++) {
                JsonObject jo = data.get(i).getAsJsonObject();
                if (!jo.get("user").getAsJsonObject().get("id").isJsonNull()
                        && !Arrays.stream(idGroupOrgLuar)
                        .anyMatch(e -> e.intValue() == jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt())) {
                    Integer idUser = jo.get("user").getAsJsonObject().get("id").getAsInt();
//                    JsonObject payment = jo.get("payment").getAsJsonObject();
//                    Integer idExpense = payment.get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt();
//                    BigInteger pph21 = payment.get("pph21").getAsBigInteger();
//                    BigInteger returned = payment.get("returned").getAsBigInteger();
//                    BigInteger totalPendapatan = new BigInteger(String.valueOf(payment.entrySet().stream()
//                            .filter(row -> {
//                                if (row.getValue().isJsonObject()) return false;
//                                else if (row.getValue().isJsonPrimitive())
//                                    if (row.getValue().getAsJsonPrimitive().isNumber() && !(row.getKey().contains("pph21")))
//                                        return true;
//                                    else return false;
//                                else return false;
//                            })
//                            .mapToInt(row -> {
//                                if (!row.getKey().equalsIgnoreCase("returned"))
//                                    return row.getValue().getAsInt();
//                                else
//                                    return row.getValue().getAsInt() * -1;
//                            }).sum()));
                    String noBuktiPotong = "";
                    if (listNoBuktiPotong.size() > 0) {
                        if (!listNoBuktiPotong.entrySet().stream().anyMatch(e -> e.getKey() == idUser)) {
                            noBuktiPotong = String.format("%08d", counter);
                            listNoBuktiPotong.put(idUser, noBuktiPotong);
//                            usu.pajak.fariz.model.UserPajak up = query.filter("id_user", idUser.toString()).get();

//                            up.setNo_bukti_potong(noBuktiPotong);
                            Nobuk nobuk = new Nobuk();
                            nobuk.setId_user(idUser.toString());
                            nobuk.setNo_bukti_potong(noBuktiPotong);
                            datastore2.save(nobuk);
                            counter++;
                        } else {
                            noBuktiPotong = listNoBuktiPotong.get(idUser);
                        }
                    } else {
                        noBuktiPotong = String.format("%08d", counter);
                        listNoBuktiPotong.put(idUser, noBuktiPotong);
                        Nobuk nobuk = new Nobuk();
                        nobuk.setId_user(idUser.toString());
                        nobuk.setNo_bukti_potong(noBuktiPotong);

//                        usu.pajak.fariz.model.UserPajak up = query.filter("id_user", idUser.toString()).get();
//                        up.setNo_bukti_potong(noBuktiPotong);
                        datastore2.save(nobuk);
                        counter++;
                    }
//                    if (Arrays.stream(idExpenseTetap).anyMatch(e -> e.intValue() == idExpense)) {
//                        JsonObject joTetap = new JsonObject();
//                        joTetap.add("user", jo.get("user").getAsJsonObject());
//                        joTetap.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
//                        joTetap.addProperty("pengembalian", returned);
//                        joTetap.addProperty("pph21", pph21);
//                        joTetap.addProperty("no_bukti_potong", noBuktiPotong);
//                        joTetap.addProperty("bulan", month);
//                        joTetap.add("unit",jo.get("unit").getAsJsonObject());
//                        joTetap.add("salary_receive", jo);
//                        listTetap.add(joTetap);
//                    } else if (Arrays.stream(idExpenseTdkTetap).anyMatch(e -> e.intValue() == idExpense)) {
//                        JsonObject joTdkTetap = new JsonObject();
//                        joTdkTetap.add("user", jo.get("user").getAsJsonObject());
//                        joTdkTetap.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
//                        joTdkTetap.addProperty("pengembalian", returned);
//                        joTdkTetap.addProperty("pph21", pph21);
//                        joTdkTetap.addProperty("no_bukti_potong", noBuktiPotong);
//                        joTdkTetap.addProperty("bulan", month);
//                        joTdkTetap.add("unit",jo.get("unit").getAsJsonObject());
//                        joTdkTetap.add("salary_receive", jo);
//                        if(jo.get("payment").getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==49 &&
//                                (jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 0 ||
//                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 2 ||
//                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 4
//                                )
//                        )
//                            listTdkTetapJasaMedis.add(joTdkTetap);
//                        else if((jo.get("payment").getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==49||jo.get("payment").getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==50) &&
//                                (jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 1 ||
//                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 5 ||
//                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 7
//                                )
//                        )
//                            listTetapJasaMedis.add(joTdkTetap);
//                        else
//                            listTdkTetap.add(joTdkTetap);
//                    } else {
//                        JsonObject joTdkJelas = new JsonObject();
//                        joTdkJelas.add("user", jo.get("user").getAsJsonObject());
//                        joTdkJelas.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
//                        joTdkJelas.addProperty("pengembalian", returned);
//                        joTdkJelas.addProperty("pph21", pph21);
//                        joTdkJelas.addProperty("no_bukti_potong", noBuktiPotong);
//                        joTdkJelas.add("salary_receive", jo);
//                        listTdkJelas.add(joTdkJelas);
//                    }
                } else {
//                    JsonObject payment = jo.get("payment").getAsJsonObject();
//                    Integer idExpense = payment.get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt();
//                    BigInteger pph21 = payment.get("pph21").getAsBigInteger();
//                    BigInteger returned = payment.get("returned").getAsBigInteger();
//                    BigInteger totalPendapatan = new BigInteger(String.valueOf(payment.entrySet().stream()
//                            .filter(row -> {
//                                if (row.getValue().isJsonObject()) return false;
//                                else if (row.getValue().isJsonPrimitive())
//                                    if (row.getValue().getAsJsonPrimitive().isNumber() && !(row.getKey().contains("pph21")))
//                                        return true;
//                                    else return false;
//                                else return false;
//                            })
//                            .mapToInt(row -> {
//                                if (!row.getKey().equalsIgnoreCase("returned"))
//                                    return row.getValue().getAsInt();
//                                else
//                                    return row.getValue().getAsInt() * -1;
//                            }).sum()));
//                    JsonObject joOrgLuar = new JsonObject();
//                    joOrgLuar.add("user", jo.get("user").getAsJsonObject());
//                    joOrgLuar.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
//                    joOrgLuar.addProperty("pengembalian", returned);
//                    joOrgLuar.addProperty("pph21", pph21);
//                    joOrgLuar.add("salary_receive", jo);
////                joTetap.addProperty("no_bukti_potong", noBuktiPotong);
//                    listOrgLuar.add(joOrgLuar);
                }
            }
        }
//
//        Map<Integer, List<JsonObject>> expenseIdTdkTau = listTdkJelas.stream().collect(
//                Collectors.groupingBy(
//                        e -> e.get("salary_receive").getAsJsonObject()
//                                    .get("payment").getAsJsonObject()
//                                    .get("activity").getAsJsonObject()
//                                    .get("expense_account").getAsJsonObject()
//                                .get("id").getAsInt()
//                )
//        );
////        List<JsonObject> sortedList = list.stream().sorted(Comparator.comparing(e -> e.get("id").getAsInt())).collect(Collectors.toList());
//
//        Map<Integer, Map<Integer, Map<Integer, List<JsonObject>>>> groupListTetap = listTetap.stream().collect(
//                Collectors.groupingBy(
//                        e -> e.get("bulan").getAsInt(), Collectors.groupingBy(
//                                s -> s.get("unit").getAsJsonObject().get("id").getAsInt(), Collectors.groupingBy(
//                                        d -> d.get("user").getAsJsonObject().get("id").getAsInt()
//                                )
//                        )
//                )
//        );


        long end = System.currentTimeMillis();
        System.out.println((end - start)+" ms.");
        System.out.println("break");
    }

    public List<BuktiPotong> getBuktiPotong(String userId, String type){
        List<BuktiPotong> buktiPotongList = new ArrayList<>();
        BuktiPotong pegawai = new BuktiPotong();
        //TODO: No. bukti potong
        String response = null;
        try {
            response = ApiRka.callApiUsu("https://api.usu.ac.id/0.1/users/"+userId+"?fieldset=structural,functional,rank","GET");
            System.out.println("user_service");
            JsonObject jsonObject = new Gson().fromJson(response,JsonObject.class).getAsJsonObject("data");
            String npwp = jsonObject.get("npwp").getAsString();
//            String removeCharNpwp = npwp.replaceAll(".","").replaceAll("-","").replaceAll(" ","");

            pegawai.setA_01(jsonObject.get("npwp").getAsString());
            pegawai.setA_02(jsonObject.get("nip").getAsString());
            pegawai.setA_03(jsonObject.get("full_name").getAsString());
            final String[] alamat = {""};
            jsonObject.get("address").getAsJsonObject().entrySet().forEach(e->{
                alamat[0] = alamat[0].concat(e.getValue().getAsString()+", ");
            });
            pegawai.setA_04(alamat[0].substring(0, alamat[0].length()-2));
            String gender = jsonObject.get("gender").getAsString();
            switch (gender){
                case "Pria":
                    pegawai.setA_05(true);
                    break;
                case "Wanita":
                    pegawai.setA_06(true);
                    break;
            }

            String jabatan = "Pegawai";
            if(!jsonObject.get("structurals").isJsonNull())
                if(jsonObject.get("structurals").getAsJsonArray().size() > 0)
                    jabatan = jsonObject.get("structurals").getAsJsonArray().get(0).getAsJsonObject().get("position").getAsJsonObject().get("title").getAsString();
            pegawai.setA_10(jabatan);

        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (type){
            case "A1":
                Query<usu.pajak.fariz.model.UserPajak> query1 = datastore2.createQuery(usu.pajak.fariz.model.UserPajak.class).disableValidation();
                query1.filter("id_user",userId);
                usu.pajak.fariz.model.UserPajak userPajak1 = query1.get();
                pegawai.setH_01(datastore2.createQuery(Nobuk.class).disableValidation().filter("id_user",userId).get().getNo_bukti_potong());
                if(Arrays.stream(idPns).anyMatch(e -> e == userPajak1.getGroup().getId())){
                    List<PendapatanTetaps> pendapatanTetapsList = datastore2.createQuery(PendapatanTetaps.class).disableValidation().filter("id_user",userId).asList();
                    BigDecimal biayaJabatanApbn = pendapatanTetapsList.stream()
                    .map(e -> e.getPajak().getBiaya_jabatan()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalBiayaJabatan = userPajak1.getTotal_pendapatan().getBiaya_jabatan_setahun();
                    BigDecimal biayaJabatanA1 = totalBiayaJabatan.subtract(biayaJabatanApbn);
                    pegawai.setB_09(biayaJabatanA1.toBigInteger());
                    pegawai.setB_11(biayaJabatanA1.toBigInteger());

                    BigDecimal netto_setahun_apbn = pendapatanTetapsList.stream().map(e -> e.getPajak().getNetto_pendapatan()).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal netto_A1_setahun = userPajak1.getTotal_pendapatan().getNetto_pendapatan_setahun();
                    BigInteger totalBruto = netto_A1_setahun.subtract(netto_setahun_apbn).add(biayaJabatanA1).toBigInteger();
//                    BigInteger totalBruto = netto_pendapatan_setahun_db - netto_pendapatan_apbn + (biaya jabatan = 6000000 - biaya_jabatan_apbn)
                    pegawai.setB_03(totalBruto);
                    pegawai.setB_08(totalBruto);

                    BigInteger totalNetto = totalBruto.subtract(biayaJabatanA1.toBigInteger());
                    pegawai.setB_12(totalNetto);
                    pegawai.setB_14(totalNetto);

                    BigInteger ptkp = userPajak1.getTotal_pendapatan().getPtkp_setahun().toBigInteger();
                    pegawai.setB_15(ptkp);

                    switch (ptkp.intValue()){
                        case 72000000:
                            pegawai.setA_07(3);
                            break;
                        case 67500000:
                            pegawai.setA_07(2);
                            break;
                        case 63000000:
                            pegawai.setA_07(1);
                            break;
                        case 58500000:
                            pegawai.setA_07(0);
                            break;
                        case 54000000:
                            pegawai.setA_08(0);
                            break;
                    }

                    BigInteger totalPkp = totalNetto.subtract(ptkp);
                    pegawai.setB_16(totalPkp);

                    BigInteger totalPph21 = userPajak1.getPph21().getUsu().toBigInteger();
                    pegawai.setB_17(totalPph21);
                    pegawai.setB_19(totalPph21);
                    pegawai.setB_20(totalPph21);
                }else{ // NON PNS
                    BigDecimal biayaJabatanA1 = userPajak1.getTotal_pendapatan().getBiaya_jabatan_setahun();
                    pegawai.setB_09(biayaJabatanA1.toBigInteger());
                    pegawai.setB_11(biayaJabatanA1.toBigInteger());

                    BigInteger totalBruto = userPajak1.getTotal_pendapatan().getNetto_pendapatan_setahun().add(biayaJabatanA1).toBigInteger();
                    pegawai.setB_01(totalBruto);
                    pegawai.setB_08(totalBruto);

                    BigInteger totalNetto = totalBruto.subtract(biayaJabatanA1.toBigInteger());
                    pegawai.setB_12(totalNetto);
                    pegawai.setB_14(totalNetto);

                    BigInteger ptkp = userPajak1.getTotal_pendapatan().getPtkp_setahun().toBigInteger();
                    pegawai.setB_15(ptkp);

                    switch (ptkp.intValue()){
                        case 72000000:
                            pegawai.setA_07(3);
                            break;
                        case 67500000:
                            pegawai.setA_07(2);
                            break;
                        case 63000000:
                            pegawai.setA_07(1);
                            break;
                        case 58500000:
                            pegawai.setA_07(0);
                            break;
                        case 54000000:
                            pegawai.setA_08(0);
                            break;
                    }

                    BigInteger totalPkp = totalNetto.subtract(ptkp);
                    if(totalPkp.compareTo(BigInteger.ZERO) > 0)
                        pegawai.setB_16(totalPkp);
                    else pegawai.setB_16(BigInteger.ZERO);

                    BigInteger totalPph21 = userPajak1.getPph21().getUsu().toBigInteger();
                    pegawai.setB_17(totalPph21);
                    pegawai.setB_19(totalPph21);
                    pegawai.setB_20(totalPph21);
                }
                buktiPotongList.add(pegawai);
                break;
            case "NON-FINAL":
                Query<usu.pajak.fariz.model.UserPajak> query = datastore2.createQuery(usu.pajak.fariz.model.UserPajak.class).disableValidation();
//                query.field("id_user").equalIgnoreCase(userId);
                query.filter("id_user",userId);
//                query.criteria("id_user").equalIgnoreCase(userId);
                usu.pajak.fariz.model.UserPajak userPajak = query.get();
                pegawai.setH_01(datastore2.createQuery(Nobuk.class).disableValidation().filter("id_user",userId).get().getNo_bukti_potong());
                if(userPajak.getNpwp()==null){
                    if(userPajak.getNpwp_simsdm()==null)
                        pegawai.setTdk_npwp(true);
                }

//                Query<RekapPph> queryRekap = datastore2.createQuery(RekapPph.class).disableValidation();
//                queryRekap.criteria("_id").equalIgnoreCase(userId);
//                RekapPph rekap = queryRekap.get();
                MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
                MongoDatabase mongoDatabase = mongoClient.getDatabase("r1_pajak2019");
                MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("TotalPPh21");
                FindIterable<Document> findIterable = mongoCollection.find();
                findIterable.filter(new BsonDocument().append("_id",new BsonString(userId)));
                Document doc = findIterable.first();
//                totalPph21Rka = doc.get("total_pph21_rka");
                Decimal128 usu = (Decimal128) doc.get("usu");
//                if(doc.get("jasa").equals(Integer.class))
//                doc.get("jasa"
                Decimal128 jasa = new Decimal128(0);
                try{
                    Integer jasaI = doc.getInteger("jasa");
                }catch (ClassCastException e){
                    jasa = (Decimal128) doc.get("jasa");
                }finally {

                }

                JsonObject jsonObject = new Gson().fromJson(doc.toJson(), JsonObject.class);

                if(userPajak.getTotal_pendapatan().getTotal_pkp_jasa()!=null){
                    if(userPajak.getTotal_pendapatan().getTotal_pkp_kegiatan()!=null){
                        // ada jasa medis dan kegiatan
                        pegawai.setKode_pajak("07");
                        BigDecimal bruto = userPajak.getTotal_pendapatan().getBruto_jasa_setahun();
                        pegawai.setBruto(bruto.toBigInteger());
                        pegawai.setDpp(bruto.multiply(new BigDecimal("0.5")).toBigInteger());
                        switch (userPajak.getSetting_pajak().getIndex_jasmed()){
                            case 0:
                                pegawai.setTarif(5);
                                break;
                            case 1:
                                pegawai.setTarif(15);
                                break;
                            case 2:
                                pegawai.setTarif(25);
                                break;
                            case 3:
                                pegawai.setTarif(30);
                                break;
                        }
                        BigInteger pph21 = jsonObject.get("total_pph21_rka").getAsBigInteger().subtract(usu.bigDecimalValue().toBigInteger());

                        pegawai.setPph_dipotong(jasa.bigDecimalValue().toBigInteger());
                        buktiPotongList.add(pegawai);

                        BuktiPotong pegawai2 = new BuktiPotong();
                        pegawai2.setKode_pajak("13");
                        pegawai2.setH_01(pegawai.getH_01());
                        pegawai2.setA_01(pegawai.getA_01());
                        pegawai2.setA_02(pegawai.getA_02());
                        pegawai2.setA_03(pegawai.getA_03());
                        pegawai2.setA_04(pegawai.getA_04());
                        pegawai2.setA_05(pegawai.getA_05());
                        pegawai2.setA_06(pegawai.getA_06());
                        pegawai2.setA_10(pegawai.getA_10());
                        BigDecimal bruto2 = userPajak.getTotal_pendapatan().getBruto_kegiatan_setahun();
                        pegawai2.setBruto(bruto2.toBigInteger());
                        pegawai2.setDpp(bruto2.toBigInteger());
                        switch (userPajak.getSetting_pajak().getIndex_kegiatan()){
                            case 0:
                                pegawai2.setTarif(5);
                                break;
                            case 1:
                                pegawai2.setTarif(15);
                                break;
                            case 2:
                                pegawai2.setTarif(25);
                                break;
                            case 3:
                                pegawai2.setTarif(30);
                                break;
                        }
//                    DBCollection rekap = datastore2.getCollection(RekapPph.class);
//                    rekap  = rekap.getCollection("TotalPPh21").fin;
                        BigInteger pph212 = pph21.subtract(jasa.bigDecimalValue().toBigInteger());
                        pegawai2.setPph_dipotong(pph212);
                        buktiPotongList.add(pegawai2);
                    }else{
                        pegawai.setKode_pajak("07");
                        BigDecimal bruto = userPajak.getTotal_pendapatan().getBruto_jasa_setahun();
                        pegawai.setBruto(bruto.toBigInteger());
                        pegawai.setDpp(bruto.multiply(new BigDecimal("0.5")).toBigInteger());
                        switch (userPajak.getSetting_pajak().getIndex_jasmed()){
                            case 0:
                                pegawai.setTarif(5);
                                break;
                            case 1:
                                pegawai.setTarif(15);
                                break;
                            case 2:
                                pegawai.setTarif(25);
                                break;
                            case 3:
                                pegawai.setTarif(30);
                                break;
                        }
                        BigInteger pph21 = jsonObject.get("total_pph21_rka").getAsBigInteger().subtract(usu.bigDecimalValue().toBigInteger());
                        pegawai.setPph_dipotong(pph21);
                        buktiPotongList.add(pegawai);
                    }
                }else{
                    pegawai.setKode_pajak("13");
                    BigDecimal bruto = userPajak.getTotal_pendapatan().getBruto_kegiatan_setahun();
                    pegawai.setBruto(bruto.toBigInteger());
                    pegawai.setDpp(bruto.toBigInteger());
                    switch (userPajak.getSetting_pajak().getIndex_kegiatan()){
                        case 0:
                            pegawai.setTarif(5);
                            break;
                        case 1:
                            pegawai.setTarif(15);
                            break;
                        case 2:
                            pegawai.setTarif(25);
                            break;
                        case 3:
                            pegawai.setTarif(30);
                            break;
                    }
//                    DBCollection rekap = datastore2.getCollection(RekapPph.class);
//                    rekap  = rekap.getCollection("TotalPPh21").fin;
                    BigInteger pph21 = jsonObject.get("total_pph21_rka").getAsBigInteger().subtract(usu.bigDecimalValue().toBigInteger());
                    pegawai.setPph_dipotong(pph21);
                    buktiPotongList.add(pegawai);
                }
                break;
        }
        return buktiPotongList;
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

@Entity(value="bukti_potong")
class Nobuk {
    @Indexed
    private String id_user;
    private String no_bukti_potong;

    public void setNo_bukti_potong(String no_bukti_potong) {
        this.no_bukti_potong = no_bukti_potong;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getNo_bukti_potong() {
        return no_bukti_potong;
    }

    public String getId_user() {
        return id_user;
    }
}