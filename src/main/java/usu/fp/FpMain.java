package usu.fp;

import com.google.gson.Gson;
import usu.fp.report.TimeRangeService;

import static spark.Spark.get;
import static spark.Spark.port;

public class FpMain {
    public static void main(String[] args){
        port(5002);
        get("/fp-report", (req, res) -> {
            String unitId = req.queryParams("workunit");
            String startTime = req.queryParams("start");
            String endTime = req.queryParams("end");

            TimeRangeService timeRangeService = new TimeRangeService(unitId,startTime,endTime);
            return timeRangeService.getResult();
        });
    }
}
