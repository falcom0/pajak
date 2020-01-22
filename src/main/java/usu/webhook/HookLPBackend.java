package usu.webhook;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static spark.Spark.*;

public class HookLPBackend {
    private static String secret = "bef1fb573c23d8ed5f1a10c866391285a5338f0af79d80646227e2a0a1cf1b194d03e9ea8990a9c75c025e7516d61b7b8087466afff2ab41c5c5e2d5b3251bd8";
    public static void main(String[] args){
        port(5001);
//        String keystoreFilePath = "/srv/www/java/apikeystore";
//        String keystoreFilePath = "D:\\cygwin64\\home\\PSI-DEV-7\\api-fariz\\keystore\\apikeystore";
//        String keystorePassword = "En-Triplet";
//        secure(keystoreFilePath, keystorePassword, null, null);

        post("/git", (req,res) ->{
            JsonParser j = new JsonParser();
            JsonElement je = j.parse(req.body());
            if(je.getAsJsonObject().get("secret").getAsString().equalsIgnoreCase(secret)) {
                String [] arg = new String[] {"/bin/bash", "-c", "git pull"};
                Process proc = new ProcessBuilder(arg).start();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String line = "";
                while((line = reader.readLine()) != null) {
                    System.out.print(line + "\n");
                }
                proc.waitFor();
                return "{ \"status\" : \"success\"}";
            }else{
                return "{ \"status\" : \"failed\"}";
            }
        });
    }
}
