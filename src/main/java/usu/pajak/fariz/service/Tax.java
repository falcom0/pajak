package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.bson.types.Decimal128;
import usu.pajak.fariz.model.*;
import usu.pajak.services.ApiRka;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tax {
    private static CellStyle currency;
    private Datastore datastore,ds2019;
    private JsonArray jsonArray;
    public static final int REQUEST  = 0;
    public static final int SALARY = 1;
    private Integer[] idExpenseTetapP1P2 = new Integer[]{1449,1450,1451,1452,1454,1455,1456,1457,1459,1460,1461,1462,1464,1465,1466,1467};
//    private Integer[] idExpenseTetap = new Integer[]{1449,1450,1451,1452,1454,1455,1456,1457,1459,1460,1461,1462,1464,1465,1466,1467,1469,1470,1471,1472,1475,1476,1477,1479,1480,1481,1483,1484,1485,1486,1488,1489,1490,1491,1493,1494,1495,1496,1498,1499,1500,1501,1503,1504,1505,1506,1508,1509,1510,1511,1513,1514,1515,1516,1518,1519,1520,1521,1523,1524,1525,1526,1528,1529,1530,1531,1533,1534,1535,1536,1538,1539,1540,1541,1543,1544,1545,1546,1548,1549,1550,1551,2849,2850,2851,2852,2854,2855,2856,2857,2589,2590,2591,2592,2584,2585,2586,2587,2539,2540,2541,2542};
    private Integer[] idExpenseTetap = new Integer[]{1475,1476,1477,1479,1480,1481,1483,1484,1485,1486,1488,1489,1490,1491,1493,1494,1495,1496,1498,1499,1500,1501,1503,1504,1505,1506,1508,1509,1510,1511,1528,1529,1530,1531,1533,1534,1535,1536,1538,1539,1540,1541,1543,1544,1545,1546,1548,1549,1550,1551,2849,2850,2851,2852,2854,2855,2856,2857};
    private Integer[] idExpenseTdkTetap = new Integer[]{1555,1556,1557,1558,1560,1561,1562,1563,1565,1566,1567,1568,1570,1571,1572,1573,1575,1576,1577,1578,1580,1581,1582,1583,1585,1586,1587,1588,1590,1591,1592,1593,1595,1596,1597,1598,1600,1601,1602,1603,1605,1606,1607,1608,1610,1611,1612,1613,1615,1616,1617,1618,1620,1621,1622,1623,1625,1626,1627,1628,1630,1631,1632,1633,1635,1636,1637,1638,1640,1641,1642,1643,1645,1646,1647,1648,1650,1651,1652,1653,1655,1656,1657,1658,1660,1661,1662,1663,1665,1666,1667,1668,1670,1671,1672,1673,1675,1676,1677,1678,1680,1681,1682,1683,1685,1686,1687,1688,1690,1691,1692,1693,1695,1696,1697,1698,1700,1701,1702,1703,1705,1706,1707,1708,1710,1711,1712,1713,1715,1716,1717,1718,1720,1721,1722,1723,1725,1726,1727,1728,1730,1731,1732,1733,1735,1736,1737,1738,1740,1741,1742,1743,1745,1746,1747,1748,1750,1751,1752,1753,1755,1756,1757,1758,1760,1761,1762,1763,1765,1766,1767,1768,1770,1771,1772,1773,1775,1776,1777,1778,1780,1781,1782,1783,1785,1786,1787,1788,1790,1791,1792,1793,1795,1796,1797,1798,1800,1801,1802,1803,1805,1806,1807,1808,1811,1812,1813,1814,1816,1817,1818,1819,1821,1822,1823,1824,1826,1827,1828,1829,1831,1832,1833,1834,1836,1837,1838,1839,1841,1842,1843,1844,1846,1847,1848,1849,1851,1852,1853,1854,1856,1857,1858,1859,1861,1862,1863,1864,1866,1867,1868,1869,1871,1872,1873,1874,1877,1878,1879,1880,1882,1883,1884,1885,1888,1889,1890,1891,1893,1894,1895,1896,1898,1899,1900,1901,1904,1905,1906,1907,1909,1910,1911,1912,1914,1915,1916,1917,1919,1920,1921,1922,1924,1925,1926,1927,1929,1930,1931,1932,1934,1935,1936,1937,1939,1940,1941,1942,1553,2420, 2421,1948, 1972,2172 };
    private Integer[] idGroupOrgLuar = new Integer[]{3,6,8,9,10,11,13,14};

    public static void main(String[] args) throws IOException {

//        new Tax().printToExcel();
//        Tax tax = new Tax();
//        tax.getListTax("1","9",false, "NON-PNBP");
//        new PnsApbn();
//        new Tax("fariz");
        //request_id=13763
//        System.out.println(Ptkp.getInstance.getPtkp("3654"));
        //Dekan
        Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&user_id=775&year=2020", "GET"), Salary.class);
        new Tax(salary);
        //Wakil Dekan
        Salary salary1 = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&user_id=790&year=2020", "GET"), Salary.class);
        new Tax(salary1);
//        for(int i=2; i<7;i++) {
//            Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month="+i+"&year=2020", "GET"), Salary.class);
//            System.out.println("Bulan "+i);
//            new Tax(salary);
//        }
//        salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month=2&year=2020","GET"),Salary.class);
//        new Tax(salary);
//        salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month=3&year=2020","GET"),Salary.class);
//        new Tax(salary);
//        new PnsApbn();
//        for (int i=1;i<=12;i++) {
//            long start = System.currentTimeMillis();
//            Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month="+i, "GET"), Salary.class); //13763
//            long end = System.currentTimeMillis();
//            System.out.println("Api bulan "+i+" waktu :"+(end-start));
//            start = System.currentTimeMillis();
//            new Tax(salary);
//            end = System.currentTimeMillis();
//            System.out.println("Perhitungan bulan "+i+" waktu :"+(end-start));
//        }
//        new Tax().printToExcel();
//        long start = System.currentTimeMillis();
//        Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month="+12, "GET"), Salary.class); //13763
//        long end = System.currentTimeMillis();
//        System.out.println("Api bulan "+12+" waktu :"+(end-start));
//        start = System.currentTimeMillis();
//        new Tax(salary);
//        end = System.currentTimeMillis();
//        System.out.println("Perhitungan bulan "+12+" waktu :"+(end-start));
    }

    public JsonArray getJsonArray() {
        return jsonArray;
    }

    /*public void printToExcel() throws IOException {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase mongoDatabase = mongoClient.getDatabase("r11_pajak2019");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("TotalPPh21");
        FindIterable<Document> findIterable = mongoCollection.find();
        Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
        currency = workbook.createCellStyle();
        currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        Sheet sheet = workbook.createSheet("PPH21");
        Row row = sheet.createRow(0);
        final Cell[] cell = {row.createCell(0)};
        cell[0].setCellValue("No.");
        cell[0] = row.createCell(1);
        cell[0].setCellValue("id_user");
        cell[0] = row.createCell(2);
        cell[0].setCellValue("Nama");
        cell[0] = row.createCell(3);
        cell[0].setCellValue("Unit");
        cell[0] = row.createCell(4);
        cell[0].setCellValue("Total PKP");
        cell[0] = row.createCell(5);
        cell[0].setCellValue("Total PKP Jasa Medis");
        cell[0] = row.createCell(6);
        cell[0].setCellValue("Total PKP Kegiatan");
        cell[0] = row.createCell(7);
        cell[0].setCellValue("PPH 21 yang telah dibayar USU");
        cell[0] = row.createCell(8);
        cell[0].setCellValue("PPH 21 yang telah dibayar melalui APBN");
        cell[0] = row.createCell(9);
        cell[0].setCellValue("Total yang telah dibayar");
        cell[0] = row.createCell(10);
        cell[0].setCellValue("PPH 21 yang seharusnya (A1)");
        cell[0] = row.createCell(11);
        cell[0].setCellValue("PPH 21 yang seharusnya (Jasa Medis)");
        cell[0] = row.createCell(12);
        cell[0].setCellValue("PPH 21 yang seharusnya (Non Final)");
        cell[0] = row.createCell(13);
        cell[0].setCellValue("Total PPH 21 dari sistem");
        cell[0] = row.createCell(14);
        cell[0].setCellValue("Lebih / Kurang Bayar");
        final int[] i = {1};
        findIterable.forEach((Consumer<? super Document>) document -> {
            if(!document.getString("_id").contains("-") && !Arrays.stream(idUserLebihBayarDibawahPtkp).anyMatch(e -> Integer.parseInt(document.getString("_id")) == e)){
                Row r = sheet.createRow(i[0]);
                Cell c = r.createCell(0);
                c.setCellValue(i[0] +".");
                c = r.createCell(1);
                c.setCellValue(document.getString("_id"));
                c = r.createCell(2);
                Document up = (Document)document.get("user");
                c.setCellValue(up.getString("full_name"));
                c = r.createCell(3);
                try {
                    BasicDBObject bdo = BasicDBObject.parse(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.1/users/"+document.getString("_id"),"GET"));
                    BasicDBObject data = (BasicDBObject) bdo.get("data");
                    c.setCellValue(data.getString("work_unit"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                c = r.createCell(4);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                Document totPen = (Document)up.get("total_pendapatan");
                if(totPen.get("total_pkp") != null) {
                    Decimal128 total_pkp = (Decimal128) totPen.get("total_pkp");
                    c.setCellValue(total_pkp.doubleValue());
                }else{
                    c.setCellValue(0.0);
                }
                c = r.createCell(5);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(totPen.get("total_pkp_jasa") != null) {
                    Decimal128 total_pkp = (Decimal128)totPen.get("total_pkp_jasa");
                    c.setCellValue(total_pkp.doubleValue());
                }else
                    c.setCellValue(0.0);
                c = r.createCell(6);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(totPen.get("total_pkp_kegiatan") != null) {
                    Decimal128 total_pkp = (Decimal128)totPen.get("total_pkp_kegiatan");
                    c.setCellValue(total_pkp.doubleValue());
                }else
                    c.setCellValue(0.0);
                c = r.createCell(7);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                c.setCellValue(document.getInteger("total_pph21_rka"));
                c = r.createCell(8);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("pns") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("pns");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("pns") instanceof Integer){
                    Integer pkp = (Integer) document.get("pns");
                    c.setCellValue(pkp);
                }
                c = r.createCell(9);
                c.setCellType(CellType.NUMERIC);
                c.setCellStyle(currency);
                c.setCellFormula("SUM(H"+(i[0]+1)+":I"+(i[0]+1)+")");
                c = r.createCell(10);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("usu") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("usu");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("usu") instanceof Integer){
                    Integer pkp = (Integer) document.get("usu");
                    c.setCellValue(pkp);
                }
                c = r.createCell(11);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("jasa") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("jasa");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("jasa") instanceof Integer){
                    Integer pkp = (Integer) document.get("jasa");
                    c.setCellValue(pkp);
                }
                c = r.createCell(12);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("kegiatan") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("kegiatan");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("kegiatan") instanceof Integer){
                    Integer pkp = (Integer) document.get("kegiatan");
                    c.setCellValue(pkp);
                }
                c = r.createCell(13);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                c.setCellFormula("SUM(K"+(i[0]+1)+":M"+(i[0]+1)+")");
                c = r.createCell(14);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                c.setCellFormula("J"+(i[0]+1)+"-N"+(i[0]+1));
                i[0]++;
            }
        });
        try (OutputStream fileOut = new FileOutputStream("D:/[R11-A1]HASIL_PPH21_2019_exclude_dibawah_PTKP.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        System.out.println(mongoCollection.countDocuments());
    }*/

    public Tax(){
//        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"revisi_pajak");
//        ds2019 = new Morphia().createDatastore(
//                new MongoClient(new MongoClientURI("mongodb://"+ MongoDb.LOCAL)), "pajak_server");
    }

    public Tax(Salary salary){
//        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"r4_pajak2019");
        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"pajak_2020_Algo_2");
        jsonArray  = new JsonArray();
//        filterTax(salary);
        filterTax2020(salary);
    }

    private void filterTax2020(Salary salary){
        if(salary != null){
            if(salary.getResponse().getSalary_receivers().stream().anyMatch(Objects::nonNull)){
                List<SalaryDetail> allData = salary.getResponse().getSalary_receivers();

                try {
                    List<SalaryDetail> listDalam = allData.stream()
                            .filter(c -> !Arrays.stream(idGroupOrgLuar).anyMatch(e -> {
                                System.out.println("salary_id :"+c.getId());
                                System.out.println("e :"+e.intValue());
                                System.out.println("group id: "+c.getUser().getGroup().getId());

                                if(c.getUser().getGroup().getId() == null){
                                    return true;
                                }else {
                                    if (e.intValue() == c.getUser().getGroup().getId())
                                        return true;
                                    else
                                        return false;
                                }
                            }))
                            .collect(Collectors.toList());


                List<SalaryDetail> listDalamA1 = listDalam.stream()
                        .filter(c -> (Arrays.stream(idExpenseTetap).anyMatch(e -> e.intValue() == c.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt())))
                        .collect(Collectors.toList());
                List<SalaryDetail> listDalamNonFinal = listDalam.stream()
                        .filter(c -> Arrays.stream(idExpenseTdkTetap).anyMatch(e -> e.intValue() == c.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt()))
                        .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt() == 49))
                        .collect(Collectors.toList());
                List<SalaryDetail> listDalamJasaMedis = listDalam.stream()
                        .filter(c -> Arrays.stream(idExpenseTdkTetap).anyMatch(e -> e.intValue() == c.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt()))
                        .filter(c -> c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt() == 49)
                        .collect(Collectors.toList());

                List<SalaryDetail> listLuar = allData.stream()
                        .filter(c -> Arrays.stream(idGroupOrgLuar).anyMatch(e -> e.intValue() == c.getUser().getGroup().getId()))
                        .collect(Collectors.toList());

                List<SalaryDetail> listLuarTenagaAhli = listLuar.stream()
                        .filter(c -> c.getUnit().getId() != 1)
                        .collect(Collectors.toList());

                List<SalaryDetail> listLuarMwa = listLuar.stream()
                        .filter(c -> c.getUnit().getId() == 1)
                        .collect(Collectors.toList());

//                calculateTaxHonor(listDalamA1,"a1"); // [DALAM]A1 {Karena insentif pajak ini tidak di pakai}
                calculateTaxSalary(listDalamA1, "a1"); // Perhitungan pajak yg di setahunkan
//                calculateTaxMwa(listDalamNonFinal,false,"kegiatan"); // [DALAM]Non Final pegawai USU
                calculateTaxHonor(listDalamNonFinal,"kegiatan"); // [DALAM]Non Final pegawai USU
                calculateTaxJasmed(listDalamJasaMedis,"jasa"); // [DALAM]Jasa Medis Dokter di luar Rumah Sakit
                calculateTaxTa(listLuarTenagaAhli,"jasa"); // [LUAR]Tenaga Ahli
                calculateTaxMwa(listLuarMwa,true,"kegiatan");

                }catch (Exception e){
                    System.out.println(e.getCause());
                }
            }else{ }
        }else{ }
    }

    public void filterTax(Salary salary){
        if(salary != null){
            if(salary.getResponse().getSalary_receivers().stream().anyMatch(Objects::nonNull)){
                List<SalaryDetail> allData = salary.getResponse().getSalary_receivers();

                //Filter berdasarkan Luar / Dalam
                //1. Luar : Pisahkan : a. MWA b. Tenaga Ahli c. Dosen Luar Negri
                List<SalaryDetail> listLuar = allData.stream()
                        .filter( c -> c.getUser().getGroup().getId() == 8)
                        .filter( c -> c.getUser().getGroup().getId() == 9)
                        .filter( c -> c.getUser().getGroup().getId() == 10)
                        .filter( c -> c.getUser().getGroup().getId() == 11) //?
//                        .filter( c -> c.getUser().getGroup().getId() == 12)
                        .filter( c -> c.getUser().getGroup().getId() == 13)
                        .filter( c -> c.getUser().getGroup().getId() == 14)
                        .filter( c -> c.getUser().getGroup().getId() == 3)
                        .filter( c -> c.getUser().getGroup().getId() == 6)
                        .collect(Collectors.toList());
                List<SalaryDetail> listMwa = listLuar.stream()
                        .filter( r -> r.getUnit().getId() == 1)
                        .collect(Collectors.toList());
                List<SalaryDetail> listTA = listLuar.stream()
                        .filter( r -> r.getUnit().getId() != 1)
                        .collect(Collectors.toList());

                //2. Dalam : Pisahkan : a. Gaji Tetap b. Honor 08170081472 Up12desty
                List<SalaryDetail> listDalam = allData.stream()
                        .filter(c -> c.getUser().getId() != null)
                        .collect(Collectors.toList());
//                List<SalaryDetail> gaji = listDalam.stream()
//                        .filter(c ->
//                                (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId() != 0) &&
//                                        (c.getPayment().getAsJsonObject().has("basic_salary") ||
//                                c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23))
//                        .collect(Collectors.toList());

//                listDalam.stream().filter(c ->
//                        (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId()!=0) &&
//                                (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt() == 23
//                                        || c.getPayment().getAsJsonObject().has("basic_salary"))).collect(Collectors.toList())

//                List<SalaryDetail> honorPNS = listDalam.stream().filter(c -> c.getUser().getGroup().getId()==1 || c.getUser().getGroup().getId()==0).collect(Collectors.toList());
//
//                List<SalaryDetail> honorNonPNS = listDalam.stream()
//                        .filter(c -> (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId()!=0) && !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt() == 23 || c.getPayment().getAsJsonObject().has("basic_salary")))
//                        .collect(Collectors.toList());
//
//                List<SalaryDetail> honor = new ArrayList<>(honorPNS);
//                honor.addAll(honorNonPNS);

                //                        .filter(c -> !(c.getPayment().getAsJsonObject().has("basic_salary")))
//                        .filter(c -> (c.getUser().getGroup().getId()==1 || c.getUser().getGroup().getId() == 0) &&
//                                c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
//                        .filter(c -> (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId() != 0) &&
//                                !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
//                                && c.getPayment().getAsJsonObject().has("p1")))

                List<SalaryDetail> honorBknJasmed = listDalam.stream()
                        .filter(c -> !((c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 49))
                        )
                        .collect(Collectors.toList());

                List<SalaryDetail> honorJasmed = listDalam.stream()
                        .filter(c -> (c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 49)
                        )
                        .collect(Collectors.toList());


                if(listMwa.size() > 0){
                    calculateTaxMwa(listMwa,true,"kegiatan");
                }

                if(listTA.size() > 0){
                    calculateTaxTa(listTA,"jasa");
                }

//                if(gaji.size() > 0){
//                    calculateTaxSalary(gaji);
//                }

                if(honorBknJasmed.size() > 0){
                    calculateTaxHonor(honorBknJasmed,"kegiatan");
                }

                if(honorJasmed.size() > 0){
                    calculateTaxJasmed(honorJasmed,"jasa");
                }
            }else{ }
        }else{ }
    }

    public Tax(String fariz){
        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"r11_pajak2019");
        jsonArray  = new JsonArray();
        try {
            List<List<SalaryDetail>> test = pembetulanTax();
            calculateTaxHonor(test.get(0),"a1"); // [DALAM]A1
            calculateTaxMwa(test.get(1),false,"kegiatan"); // [DALAM]Non Final pegawai USU
//            calculateTaxHonor(test.get(1),"kegiatan"); // [DALAM]A1 KEGIATAN pegawai USU
            calculateTaxJasmed(test.get(2),"jasa"); // [DALAM]Jasa Medis Dokter di luar Rumah Sakit
            calculateTaxTa(test.get(3),"jasa"); // [LUAR]Tenaga Ahli
            calculateTaxMwa(test.get(4),true,"kegiatan"); // [LUAR]Komisaris atau MWA
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<List<SalaryDetail>> pembetulanTax() throws IOException {
        List<List<SalaryDetail>> result = new ArrayList<>();
        List<SalaryDetail> r_listTetap = new ArrayList<>();
        List<SalaryDetail> r_listTdkTetap = new ArrayList<>();
        List<SalaryDetail> r_listOrgLuar = new ArrayList<>();
        List<SalaryDetail> r_listOrgLuarMwa = new ArrayList<>();
        List<SalaryDetail> r_listTdkTetapJasaMedis = new ArrayList<>();
        List<JsonObject> listTetap = new ArrayList<>();
        List<JsonObject> listTetapJasaMedis = new ArrayList<>();
        List<JsonObject> listTdkTetapJasaMedis = new ArrayList<>();
        List<JsonObject> listTdkTetap = new ArrayList<>();
        List<JsonObject> listTdkJelas = new ArrayList<>();
        List<JsonObject> listOrgLuar = new ArrayList<>();
        List<JsonObject> listOrgLuarMWA = new ArrayList<>();
        HashMap<Integer, String> listNoBuktiPotong = new HashMap<>();
        int counter = 1;
        for(int month = 1;month <= 12; month++) {
            JsonArray data = new Gson().fromJson(ApiRka.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?month="+month+"&year=2019&status=1", "GET"), JsonObject.class).getAsJsonObject("response").getAsJsonArray("salary_receivers");
            for (int i = 0; i < data.size(); i++) {
                JsonObject jo = data.get(i).getAsJsonObject();
                if (!jo.get("user").getAsJsonObject().get("id").isJsonNull()
                        && !Arrays.stream(idGroupOrgLuar).anyMatch(e -> e.intValue() == jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt())) {
                    Integer idUser = jo.get("user").getAsJsonObject().get("id").getAsInt();
                    JsonObject payment = jo.get("payment").getAsJsonObject();
                    Integer idExpense = payment.get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt();
                    BigInteger pph21 = payment.get("pph21").getAsBigInteger();
                    BigInteger returned = payment.get("returned").getAsBigInteger();
                    BigInteger totalPendapatan = new BigInteger(String.valueOf(payment.entrySet().stream()
                            .filter(row -> {
                                if (row.getValue().isJsonObject()) return false;
                                else if (row.getValue().isJsonPrimitive())
                                    if (row.getValue().getAsJsonPrimitive().isNumber() && !(row.getKey().contains("pph21")))
                                        return true;
                                    else return false;
                                else return false;
                            })
                            .mapToInt(row -> {
                                if (!row.getKey().equalsIgnoreCase("returned"))
                                    return row.getValue().getAsInt();
                                else
                                    return row.getValue().getAsInt() * -1;
                            }).sum()));
                    String noBuktiPotong = "";
                    if (listNoBuktiPotong.size() > 0) {
                        if (!listNoBuktiPotong.entrySet().stream().anyMatch(e -> e.getKey() == idUser)) {
                            noBuktiPotong = String.format("%08d", counter);
                            listNoBuktiPotong.put(idUser, noBuktiPotong);
                            counter++;
                        } else {
                            noBuktiPotong = listNoBuktiPotong.get(idUser);
                        }
                    } else {
                        noBuktiPotong = String.format("%08d", counter);
                        listNoBuktiPotong.put(idUser, noBuktiPotong);
                        counter++;
                    }
                    if (Arrays.stream(idExpenseTetap).anyMatch(e -> e.intValue() == idExpense)) {
                        JsonObject joTetap = new JsonObject();
                        joTetap.add("user", jo.get("user").getAsJsonObject());
                        joTetap.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
                        joTetap.addProperty("pengembalian", returned);
                        joTetap.addProperty("pph21", pph21);
                        joTetap.addProperty("no_bukti_potong", noBuktiPotong);
                        joTetap.addProperty("bulan", month);
                        joTetap.add("unit",jo.get("unit").getAsJsonObject());
                        joTetap.add("salary_receive", jo);
                        listTetap.add(joTetap);
                        r_listTetap.add(new Gson().fromJson(jo, SalaryDetail.class));
                    } else if (Arrays.stream(idExpenseTdkTetap).anyMatch(e -> e.intValue() == idExpense)) {
                        JsonObject joTdkTetap = new JsonObject();
                        joTdkTetap.add("user", jo.get("user").getAsJsonObject());
                        joTdkTetap.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
                        joTdkTetap.addProperty("pengembalian", returned);
                        joTdkTetap.addProperty("pph21", pph21);
                        joTdkTetap.addProperty("no_bukti_potong", noBuktiPotong);
                        joTdkTetap.addProperty("bulan", month);
                        joTdkTetap.add("unit",jo.get("unit").getAsJsonObject());
                        joTdkTetap.add("salary_receive", jo);
                        if(jo.get("payment").getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==49 &&
                                (jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 0 ||
                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 2 ||
                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 4
                                )
                        ) {
                            listTdkTetapJasaMedis.add(joTdkTetap);
                            r_listTdkTetapJasaMedis.add(new Gson().fromJson(jo, SalaryDetail.class));
                        }else if((jo.get("payment").getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==49||jo.get("payment").getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==50) &&
                                (jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 1 ||
                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 5 ||
                                        jo.get("user").getAsJsonObject().get("group").getAsJsonObject().get("id").getAsInt() == 7
                                )
                        ) {
                            listTetapJasaMedis.add(joTdkTetap);
                            r_listTetap.add(new Gson().fromJson(jo, SalaryDetail.class));
                        }else {
                            listTdkTetap.add(joTdkTetap);
                            r_listTdkTetap.add(new Gson().fromJson(jo, SalaryDetail.class));
                        }
                    } else {
                        JsonObject joTdkJelas = new JsonObject();
                        joTdkJelas.add("user", jo.get("user").getAsJsonObject());
                        joTdkJelas.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
                        joTdkJelas.addProperty("pengembalian", returned);
                        joTdkJelas.addProperty("pph21", pph21);
                        joTdkJelas.addProperty("no_bukti_potong", noBuktiPotong);
                        joTdkJelas.add("salary_receive", jo);
                        listTdkJelas.add(joTdkJelas);
                    }
                } else {
                    JsonObject payment = jo.get("payment").getAsJsonObject();
                    Integer idExpense = payment.get("activity").getAsJsonObject().get("expense_account").getAsJsonObject().get("id").getAsInt();
                    BigInteger pph21 = payment.get("pph21").getAsBigInteger();
                    BigInteger returned = payment.get("returned").getAsBigInteger();
                    BigInteger totalPendapatan = new BigInteger(String.valueOf(payment.entrySet().stream()
                            .filter(row -> {
                                if (row.getValue().isJsonObject()) return false;
                                else if (row.getValue().isJsonPrimitive())
                                    if (row.getValue().getAsJsonPrimitive().isNumber() && !(row.getKey().contains("pph21")))
                                        return true;
                                    else return false;
                                else return false;
                            })
                            .mapToInt(row -> {
                                if (!row.getKey().equalsIgnoreCase("returned"))
                                    return row.getValue().getAsInt();
                                else
                                    return row.getValue().getAsInt() * -1;
                            }).sum()));
                    JsonObject joOrgLuar = new JsonObject();
                    joOrgLuar.add("user", jo.get("user").getAsJsonObject());
                    joOrgLuar.addProperty("bruto", totalPendapatan); // bruto setelah pengembalian / setelah dikurangi returned
                    joOrgLuar.addProperty("pengembalian", returned);
                    joOrgLuar.addProperty("pph21", pph21);
                    joOrgLuar.add("salary_receive", jo);
//                joTetap.addProperty("no_bukti_potong", noBuktiPotong);
                    if(jo.get("unit").getAsJsonObject().get("id").getAsInt() == 1){
                        // mwa
                        listOrgLuarMWA.add(joOrgLuar);
                        r_listOrgLuarMwa.add(new Gson().fromJson(jo, SalaryDetail.class));
                    }else {
                        listOrgLuar.add(joOrgLuar);
                        r_listOrgLuar.add(new Gson().fromJson(jo, SalaryDetail.class));
                    }
                }
            }
        }

        result.add(0, r_listTetap);
        result.add(1, r_listTdkTetap);
        result.add(2, r_listTdkTetapJasaMedis);
        result.add(3, r_listOrgLuar);
        result.add(4, r_listOrgLuarMwa);

        Map<Integer, List<JsonObject>> expenseIdTdkTau = listTdkJelas.stream().collect(
                Collectors.groupingBy(
                        e -> e.get("salary_receive").getAsJsonObject()
                                .get("payment").getAsJsonObject()
                                .get("activity").getAsJsonObject()
                                .get("expense_account").getAsJsonObject()
                                .get("id").getAsInt()
                )
        );
//        List<JsonObject> sortedList = list.stream().sorted(Comparator.comparing(e -> e.get("id").getAsInt())).collect(Collectors.toList());

        Map<Integer, Map<Integer, Map<Integer, List<JsonObject>>>> groupListTetap = listTetap.stream().collect(
                Collectors.groupingBy(
                        e -> e.get("bulan").getAsInt(), Collectors.groupingBy(
                                s -> s.get("unit").getAsJsonObject().get("id").getAsInt(), Collectors.groupingBy(
                                        d -> d.get("user").getAsJsonObject().get("id").getAsInt()
                                )
                        )
                )
        );

        return result;
    }

    private void calculateTaxMwa(List<SalaryDetail> salaryDetailList, boolean isLuar, String type){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = new UserPajak();
            if(isLuar)
                userPajak = getUserPajakLuar(sd,"MWA");
            else
                userPajak = getUserPajak(sd);

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
            qPt.and(qPt.criteria("id_user").contains(userPajak.getId_user()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = calculateModelKegiatanTax(sd,userPajak,BigDecimal.valueOf(1));
                pendapatanTdkTetaps.setType(type);
                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
            }else{//duplicate request
                PendapatanTdkTetaps ptt = qPt.first();
                ptt.setType(type);
                datastore.save(ptt);
                BigDecimal total_pph21_sementara = sumPPH21(ptt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private void calculateTaxTa(List<SalaryDetail> salaryDetailList,String type){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajakLuar(sd,"TA");

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
            qPt.and(qPt.criteria("id_user").contains(userPajak.getId_user()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = calculateModelJasaTax(sd,userPajak,BigDecimal.valueOf(0.5));
                pendapatanTdkTetaps.setType(type);
                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
            }else{//duplicate request
                PendapatanTdkTetaps ptt = qPt.first();
                ptt.setType(type);
                datastore.save(ptt);
                BigDecimal total_pph21_sementara = sumPPH21(ptt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private void calculateTaxSalary(List<SalaryDetail> salaryDetailList, String type){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTetaps> qPt = datastore.createQuery(PendapatanTetaps.class)
                    .disableValidation();
//                    .filter("id_user", sd.getUser().getId().toString());
            qPt.and(qPt.criteria("id_user").contains(sd.getUser().getId().toString()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){ //not exist (insert new data)
                PendapatanTetaps pendapatanTetaps = initializePendapatanTetap(sd, userPajak);
                pendapatanTetaps.setType(type);
                BigDecimal totalPendapatanSementara = getPendapatanSementara(sd);

                Pajak pajak = new Pajak();
                pajak.setTotal_pendapatan_rka(totalPendapatanSementara);
//                pajak.setJkk(StaticValue.jkk); Tidak jelas dari Biro Keuangan tidak ada
//                pajak.setJkm(StaticValue.jkm); Tidak jelas dari Biro Keuangan tidak ada
//                pajak.setBpjs_kesehatan(StaticValue.bpjs_kesehatan); Tidak jelas dari Biro Keuangan tidak ada

//                totalPendapatanSementara = totalPendapatanSementara.add(StaticValue.jkk).add(StaticValue.jkm).add(StaticValue.bpjs_kesehatan);
                pajak.setBruto_pendapatan(totalPendapatanSementara);

                BigDecimal biayaJabatan = getBiayaJabatan(totalPendapatanSementara,userPajak,pajak);

                BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(biayaJabatan);
                pajak.setNetto_pendapatan(nettoPendapatan);

                BigDecimal pkpSetahun = new BigDecimal(0.00), pkp = new BigDecimal(0.00);
                BigDecimal ptkpSetahun = new BigDecimal(new Ptkp().getPtkp(sd.getUser().getId().toString()));

                if(userPajak.getTotal_pendapatan().getPtkp_setahun() == null)
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

                Integer month = Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]);
                pendapatanTetaps.setMonth(month);
                pendapatanTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));
                BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(13-month));

                pkpSetahun = calculateSisaPtkp(userPajak,nettoPendapatanSetahun,pkpSetahun,ptkpSetahun);

                TarifPajak t = resultTax(userPajak,pkpSetahun,true);

                BasicDBList listPph21 = t.getListPph21();
                BigDecimal total_pph21_sementara = sumPPH21(listPph21);

                pajak.setNetto_take_homepay(totalPendapatanSementara.subtract(total_pph21_sementara));
                pajak.setPph21(listPph21);
                UserPajakTax upt = new UserPajakTax();
                upt.setIndex(t.getIndex());
                upt.setReminder(t.getReminderPajak());
                pajak.set_recordCalTax(upt);
                userPajak.getSetting_pajak().setReminder(t.getReminderPajak());
                userPajak.getSetting_pajak().setIndex(t.getIndex());
                pendapatanTetaps.setPajak(pajak);
                pendapatanTetaps.setStatus(true);

                if(userPajak.getPph21().getUsu()==null) {
                    userPajak.getPph21().setUsu(total_pph21_sementara);
                }else {
                    userPajak.getPph21().setUsu(userPajak.getPph21().getUsu().add(total_pph21_sementara));
                }

                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());

                datastore.save(userPajak);
                datastore.save(pendapatanTetaps);
                jsonArray.add(jsonObject);
            }else{ //already has data pendapatan tetap
                PendapatanTetaps pt = qPt.first();
                pt.setType(type);
                datastore.save(pt);
                BigDecimal total_pph21_sementara = sumPPH21(pt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                /*Query<PendapatanTetaps> x = datastore.createQuery(PendapatanTetaps.class)
                        .disableValidation();
//                        .filter("id_user", sd.getUser().getId().toString())
//                        .filter("salary_id", sd.getId().intValue());
                x.and(x.criteria("id_user").equal(sd.getUser().getId().toString()),x.criteria("salary_id").equal(sd.getId().intValue()));
//                qPt.filter("salary_id",sd.getId().intValue());
                if(x.count()==0){ //salary not duplicate
                    List<PendapatanTetaps> listPendapatanTetap = qPt.find().toList();
                    PendapatanTetaps pBefore = listPendapatanTetap.get(listPendapatanTetap.size()-1);
                    BigDecimal tBefore = pBefore.getPajak().getTotal_pendapatan_rka();
                    BigDecimal tNow = getPendapatanSementara(sd);
                    if(tBefore == null){ // kemungkinan pendapatan apbn bercampur dgn gaji
                        tBefore = pBefore.getPajak().getBruto_pendapatan();
                        System.out.println(pBefore.getSalary_id()+" Salary_id");
                        System.out.println(userPajak.getId_user()+" UserPajak");
                        System.out.println(listPendapatanTetap.size()+" Pendapatan Tetap Size");
                    }
                    if(tBefore.compareTo(tNow)==0){ // just input not updating netto setahun
                        PendapatanTetaps pendapatanTetaps = initializePendapatanTetap(sd, userPajak);

                        Pajak pajak = pBefore.getPajak();
                        tNow = tNow.add(StaticValue.jkk).add(StaticValue.jkm).add(StaticValue.bpjs_kesehatan);
                        BigDecimal biayaJabatan = getBiayaJabatan(tNow,userPajak,pajak);
                        BigDecimal nettoPendapatan = tNow.subtract(biayaJabatan);
                        pajak.setNetto_pendapatan(nettoPendapatan);

                        pendapatanTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
                        pendapatanTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));

                        pendapatanTetaps.setPajak(pajak);
                        pendapatanTetaps.setStatus(true);
                        userPajak.getPph21().setUsu(userPajak.getPph21().getUsu().add(sumPPH21(pajak.getPph21())));

                        datastore.save(userPajak);
                        datastore.save(pendapatanTetaps);
                    }else{ // there is some difference
                        *//**
                         * TODO
                         *
                         * jika lebih dari sebelumya hanya hitung yg berlebihnya saja
                         * jika kurang dari sebelumnya ga tau mau diapain
                         *//*
                        System.out.println("Here Gaji Tidak Sama dgn bulan sebelumnya");
                    }
                }else{ //salary sudah ada (salary duplicate)
                    *//**
                     * TODO
                     *
                     * kirim hasil pph 21 ke RKA
                     *//*
                }*/
            }
        });
    }

    private void calculateTaxHonor(List<SalaryDetail> salaryDetailList, String type){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
//                    .filter("id_user", sd.getUser().getId().toString())
//                    .filter("salary_id", sd.getId().toString());
            qPt.and(qPt.criteria("id_user").contains(sd.getUser().getId().toString()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);
                pendapatanTdkTetaps.setType(type);
                BigDecimal totalPendapatanSementara = getPendapatanSementara(sd);
                Pajak pajak = new Pajak();
                pajak.setTotal_pendapatan_rka(totalPendapatanSementara);
                pajak.setBruto_pendapatan(totalPendapatanSementara);

                BigDecimal biayaJabatan = getBiayaJabatan(totalPendapatanSementara,userPajak,pajak);

                BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(biayaJabatan);
                pajak.setNetto_pendapatan(nettoPendapatan);

                BigDecimal pkp = new BigDecimal(0.00);

                if(userPajak.getTotal_pendapatan().getPtkp_setahun() == null) {
                    BigDecimal ptkpSetahun = new BigDecimal(new Ptkp().getPtkp(sd.getUser().getId().toString()));
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);
                }

                pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
                pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));

                pkp = calculateSisaPtkp(userPajak,nettoPendapatan,pkp,userPajak.getTotal_pendapatan().getPtkp_setahun());

                TarifPajak t = resultTax(userPajak, pkp, false);

//                BasicDBList listPph21 = t.getListPph21();
                BigDecimal total_pph21_sementara = sumPPH21(t.getListPph21());

                pajak.setNetto_take_homepay(totalPendapatanSementara.subtract(total_pph21_sementara));
                pajak.setPph21(t.getListPph21());
                UserPajakTax upt = new UserPajakTax();
                upt.setIndex(t.getIndex());
                upt.setReminder(t.getReminderPajak());
                pajak.set_recordCalTax(upt);
                userPajak.getSetting_pajak().setReminder(t.getReminderPajak());
                userPajak.getSetting_pajak().setIndex(t.getIndex());
                pendapatanTdkTetaps.setPajak(pajak);
                pendapatanTdkTetaps.setStatus(true);

                if(userPajak.getPph21().getUsu()==null) {
                    userPajak.getPph21().setUsu(total_pph21_sementara);
                }else {
                    userPajak.getPph21().setUsu(userPajak.getPph21().getUsu().add(total_pph21_sementara));
                }

                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());

                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
                jsonArray.add(jsonObject);
            }else{ //duplicate request
                PendapatanTdkTetaps ptt = qPt.first();
                ptt.setType(type);
                datastore.save(ptt);
                BigDecimal total_pph21_sementara = sumPPH21(ptt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private void calculateTaxJasmed(List<SalaryDetail> salaryDetailList, String type){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
//                    .filter("id_user", sd.getUser().getId().toString())
//                    .filter("salary_id", sd.getId().toString());
            qPt.and(qPt.criteria("id_user").contains(sd.getUser().getId().toString()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null) {
                PendapatanTdkTetaps pendapatanTdkTetaps = calculateModelJasaTax(sd,userPajak,BigDecimal.valueOf(0.5));
                pendapatanTdkTetaps.setType(type);
                BigDecimal ptkpSetahun = new BigDecimal(new Ptkp().getPtkp(sd.getUser().getId().toString()));
                if (userPajak.getTotal_pendapatan().getPtkp_setahun() == null)
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
            }else{//duplicate request
                PendapatanTdkTetaps pendapatanTdkTetaps = qPt.first();
                pendapatanTdkTetaps.setType(type);
                datastore.save(pendapatanTdkTetaps);
                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private TarifPajak resultTax(UserPajak userPajak,BigDecimal pkp, boolean rutin){
        TarifPajak t = new TarifPajak();
        BigDecimal reminder;
        Integer index;
        if(userPajak.getPph21().getUsu()==null){
            if(userPajak.getPph21().getPns()==null) {
                reminder = new BigDecimal("50000000.00");
                index = 0;
            }else{
                reminder = userPajak.getSetting_pajak().getReminder();
                index = userPajak.getSetting_pajak().getIndex();
            }
        }else{
            reminder = userPajak.getSetting_pajak().getReminder();
            index = userPajak.getSetting_pajak().getIndex();
        }

        if(userPajak.getNpwp_simsdm()==null||userPajak.getNpwp_simsdm().equalsIgnoreCase("")){
            if(userPajak.getNpwp()==null || userPajak.getNpwp().equalsIgnoreCase(""))
                t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, rutin);
            else
                t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, rutin);
        }else{
            t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, rutin);
        }

        return t;
    }

    private TarifPajak resultTaxJasa(UserPajak userPajak, BigDecimal pkp){
        TarifPajak t = new TarifPajak();
        BigDecimal reminderJasmed;
        Integer indexJasmed;
        if(userPajak.getPph21().getJasa()==null){
            reminderJasmed = new BigDecimal("50000000.00");
            indexJasmed = 0;
        }else{
            reminderJasmed = userPajak.getSetting_pajak().getReminder_jasmed();
            indexJasmed = userPajak.getSetting_pajak().getIndex_jasmed();
        }

        if(userPajak.getNpwp_simsdm()==null||userPajak.getNpwp_simsdm().equalsIgnoreCase("")){
            if(userPajak.getNpwp()==null || userPajak.getNpwp().equalsIgnoreCase(""))
                t.hitungPajak(reminderJasmed,pkp,indexJasmed, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
            else
                t.hitungPajak(reminderJasmed,pkp,indexJasmed, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
        }else{
            t.hitungPajak(reminderJasmed,pkp,indexJasmed, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
        }

        return t;
    }

    private TarifPajak resultTaxKegiatan(UserPajak userPajak, BigDecimal pkp){
        TarifPajak t = new TarifPajak();
        BigDecimal reminderKegiatan;
        Integer indexKegiatan;
        if(userPajak.getPph21().getKegiatan()==null){
            reminderKegiatan = new BigDecimal("50000000.00");
            indexKegiatan = 0;
        }else{
            reminderKegiatan = userPajak.getSetting_pajak().getReminder_kegiatan();
            indexKegiatan = userPajak.getSetting_pajak().getIndex_kegiatan();
        }

        if(userPajak.getNpwp_simsdm()==null||userPajak.getNpwp_simsdm().equalsIgnoreCase("")){
            if(userPajak.getNpwp()==null || userPajak.getNpwp().equalsIgnoreCase(""))
                t.hitungPajak(reminderKegiatan,pkp,indexKegiatan, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
            else
                t.hitungPajak(reminderKegiatan,pkp,indexKegiatan, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
        }else{
            t.hitungPajak(reminderKegiatan,pkp,indexKegiatan, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
        }

        return t;
    }

    private UserPajak getUserPajak(SalaryDetail sd){
        Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user", sd.getUser().getId().toString());
        UserPajak userPajak = query.first();
        if(userPajak!=null) { // update data
            if(userPajak.getNpwp_simsdm()==null) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
            else if (userPajak.getNpwp_simsdm().isEmpty()) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
        }else{//insert data
            userPajak = new UserPajak();
            userPajak.setId_user(sd.getUser().getId().toString());
            userPajak.setNpwp("");
            userPajak.setNpwp_simsdm(sd.getUser().getNpwp());
            userPajak.setFront_degree(sd.getUser().getFront_degree());
            userPajak.setFull_name(sd.getUser().getFull_name());
            userPajak.setBehind_degree(sd.getUser().getBehind_degree());
            userPajak.setNip_simsdm(sd.getUser().getNip_nik().toString());
            userPajak.setGroup(sd.getUser().getGroup());
            userPajak.setTotal_pendapatan(new UserPajakPendapatan());
            userPajak.setSetting_pajak(new UserPajakTax());
            userPajak.setPph21(new UserPajakPPH());
        }
        return userPajak;
    }

    private UserPajak getUserPajakLuar(SalaryDetail sd, String type){
        UserPajak userPajak;
        Query<UserPajak> qUP = datastore.createQuery(UserPajak.class).disableValidation();
        qUP.and(qUP.criteria("id_user").contains(type),qUP.criteria("full_name").containsIgnoreCase(sd.getUser().getFull_name().trim()));
        if(qUP.first()==null){//input user baru
            userPajak = new UserPajak();
            userPajak.setId_user(type+"-"+System.currentTimeMillis());
            userPajak.setNpwp_simsdm("0");
            userPajak.setFull_name(sd.getUser().getFull_name());
            userPajak.setTotal_pendapatan(new UserPajakPendapatan());
            userPajak.setSetting_pajak(new UserPajakTax());
            userPajak.setPph21(new UserPajakPPH());
        }else{// user udah ada
            userPajak = qUP.first();
        }
        return userPajak;
    }

    private BigDecimal sumPPH21(BasicDBList listPph21){
        BigDecimal total_pph21_sementara = BigDecimal.ZERO;
        for(int i=0;i<listPph21.size();i++) {
            BasicDBObject obj = (BasicDBObject) listPph21.get(i);
            total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
        }
        return total_pph21_sementara;
    }

    private PendapatanTetaps initializePendapatanTetap(SalaryDetail sd, UserPajak userPajak) {
        PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
        pendapatanTetaps.setId_user(userPajak.getId_user());
        pendapatanTetaps.setSalary_id(sd.getId().intValue());
        pendapatanTetaps.setUnit(sd.getUnit());
        BasicDBObject rkaPayment = BasicDBObject.parse(sd.getPayment().getAsJsonObject().toString());
        pendapatanTetaps.setRka_payment(rkaPayment);
        return  pendapatanTetaps;
    }

    private PendapatanTdkTetaps initializePendapatanTdkTetap(SalaryDetail sd, UserPajak userPajak){
        PendapatanTdkTetaps pendapatanTdkTetaps = new PendapatanTdkTetaps();
        pendapatanTdkTetaps.setId_user(userPajak.getId_user());
        pendapatanTdkTetaps.setSalary_id(sd.getId().intValue());
        pendapatanTdkTetaps.setUnit(sd.getUnit());
        BasicDBObject rkaPayment = BasicDBObject.parse(sd.getPayment().getAsJsonObject().toString());
        pendapatanTdkTetaps.setRka_payment(rkaPayment);
        return  pendapatanTdkTetaps;
    }

    private BigDecimal getPendapatanSementara(SalaryDetail sd){
        return new BigDecimal(sd.getPayment().getAsJsonObject().entrySet().stream()
                .filter(row -> {
                    if(row.getValue().isJsonObject()) return false;
                    else
                        if(row.getValue().isJsonPrimitive())
                            if(row.getValue().getAsJsonPrimitive().isNumber() && !(row.getKey().contains("pph21"))) return true;
                            else return false;
                        else return  false;
                })
                .mapToInt(row -> {
                    if(!row.getKey().equalsIgnoreCase("returned"))
                        return row.getValue().getAsInt();
                    else
                        return 0;
                }).sum() - sd.getPayment().getAsJsonObject().get("returned").getAsInt());
    }

    private BigDecimal getBiayaJabatan(BigDecimal totalPendapatanSementara, UserPajak userPajak, Pajak pajak){
        BigDecimal biayaJabatan = totalPendapatanSementara.multiply(StaticValue.persenBiayaJabatan);
        if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun() == null){
            if (biayaJabatan.compareTo(StaticValue.limitBiayaJabatan) <= 0) {
                pajak.setBiaya_jabatan(biayaJabatan);
                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(biayaJabatan);
            }else {
                biayaJabatan = BigDecimal.ZERO;
                pajak.setBiaya_jabatan(biayaJabatan);
                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(StaticValue.limitBiayaJabatan);
            }
        }else{
            if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().compareTo(StaticValue.limitBiayaJabatan)<0){
                if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().add(biayaJabatan).compareTo(StaticValue.limitBiayaJabatan)<0){
                    userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(
                            userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().add(biayaJabatan));
                    pajak.setBiaya_jabatan(biayaJabatan);
                }else{
                    biayaJabatan = StaticValue.limitBiayaJabatan.subtract(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun());
                    userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(StaticValue.limitBiayaJabatan);
                }
            }else{
                biayaJabatan = BigDecimal.ZERO;
                pajak.setBiaya_jabatan(biayaJabatan);
                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(StaticValue.limitBiayaJabatan);
            }
        }
        return biayaJabatan;
    }

    private BigDecimal calculateSisaPtkp(UserPajak userPajak, BigDecimal nettoPendapatan, BigDecimal pkp, BigDecimal ptkp){
        boolean isNettoPendapatanSetahun = false;
        if(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun() == null){
            userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(nettoPendapatan);
        }else{
            if(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().compareTo(BigDecimal.ZERO)>0) {
                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(
                        userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().add(nettoPendapatan)
                );
                isNettoPendapatanSetahun = true;
            }else{
                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(nettoPendapatan);
            }
        }

        BigDecimal sisaPtkpSetahun;
        if(isNettoPendapatanSetahun){
            if(userPajak.getTotal_pendapatan().getSisa_ptkp().compareTo(BigDecimal.ZERO)>0){
                sisaPtkpSetahun = userPajak.getTotal_pendapatan().getSisa_ptkp();
                if(sisaPtkpSetahun.compareTo(nettoPendapatan) > 0){
                    sisaPtkpSetahun = sisaPtkpSetahun.subtract(nettoPendapatan);
                    pkp = BigDecimal.ZERO;
                }else{
                    pkp = nettoPendapatan.subtract(sisaPtkpSetahun);
                    sisaPtkpSetahun = BigDecimal.ZERO;
                }
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
                userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
            }else{
                pkp = nettoPendapatan;
                userPajak.getTotal_pendapatan().setTotal_pkp(userPajak.getTotal_pendapatan().getTotal_pkp().add(nettoPendapatan));
                sisaPtkpSetahun = BigDecimal.ZERO;
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
            }
        }else {
            if (nettoPendapatan.compareTo(ptkp) >= 0) {
                pkp = nettoPendapatan.subtract(ptkp);
                sisaPtkpSetahun = new BigDecimal(0.00);
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
                userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
            } else {
                userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
                sisaPtkpSetahun = ptkp.subtract(nettoPendapatan);
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
            }
        }
        return pkp;
    }

    private PendapatanTdkTetaps calculateModelKegiatanTax(SalaryDetail sd, UserPajak userPajak, BigDecimal persenKenaPajak){
        PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);
        BigDecimal brutoPendapatan = getPendapatanSementara(sd);
        Pajak pajak = new Pajak();
        pajak.setTotal_pendapatan_rka(brutoPendapatan);
        pajak.setBruto_pendapatan(brutoPendapatan);
        pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
        pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));
        if (userPajak.getTotal_pendapatan().getBruto_kegiatan_setahun() == null)
            userPajak.getTotal_pendapatan().setBruto_kegiatan_setahun(brutoPendapatan);
        else
            userPajak.getTotal_pendapatan().setBruto_kegiatan_setahun(
                    userPajak.getTotal_pendapatan().getBruto_kegiatan_setahun().add(brutoPendapatan)
            );
        BigDecimal pkp = brutoPendapatan.multiply(persenKenaPajak);
        if (userPajak.getTotal_pendapatan().getTotal_pkp_kegiatan() == null)
            userPajak.getTotal_pendapatan().setTotal_pkp_kegiatan(pkp);
        else
            userPajak.getTotal_pendapatan().setTotal_pkp_kegiatan(
                    userPajak.getTotal_pendapatan().getTotal_pkp_kegiatan().add(pkp)
            );
        TarifPajak t = resultTaxKegiatan(userPajak, pkp);
        BigDecimal total_pph21_sementara = sumPPH21(t.getListPph21());
        pajak.setNetto_take_homepay(brutoPendapatan.subtract(total_pph21_sementara));
        pajak.setPph21(t.getListPph21());
        UserPajakTax upt = new UserPajakTax();
        upt.setIndex_kegiatan(t.getIndex());
        upt.setReminder_kegiatan(t.getReminderPajak());
        pajak.set_recordCalTax(upt);
        userPajak.getSetting_pajak().setReminder_kegiatan(t.getReminderPajak());
        userPajak.getSetting_pajak().setIndex_kegiatan(t.getIndex());
        pendapatanTdkTetaps.setPajak(pajak);
        pendapatanTdkTetaps.setStatus(true);

        if (userPajak.getPph21().getKegiatan() == null) {
            userPajak.getPph21().setKegiatan(total_pph21_sementara);
        } else {
            userPajak.getPph21().setKegiatan(userPajak.getPph21().getKegiatan().add(total_pph21_sementara));
        }

        return pendapatanTdkTetaps;
    }

    private PendapatanTdkTetaps calculateModelJasaTax(SalaryDetail sd, UserPajak userPajak, BigDecimal persenKenaPajak){
        PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);
        BigDecimal brutoPendapatan = getPendapatanSementara(sd);
        Pajak pajak = new Pajak();
        pajak.setTotal_pendapatan_rka(brutoPendapatan);
        pajak.setBruto_pendapatan(brutoPendapatan);
        pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
        pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));
        if (userPajak.getTotal_pendapatan().getBruto_jasa_setahun() == null)
            userPajak.getTotal_pendapatan().setBruto_jasa_setahun(brutoPendapatan);
        else
            userPajak.getTotal_pendapatan().setBruto_jasa_setahun(
                    userPajak.getTotal_pendapatan().getBruto_jasa_setahun().add(brutoPendapatan)
            );
        BigDecimal pkp = brutoPendapatan.multiply(persenKenaPajak);
        if (userPajak.getTotal_pendapatan().getTotal_pkp_jasa() == null)
            userPajak.getTotal_pendapatan().setTotal_pkp_jasa(pkp);
        else
            userPajak.getTotal_pendapatan().setTotal_pkp_jasa(
                    userPajak.getTotal_pendapatan().getTotal_pkp_jasa().add(pkp)
            );
        TarifPajak t = resultTaxJasa(userPajak, pkp);
        BigDecimal total_pph21_sementara = sumPPH21(t.getListPph21());
        pajak.setNetto_take_homepay(brutoPendapatan.subtract(total_pph21_sementara));
        pajak.setPph21(t.getListPph21());
        UserPajakTax upt = new UserPajakTax();
        upt.setIndex_jasmed(t.getIndex());
        upt.setReminder_jasmed(t.getReminderPajak());
        pajak.set_recordCalTax(upt);
        userPajak.getSetting_pajak().setReminder_jasmed(t.getReminderPajak());
        userPajak.getSetting_pajak().setIndex_jasmed(t.getIndex());
        pendapatanTdkTetaps.setPajak(pajak);
        pendapatanTdkTetaps.setStatus(true);

        if (userPajak.getPph21().getJasa() == null) {
            userPajak.getPph21().setJasa(total_pph21_sementara);
        } else {
            userPajak.getPph21().setJasa(userPajak.getPph21().getJasa().add(total_pph21_sementara));
        }

        return pendapatanTdkTetaps;
    }

    public Pajak getDetailTax(Integer salaryId){
        Query<PendapatanTetaps> pendapatanTetapsQuery = datastore.createQuery(PendapatanTetaps.class).disableValidation();
        pendapatanTetapsQuery.criteria("salary_id").equal(salaryId);
        if(pendapatanTetapsQuery.first()!=null){
            PendapatanTetaps pendapatanTetaps = pendapatanTetapsQuery.first();
            return pendapatanTetaps.getPajak();
        }else{
            Query<PendapatanTdkTetaps> pendapatanTdkTetapsQuery = datastore.createQuery(PendapatanTdkTetaps.class).disableValidation();
            pendapatanTdkTetapsQuery.criteria("salary_id").equal(salaryId);
            if(pendapatanTdkTetapsQuery.first()!=null){
                PendapatanTdkTetaps pendapatanTdkTetaps = pendapatanTdkTetapsQuery.first();
                return pendapatanTdkTetaps.getPajak();
            }else{
                return null;
            }
        }
    }

    public UserPajakPendapatan getProfileTax(String userId){
        Query<UserPajak> userPajakQuery = datastore.createQuery(UserPajak.class).disableValidation();
        userPajakQuery.criteria("id_user").equalIgnoreCase(userId);
        if(userPajakQuery.first() != null){
            UserPajak userPajak = userPajakQuery.first();
            return userPajak.getTotal_pendapatan();
        }else{
            return null;
        }
    }

    public List<UserPajak> getListTax(String month, String unitId, boolean pegawaiLuar, String sumberDana){
        Query<PendapatanTdkTetaps> pendapatanTdkTetapsQuery = datastore.find(PendapatanTdkTetaps.class).disableValidation();
        Query<UserPajak> userPajakQuery = datastore.find(UserPajak.class).disableValidation();
        if(unitId != null){ // specific unit
            pendapatanTdkTetapsQuery.and(
                    pendapatanTdkTetapsQuery.criteria("month").equal(Integer.parseInt(month)),
                    pendapatanTdkTetapsQuery.criteria("unit.id").equal(Integer.parseInt(unitId)),
                    pendapatanTdkTetapsQuery.criteria("rka_payment.activity.source_of_fund").equalIgnoreCase(sumberDana)
            );
            if(!pegawaiLuar){
                pendapatanTdkTetapsQuery.criteria("id_user").not().containsIgnoreCase("-");
            }else{
                pendapatanTdkTetapsQuery.criteria("id_user").containsIgnoreCase("-");
            }

            List<PendapatanTdkTetaps> pendapatanTdkTetapsList = pendapatanTdkTetapsQuery.find().toList();
            Map<String, BigDecimal> pendapatan = pendapatanTdkTetapsList.stream()
                .collect(
                    Collectors.groupingBy(
                        PendapatanTdkTetaps::getId_user,
                        Collectors.mapping(
                            PendapatanTdkTetaps::getNettoPendapatan,
                            Collectors.reducing(
                                BigDecimal.ZERO,
                                BigDecimal::add
                            )
                        )
                    )
                );

            Map<String, BigDecimal> pajak = pendapatanTdkTetapsList.stream()
                    .collect(
                            Collectors.groupingBy(
                                    PendapatanTdkTetaps::getId_user,
                                    Collectors.mapping(
                                            PendapatanTdkTetaps::getPph21,
                                            Collectors.reducing(
                                                    BigDecimal.ZERO,
                                                    BigDecimal::add
                                            )
                                    )
                            )
                    );

            userPajakQuery.field("id_user").in(pendapatan.keySet());
            List<UserPajak> userPajakList = userPajakQuery.find().toList();
            userPajakList.forEach(e -> {
                e.setTotalNettoPendapatan(pendapatan.get(e.getId_user()));
                e.setTotalPph21(pajak.get(e.getId_user()));
            });
//            userPajakList.stream().forEach(System.out::println);
            return userPajakList;
        }else{ // general or whole USU
            return null;
        }

    }

    public BuktiPotong getBuktiPotong(String userId){
        BuktiPotong pegawai = new BuktiPotong();
        try {
            String response = ApiRka.callApiUsu("https://api.usu.ac.id/0.1/users/"+userId+"?fieldset=structural,functional,rank","GET");


            JsonObject jsonObject = new Gson().fromJson(response,JsonObject.class).getAsJsonObject("data");
            pegawai.setA_01(jsonObject.get("npwp").getAsString());
            pegawai.setA_02(jsonObject.get("nip").getAsString());
            pegawai.setA_03(jsonObject.get("full_name").getAsString());
            final String[] alamat = {""};
            jsonObject.get("address").getAsJsonObject().entrySet().forEach(e->{
                alamat[0] = alamat[0].concat(e.getValue().getAsString()+",");
            });
            pegawai.setA_04(alamat[0].substring(0, alamat[0].length()-1));
            String gender = jsonObject.get("gender").getAsString();
            switch (gender){
                case "Pria":
                    pegawai.setA_05(true);
                    break;
                case "Wanita":
                    pegawai.setA_06(true);
                    break;
            }
            //TODO: Status dan Jumlah Tanggungan PTKP

            //TODO: Nama Jabatan

//            pegawai.setB_01(BigInteger.ZERO);
            response = ApiRka.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?source_of_fund=NON-PNBP&year=2019&status=1&mode=summary2&user_id="+userId,"GET");
            Salary salary = new Gson().fromJson(response, Salary.class);
            SalaryDetail salaryDetail = salary.getResponse().getSalary_receivers().get(0);
            BigInteger totalBruto = new BigInteger(Integer.toString(salaryDetail.getSummary().get("total").getAsInt()));
            pegawai.setB_03(totalBruto);
            pegawai.setB_08(totalBruto);

            BigDecimal biayaJabatan = StaticValue.persenBiayaJabatan.multiply(new BigDecimal(totalBruto.intValue()));
            if(biayaJabatan.compareTo(StaticValue.limitBiayaJabatan) > 0){biayaJabatan = StaticValue.limitBiayaJabatan; pegawai.setB_09(StaticValue.limitBiayaJabatan.toBigInteger());}
            else{pegawai.setB_09(biayaJabatan.toBigInteger());}
            pegawai.setB_11(biayaJabatan.toBigInteger());

            BigInteger totalNetto = totalBruto.subtract(biayaJabatan.toBigInteger());
            pegawai.setB_12(totalNetto);
//            pegawai.setB_13(BigInteger.ZERO);
            pegawai.setB_14(totalNetto);

            Query<usu.pajak.model.UserPajak> query = ds2019.find(usu.pajak.model.UserPajak.class).disableValidation();
            query.criteria("id_user").equalIgnoreCase(userId);
//            query.filter("id_user",userId);
            usu.pajak.model.UserPajak userPajak = query.find().toList().get(0);
            BigInteger ptkp = new BigInteger(userPajak.getPtkp_setahun());
            pegawai.setB_15(ptkp);

            BigInteger totalPkp = totalNetto.subtract(ptkp);
            pegawai.setB_16(totalPkp);

            BigInteger totalPph21 = new BigInteger(Integer.toString(salaryDetail.getSummary().get("pph21").getAsInt()));
            pegawai.setB_17(totalPph21);
            pegawai.setB_19(totalPph21);
            pegawai.setB_20(totalPph21);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pegawai;
    }

    public void deleteTax(Integer type, Integer value){
        switch (type) {
            case REQUEST:
                Query<PendapatanTdkTetaps> pendapatanTdkTetapsQuery = datastore.createQuery(PendapatanTdkTetaps.class).disableValidation();
                pendapatanTdkTetapsQuery.criteria("request_id").equal(value);
                if(pendapatanTdkTetapsQuery.first()!=null){
                    List<PendapatanTdkTetaps> pendapatanTdkTetapsList = pendapatanTdkTetapsQuery.asList();
                    for (PendapatanTdkTetaps pendapatanTdkTetaps: pendapatanTdkTetapsList) {
                        Query<UserPajak> query = datastore.createQuery(UserPajak.class).disableValidation();
                        query.criteria("id_user").equalIgnoreCase(pendapatanTdkTetaps.getId_user());
                        UserPajak userPajak = query.first();

                    }
//                    return pendapatanTdkTetaps.getPajak();
                }else{
//                    return null;
                }
                break;
            case SALARY:
                pendapatanTdkTetapsQuery = datastore.createQuery(PendapatanTdkTetaps.class).disableValidation();
                pendapatanTdkTetapsQuery.criteria("salary_id").equal(value);
                if(pendapatanTdkTetapsQuery.first()!=null){
                    PendapatanTdkTetaps pendapatanTdkTetaps = pendapatanTdkTetapsQuery.first();
                    Query<UserPajak> query = datastore.createQuery(UserPajak.class).disableValidation();
                    query.criteria("id_user").equalIgnoreCase(pendapatanTdkTetaps.getId_user());
                    UserPajak userPajak = query.first();
//                    return pendapatanTdkTetaps.getPajak();
                }else{
//                    return null;
                }
                break;
            default:
                break;
        }
    }

    /*private Integer[] idUserLebihBayarDibawahPtkp = new Integer[]{
            5863,
            6173,
            5677,
            5435,
            2997,
            5865,
            2605,
            5671,
            6156,
            6107,
            4434,
            2691,
            2579,
            6217,
            5681,
            1007,
            4459,
            6084,
            2567,
            4272,
            4144,
            6734,
            4460,
            5673,
            4450,
            5603,
            3734,
            2637,
            3005,
            4469,
            2654,
            4107,
            4445,
            2607,
            6078,
            2670,
            2687,
            2618,
            4502,
            3748,
            6176,
            3774,
            6206,
            4428,
            4383,
            489,
            5839,
            4358,
            6175,
            6184,
            4140,
            6174,
            4522,
            5675,
            4177,
            4138,
            4048,
            5672,
            4142,
            4415,
            4378,
            4376,
            4178,
            3726,
            4354,
            5438,
            6203,
            4382,
            5539,
            5547,
            4143,
            4373,
            6280,
            4384,
            2641,
            4426,
            4377,
            6193,
            4313,
            4455,
            6151,
            4175,
            3699,
            2666,
            5862,
            5553,
            6733,
            5848,
            2640,
            5846,
            4381,
            5843,
            4421,
            4435,
            4147,
            3712,
            4534,
            5573,
            3711,
            4438,
            5559,
            4223,
            5562,
            5561,
            5560,
            5849,
            4403,
            4183,
            3770,
            4186,
            3713,
            4533,
            7003,
            4261,
            4139,
            5540,
            4487,
            6201,
            4217,
            6231,
            5502,
            4387,
            4231,
            4357,
            4098,
            6187,
            3685,
            6152,
            3686,
            3683,
            2619,
            6170,
            4284,
            4262,
            3687,
            5670,
            4414,
            4224,
            4270,
            4432,
            4409,
            6212,
            4431,
            4400,
            4399,
            6211,
            4225,
            4150,
            4164,
            3796,
            5861,
            4369,
            4416,
            450,
            6008,
            4405,
            4436,
            6101,
            4424,
            4486,
            4398,
            4370,
            5517,
            5531,
            4176,
            4197,
            4208,
            3749,
            6256,
            4310,
            4443,
            4493,
            4406,
            4308,
            4232,
            4115,
            1383,
            4111,
            3691,
            4179,
            4266,
            5630,
            5521,
            3805,
            3682,
            4221,
            6927,
            485,
            4229,
            4264,
            4407,
            5437,
            4230,
            4227,
            4280,
            3799,
            6090,
            4226,
            3808,
            5511,
            6072,
            4065,
            4412,
            3708,
            6216,
            4192,
            6116,
            5789,
            5516,
            4290,
            6154,
            4402,
            3798,
            4488,
            4471,
            4464,
            4458,
            4442,
            5535,
            3802,
            4440,
            4181,
            6087,
            3919,
            3676,
            4117,
            3707,
            4332,
            6129,
            4275,
            3875,
            4388,
            4128,
            3267,
            3924,
            6272,
            4127,
            4180,
            4392,
            2574,
            3809,
            4165,
            4462,
            5644,
            4285,
            6016,
            5489,
            4394,
            5490,
            6215,
            3714,
            4401,
            5979,
            3100,
            5519,
            4408,
            5477,
            4439,
            4453,
            4404,
            5476,
            4049,
            6105,
            4410,
            6219,
            4330,
            5497,
            4137,
            4475,
            4482,
            4095,
            5492,
            3684,
            4204,
            3730,
            5478,
            5522,
            5448,
            3754,
            6013,
            3670,
            4494,
            4260,
            5479,
            4497,
            5941,
            5488,
            4211,
            5475,
            6074,
            4206,
            5486,
            5529,
            5543,
            3923,
            6168,
            3838,
            3879,
            4213,
            4216,
            3784,
            5474,
            5491,
            4468,
            5498,
            6069,
            6209,
            3786,
            4411,
            3752,
            4214,
            5527,
            4252,
            5687,
            6180,
            4219,
            5473,
            4430,
            5643,
            4289,
            4538,
            3836,
            4299,
            6049,
            4160,
            6221,
            4480,
            3927,
            4071,
            5480,
            4425,
            3716,
            4156,
            5485,
            4119,
            3944,
            5525,
            6020,
            4413,
            6082,
            4189,
            4278,
            5607,
            5433,
            617,
            3905,
            4222,
            4254,
            3811,
            4429,
            4267,
            657,
            5508,
            6025,
            5484,
            6220,
            6218,
            6213,
            4191,
            3764,
            4353,
            6110,
            5688,
            4110,
            3840,
            6117,
            5499,
            4329,
            4335,
            5679,
            3848,
            5669,
            3694,
            3744,
            5616,
            3880,
            6080,
            3678,
            5668,
            4496,
            4318,
            3705,
            4395,
            3662,
            4419,
            3783,
            3867,
            3745,
            3914,
            4484,
            4349,
            4212,
            5610,
            5518,
            4006,
            4441,
            5481,
            6181,
            4422,
            5998,
            4151,
            4220,
            3790,
            6108,
            5493,
            4495,
            5544,
            4292,
            3824,
            6029,
            4273,
            4203,
            4481,
            6260,
            5605,
            3861,
            4109,
            5524,
            5602,
            3769,
            215,
            3767,
            6081,
            6083,
            3753,
            6065,
            4474,
            6005,
            6009,
            5509,
            6166,
            5923,
            4478,
            6007,
            4228,
            3768,
            4133,
            300,
            5507,
            4467,
            5434,
            4097,
            5513,
            4255,
            5690,
            5512,
            4325,
            6061,
            3822,
            6143,
            3679,
            5514,
            3912,
            4303,
            6095,
            3733,
            4333,
            4126,
            4302,
            6017,
            4161,
            4205,
            6089,
            5925,
            3820,
            4201,
            2964,
            4116,
            4427,
            5571,
            5664,
            3921,
            73,
            4393,
            5445,
            5579,
            6026,
            3911,
            5899,
            4131,
            1431,
            3931,
            4149,
            3965,
            4234,
            3741,
            5520,
            4114,
            3704,
            3671,
            3692,
            3772,
            4321,
            3872,
            5510,
            5900,
            3720,
            5617,
            6279,
            4386,
            4479,
            3742,
            3835,
            4256,
            3763,
            5689,
            480,
            4113,
            6093,
            5495,
            3803,
            3751,
            4282,
            3807,
            7016,
            3933,
            5927,
            3882,
            6257,
            4352,
            4153,
            3693,
            4207,
            4283,
            4100,
            3755,
            4314,
            3864,
            6258,
            2962,
            1426,
            5791,
            4023,
            69,
            4073,
            1361,
            80,
            3791,
            4274,
            3832,
            4327,
            3932,
            4463,
            5515,
            5869,
            5678,
            3823,
            471,
            6066,
            4008,
            3915,
            3855,
            3873,
            6019,
            3999,
            3757,
            3953,
            3672,
            3681,
            4397,
            4390,
            3792,
            4348,
            4129,
            4347,
            3871,
            2984,
            7112,
            5788,
            4473,
            4485,
            5532,
            4130,
            3677,
            6044,
            6070,
            3916,
            3967,
            5980,
            5787,
            4120,
            5542,
            3743,
            5604,
            5844,
            3673,
            5841,
            4155,
            5611,
            3663,
            4163,
            3674,
            5556,
            5613,
            5623,
            3856,
            3845,
            4396,
            4281,
            4003,
            3701,
            3762,
            4300,
            6282,
            5614,
            4447,
            4102,
            1517,
            4121,
            4472,
            4196,
            4099,
            1369,
            4103,
            5618,
            204,
            6002,
            3668,
            3736,
            455,
            4322,
            2113,
            5937,
            145,
            3773,
            5901,
            220,
            6276,
            2958,
            210,
            5581,
            2511,
            224,
            4050,
            3289,
            216,
            3187,
            2954,
            168,
            86,
            4337,
            5554,
            1,
            398,
            738,
            139,
            1485,
    };*/
}
