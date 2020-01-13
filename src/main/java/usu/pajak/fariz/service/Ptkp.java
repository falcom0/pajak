package usu.pajak.fariz.service;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import usu.pajak.fariz.model.UserSimSdm;

public class Ptkp {
//    getInstance();
    private static final Integer initPtkp = 54000000;
    private Integer additionalPtkp = 4500000;
    public Integer getPtkp(String userId){
        Integer ptkp = 0;
        try {
            Response response = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.1/users/" + userId + "/ptkp", "GET"), Response.class);
            UserSimSdm us = response.getResponse().get(0);
            int count = 0;
            if(us.getGender().equalsIgnoreCase("Pria") && us.getHas_couple())  count++;
            else if(us.getGender().equalsIgnoreCase("Wanita") && us.getHas_couple()) return initPtkp;
            if(us.getNum_of_children()>0 && us.getNum_of_children()<=3) count += us.getNum_of_children();
            additionalPtkp = count * additionalPtkp;
            ptkp = initPtkp+additionalPtkp;
            return ptkp;
        }catch (IOException io){
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}

class Response{
    private List<UserSimSdm> response;
    public List<UserSimSdm> getResponse() {
        return response;
    }
}