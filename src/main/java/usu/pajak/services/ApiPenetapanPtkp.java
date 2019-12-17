package usu.pajak.services;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class ApiPenetapanPtkp {

    public void getDataPegawai(String userId) throws IOException {
        String endpoint = "https://api.usu.ac.id/0.1/users/"+userId+"/ptkp";
//        URL obj = new URL(endpoint);
//        HttpsURLConnection conn= (HttpsURLConnection) obj.openConnection();
//
//        conn.setRequestMethod( "GET" );
//        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////        conn.setRequestProperty("Authorization", "Bearer "+getSSO("88062916081001","casper14").getToken());
//        conn.setRequestProperty("AppSecret", "simrkausu");
//        conn.setUseCaches( false );
//        conn.setDoOutput( true );
//        conn.setDoInput(true);
//
////        DataOutputStream wr;
////        wr = new DataOutputStream(conn.getOutputStream());
////        wr.writeBytes(postData);
////        wr.flush();
////        wr.close();
//
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(conn.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
////            System.out.println(inputLine);
//        }
//        in.close();
////        return response.toString();
    }

    public void setPtpkp(){
//        getDataPegawai();
        // put ke endpoint
        // save ke database
    }
}
