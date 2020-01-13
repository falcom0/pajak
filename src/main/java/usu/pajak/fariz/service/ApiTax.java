package usu.pajak.fariz.service;

import com.google.gson.Gson;
import usu.pajak.fariz.model.Pajak;
import usu.pajak.fariz.model.Salary;
import usu.pajak.services.ApiRka;

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
//
//        get("/profile-tax",(request, response) -> {
//            String user_id = request.queryParams("user_id");
//
//        });
//
        get("/detail-tax", (request, response) -> {
            String salaryId = request.queryParams("salary_id");
            Tax tax = new Tax();
            if(salaryId != null) {
                String result = new Gson().toJson(tax.getDetailTax(Integer.parseInt(salaryId)), Pajak.class);
                return result;
            }else{
                return "{ \"code\":401,\"status\": \"failed\",\"salary_id\": " + salaryId + "}";
            }
        });
//
//        get("/list-tax", () -> {
//
//        });
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
