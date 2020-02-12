package usu.pajak.fariz.service;

import com.google.gson.Gson;
import usu.pajak.fariz.model.*;
import usu.pajak.services.ApiRka;

import java.util.List;

import static spark.Spark.*;

public class ApiTax {

    public static void main(String[] args) {
        port(8253);
        get("/tax", (request,response) -> {
            String requestId = request.queryParams("request_id");
            String salaryId = request.queryParams("salary_id");
            if(requestId != null){
                Salary salary = new Gson().fromJson(
                        ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?request_id="+requestId,"GET")
                ,Salary.class);
                if(salary != null){
                    Tax tax = new Tax(salary);
                    if(tax.getJsonArray() != null){
                        ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts", "PUT", tax.getJsonArray());
                        return "{ \"code\":200,\"status\": \"success\"}";
                    }else
                        return "{ \"code\":401,\"status\": \"failed\",\"request_id\": " + requestId + "}";
                }else{
                    return "{ \"code\":401,\"status\": \"failed\",\"request_id\": " + requestId + "}";
                }
            }else{
                Salary salary = new Gson().fromJson(
                        ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?salary_id="+salaryId,"GET")
                        ,Salary.class);
                if(salary != null){
                    Tax tax = new Tax(salary);
                    if(tax.getJsonArray() != null){
                        ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts", "PUT", tax.getJsonArray());
                        return "{ \"code\":200,\"status\": \"success\"}";
                    }else
                        return "{ \"code\":401,\"status\": \"failed\",\"salary_id\": " + salaryId + "}";
                }else{
                    return "{ \"code\":401,\"status\": \"failed\",\"salary_id\": " + salaryId + "}";
                }
            }
        });

        get("/profile-tax",(request, response) -> {
            String userId = request.queryParams("user_id");
            Tax tax = new Tax();
            if(userId != null){
                UserPajakPendapatan userPajakPendapatan = tax.getProfileTax(userId);
                if(userPajakPendapatan != null)  return new Gson().toJson(userPajakPendapatan, UserPajakPendapatan.class);
                else  return "{ \"code\":401,\"status\": \"failed\",\"user_id\": " + userId + "}";
            }else return "{ \"code\":401,\"status\": \"failed\",\"user_id\": " + userId + "}";
        });

        get("/detail-tax", (request, response) -> {
            String salaryId = request.queryParams("salary_id");
            Tax tax = new Tax();
            if(salaryId != null) {
                Pajak pajak = tax.getDetailTax(Integer.parseInt(salaryId));
                if(pajak != null) return new Gson().toJson(pajak, Pajak.class);
                else return "{ \"code\":401,\"status\": \"failed\",\"salary_id\": " + salaryId + "}";
            }else  return "{ \"code\":401,\"status\": \"failed\",\"salary_id\": " + salaryId + "}";
        });

        get("/list-tax", (request, response) -> {
            String month = request.queryParams("month");
            String unitId = null;
            String sumberDana = null;
            boolean pegawai_luar = false;
//            boolean apbn = false;

            if(request.queryParams("unit_id")!= null)
                unitId = request.queryParams("unit_id");
//            if(request.queryParams("apbn")!=null)
//                apbn = Boolean.valueOf(request.queryParams("apbn"));
            if(request.queryParams("sumber_dana")!=null)
                sumberDana = request.queryParams("sumber_dana");
            if(request.queryParams("pegawai_luar")!=null)
                pegawai_luar = Boolean.valueOf(request.queryParams("pegawai_luar"));

            List<UserPajak> userPajakList = new Tax().getListTax(month,unitId,pegawai_luar,sumberDana);
            String result = new Gson().toJson(userPajakList);
            return result;
        });

        get("/bukti_potong", (request, response) -> {
            String userId = request.queryParams("user_id");
            String result = new Gson().toJson(new Tax().getBuktiPotong(userId), BuktiPotong.class);
            return result;
        });
//
//        delete("/delete-salary", (request, response) -> {
//            String requestId = request.queryParams("request_id");
//            String salaryId = request.queryParams("salary_id");
//            if(requestId != null){
//
//            }else if(salaryId != null){
//
//            }else{
//
//            }
//        });


    }
}
