package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public enum ReceiveRka {
    getInstance();
    private String token = "";

    public Token getSSO(String identity, String password) throws IOException {
        String url = "https://akun.usu.ac.id/auth/login/apps?random_char=TVWBJBSuwyewbwgcuw23657438zs";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "identity="+identity+"&password="+password;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr;
        wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        InputStream response = con.getInputStream();

        Scanner scanner = new Scanner(response);
        String responseBody = scanner.useDelimiter("\\A").next();
        Token token = new Gson().fromJson(responseBody,Token.class);
        return token;
    }

    public String callApiUsu(String ep, String method) throws IOException {
        URL obj = new URL(ep);
        HttpURLConnection conn= (HttpURLConnection) obj.openConnection();

        if(token.isEmpty()) token=getSSO("88062916081001","casper14").getToken();

        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer "+token);
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( true );
        conn.setDoOutput( true );
        conn.setDoInput(true);

        StringBuffer response = new StringBuffer();
        if(conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }else{
            response.append("{ \"code\": 404}");
        }
        return response.toString();
    }

    public String callApiUsu(String ep, String method, JsonArray jsonArray) throws IOException {
        URL obj = new URL(ep);
        HttpsURLConnection conn= (HttpsURLConnection) obj.openConnection();

        if(token.isEmpty()) token=getSSO("88062916081001","casper14").getToken();

        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer "+token);
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( false );
        conn.setDoOutput( true );
        conn.setDoInput(true);

        DataOutputStream wr;
        wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(jsonArray.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}

class Token{
    private String token;

    public String getToken() {
        return token;
    }

}