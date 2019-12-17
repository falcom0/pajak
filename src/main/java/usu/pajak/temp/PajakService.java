package usu.pajak.temp;

import usu.pajak.model.Salary;
import usu.pajak.model.SalaryDetail;

import java.util.List;
import java.util.stream.Collectors;

public class PajakService {
    public void filterSalary(Salary salary){
        if(salary != null && salary.getResponse().getSalary_receivers().size()>0){
            List<SalaryDetail> totalData = salary.getResponse().getSalary_receivers();
            List<SalaryDetail> gaji = totalData.stream()
                    .filter(c -> c.getPayment().getAsJsonObject().has("basic_salary"))
                    .collect(Collectors.toList());
            List<SalaryDetail> honorGaji = totalData.stream()
                    .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                    .filter(c -> (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
                            && c.getPayment().getAsJsonObject().has("p1"))
                    .collect(Collectors.toList());
            List<SalaryDetail> honor = totalData.stream()
                    .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                    .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
                            && c.getPayment().getAsJsonObject().has("p1")))
                    .collect(Collectors.toList());


        }else{

        }
    }
}
