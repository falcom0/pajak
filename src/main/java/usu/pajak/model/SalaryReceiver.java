package usu.pajak.model;

import java.util.ArrayList;
import java.util.List;

public class SalaryReceiver{
    private List<SalaryDetail> salary_receivers;

    public List<SalaryDetail> getSalary_receivers() {
        return salary_receivers == null ? new ArrayList<>() : salary_receivers;
    }
}
