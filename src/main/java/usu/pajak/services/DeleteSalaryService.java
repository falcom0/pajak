package usu.pajak.services;

import java.util.logging.Logger;

public class DeleteSalaryService {

    private Logger logger;

    public DeleteSalaryService(Logger logger){
        this.logger = logger;
    }

    public boolean delete(String requestId){
        new ApiRka(logger).deleteSalaryByRequest(requestId);
        return true;
    }

    public boolean deleteBySalary(String salaryId){
        new ApiRka(logger).deleteSalaryById(salaryId);
        return true;
    }
}
