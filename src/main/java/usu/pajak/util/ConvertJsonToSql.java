package usu.pajak.util;

import com.google.gson.Gson;

import java.io.File;
import java.io.Reader;

public class ConvertJsonToSql {
    public static void main(String[] args) {
        String js = "(11,104,'ACEH')," +
                "(12,104,'SUMATERA UTARA')," +
                "(13,104,'SUMATERA BARAT')," +
                "(14,104,'RIAU')," +
                "(15,104,'JAMBI')," +
                "(16,104,'SUMATERA SELATAN')," +
                "(17,104,'BENGKULU')," +
                "(18,104,'LAMPUNG')," +
                "(19,104,'KEPULAUAN BANGKA BELITUNG')," +
                "(21,104,'KEPULAUAN RIAU')," +
                "(31,104,'DKI JAKARTA')," +
                "(32,104,'JAWA BARAT')," +
                "(33,104,'JAWA TENGAH')," +
                "(34,104,'DI YOGYAKARTA')," +
                "(35,104,'JAWA TIMUR')," +
                "(36,104,'BANTEN')," +
                "(51,104,'BALI')," +
                "(52,104,'NUSA TENGGARA BARAT')," +
                "(53,104,'NUSA TENGGARA TIMUR')," +
                "(61,104,'KALIMANTAN BARAT')," +
                "(62,104,'KALIMANTAN TENGAH')," +
                "(63,104,'KALIMANTAN SELATAN')," +
                "(64,104,'KALIMANTAN TIMUR')," +
                "(65,104,'KALIMANTAN UTARA')," +
                "(71,104,'SULAWESI UTARA')," +
                "(72,104,'SULAWESI TENGAH')," +
                "(73,104,'SULAWESI SELATAN')," +
                "(74,104,'SULAWESI TENGGARA')," +
                "(75,104,'GORONTALO')," +
                "(76,104,'SULAWESI BARAT')," +
                "(81,104,'MALUKU')," +
                "(82,104,'MALUKU UTARA')," +
                "(91,104,'PAPUA BARAT')," +
                "(94,104,'PAPUA')";
//        Country[] countries = new Gson().fromJson(js,Country[].class);
//        String values = "";
//        for (Country country: countries) {
//            values = values.concat("("+country.getName()+",104,'"+country.getCode()+"'),");
//        }
//        values = values.substring(0,values.length()-1);m
        String query = "INSERT INTO city (id,country_id, name) VALUES "+js+";";
        System.out.println(query);
    }
}

class Country{
    private String name;
    private String code;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}