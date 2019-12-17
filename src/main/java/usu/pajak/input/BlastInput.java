package usu.pajak.input;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import usu.pajak.model.Salary;
import usu.pajak.model.SalaryDetail;
import usu.pajak.model.UserPajak;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BlastInput {
//    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_server")); //connect to mongodb
//    private Datastore datastoreServer = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_server");
    private static MongoClient clientNew = new MongoClient(new MongoClientURI("mongodb://localhost:27017/new_pajak_2019"));
    private Datastore datastoreNew = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(clientNew, "new_pajak_2019");


    public BlastInput() throws IOException, ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
//        Workbook workbook = WorkbookFactory.create(new File("D:/deleteUserPajak.xlsx"));
//        Sheet sheet = workbook.getSheetAt(0);
//        sheet.forEach(row -> {
//            Cell cell = row.getCell(0);
//            Query<UserPajak> query = datastoreServer.createQuery(UserPajak.class).filter("id_user", cell.getStringCellValue().trim());
//            System.out.println(datastoreServer.delete(query).getN());
//        });
//        connectToRkaDb();
//        Query<UserPajak> query = datastoreServer.createQuery(UserPajak.class).disableValidation();
//        query.and(query.criteria("id_user").not().containsIgnoreCase("-"));
//        List<UserPajak> listUPServer = query.asList();
//
        Query<UserPajak> queryNew = datastoreNew.createQuery(UserPajak.class).disableValidation();
        queryNew.and(queryNew.criteria("id_user").not().containsIgnoreCase("-"));
        List<UserPajak> listUPNew = queryNew.asList();
//        ApiRka apiRka = new ApiRka();
//        List<Salary> listSalary = new ArrayList<>();
//        AtomicReference<Salary> s = null;
//        AtomicLong st = new AtomicLong();
//        AtomicLong en = new AtomicLong();

        List<CompletableFuture<Salary>> listComSalary = listUPNew.parallelStream().map(list -> downloadSalary(list.getId_user())).collect(Collectors.toList());

        CompletableFuture<Void> allSal = CompletableFuture.allOf(
          listComSalary.toArray(new CompletableFuture[listComSalary.size()])
        );

        CompletableFuture<List<Salary>> allPag = allSal.thenApply(v -> {
            return listComSalary.stream().map(d -> d.join()).collect(Collectors.toList());
        });

        System.out.println(allPag.get().size());
        long end = System.currentTimeMillis();
        System.out.println((end-start)+"ms.");
//        List<String> webPageLinks = Arrays.asList(...);

        /*long start = System.currentTimeMillis();
        CompletableFuture<Salary> s1 = new CompletableFuture().supplyAsync(() -> {
            try {
//                st.set(System.currentTimeMillis());
                return new Gson().fromJson(
                        callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipt?status=1&user_id=5622", "GET")
                        , Salary.class);
//                en.set(System.currentTimeMillis());

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
//                s1.thenAccept(i -> listSalary.add(i))
                s1.thenRun(()->System.out.println("ms. Id user 1"));
        CompletableFuture<Salary> s2 = new CompletableFuture().supplyAsync(() -> {
            try {
//                st.set(System.currentTimeMillis());
                return new Gson().fromJson(
                        callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipt?status=1&user_id=2415", "GET")
                        , Salary.class);
//                en.set(System.currentTimeMillis());
//                return s.get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
//                s2.thenAccept(i -> listSalary.add(i))
                s2.thenRun(()->System.out.println("ms. Id user 2"));

//        Thread.sleep(18000);
        try {
//            s1.get();
//            s2.get();
            long end = System.currentTimeMillis();
            System.out.println((end-start)+"ms. "+s1.get().toString()+" "+s2.get().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Testr");*/

//        listUPNew.forEach(row-> {
//            try {
//                long start = System.currentTimeMillis();
//                Salary salary = new Gson().fromJson(
//                        apiRka.callApiUsu(
//                                "https://api.usu.ac.id/0.2/salary_receipt?status=1&user_id="+row.getId_user(), "GET")
//                        , Salary.class);
//                long end = System.currentTimeMillis();
//                System.out.println((end-start)+"ms. Id user "+row.getId_user());
//                listSalary.add(salary);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

        /*listUPNew.forEach(row -> {
            BigDecimal total_pph21_dibayar = new BigDecimal(row.getTotal_pph21_usu_dibayar());
            BigDecimal total_pph21_usu = new BigDecimal(row.getTotal_pph21_usu());
            BigDecimal realValue = total_pph21_usu.subtract(total_pph21_dibayar);

            UserPajak target = datastoreServer.createQuery(UserPajak.class).disableValidation()
                    .filter("id_user", row.getId_user()).get();

            BigDecimal t_pph21_dibayar = new BigDecimal(target.getTotal_pph21_usu_dibayar());

            try {

//                long start = System.currentTimeMillis();
                Salary salary = new Gson().fromJson(
                        apiRka.callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipt?status=1&user_id="+row.getId_user(), "GET")
                        , Salary.class);
//                long end = System.currentTimeMillis();
//                System.out.println((end-start)+"ms. Id user "+row.getId_user());

                BigDecimal targetPph21 = BigDecimal.ZERO;
                if(!(salary.getResponse() == null)) {
                    List<SalaryDetail> listSd = salary.getResponse().getSalary_receivers();
                    targetPph21 = listSd.stream().map(r -> {
                        if(!r.getPayment().getAsJsonObject().get("pph21").isJsonNull())
                            return r.getPayment().getAsJsonObject().get("pph21").getAsBigDecimal();
                        else
                            return BigDecimal.ZERO;
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                BigDecimal realTarget = targetPph21.subtract(t_pph21_dibayar);
                if(realValue.compareTo(realTarget) > 0){
                    BigDecimal kurangBayar = realValue.subtract(realTarget);
                    target.setTotal_pph21_kurang_bayar(kurangBayar.toString());
//                    datastoreServer.save(target);
                }else if(realValue.compareTo(realTarget) < 0){
                    BigDecimal lebihBayar = realTarget.subtract(realValue);
                    target.setTotal_pph21_lebih_bayar(lebihBayar.toString());
//                    datastoreServer.save(target);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });*/

    }

    public static Token getSSO(String identity, String password) throws IOException {
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
//        System.out.println(token.getToken());
        return token;
    }

    public String token="";

    public String callApiUsu(String ep, String method) throws IOException {
//        String endpoint = "https://api.usu.ac.id/0.2/salary_receipts";
        URL obj = new URL(ep);
        HttpURLConnection conn= (HttpURLConnection) obj.openConnection();

        if(token.isEmpty()) getSSO("88062916081001","casper14").getToken() ;


        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer "+token);
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( true );
        conn.setDoOutput( true );
        conn.setDoInput(true);

//        DataOutputStream wr;
//        wr = new DataOutputStream(conn.getOutputStream());
//        wr.writeBytes(postData);
//        wr.flush();
//        wr.close();
        StringBuffer response = new StringBuffer();
        if(conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
//            System.out.println(inputLine);
            }
            in.close();
        }else{
            response.append("{ \"code\": 404}");
        }
        return response.toString();
    }

    CompletableFuture<Salary> downloadSalary(String userId){
        CompletableFuture<Salary> s = new CompletableFuture().supplyAsync(() -> {
            try {
                return new Gson().fromJson(
                        callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipt?status=1&user_id="+userId, "GET")
                        , Salary.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        s.thenRun(() -> System.out.println(userId));
        return s;
    }

    public void connectToRkaDb(){
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://api.usu.ac.id/people", "api", "h0ZFqlEaStTsAf4c")) {
//            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost/people", "root", "qwerty123098")) {
            // create a Statement

            try (PreparedStatement stmt = conn.prepareStatement("")) {
                //execute query
                long start = System.currentTimeMillis();
                try (ResultSet rs = stmt.executeQuery("SELECT rka_salary_receipt.*," +
                        "       a.title            AS activity_title,\n" +
                        "       a.source_of_fund,\n" +
                        "       plan.title         AS plan_title,\n" +
                        "       rst.title,\n" +
                        "       rri.salary_type_id AS type_id,\n" +
                        "       rri.salary_counter,\n" +
                        "       rst.component,\n" +
                        "       b.front_degree,\n" +
                        "       b.full_name,\n" +
                        "       b.behind_degree,\n" +
                        "       b.number_of_employee_holding,\n" +
                        "       b.npwp,\n" +
                        "       b.type             AS user_type_id,\n" +
                        "       rg.title           AS user_type_title,\n" +
                        "       d.id               AS unit_id,\n" +
                        "       d.type,\n" +
                        "       d.name             AS unit_name,\n" +
                        "       d.code             AS unit_code,\n" +
                        "       r.id               AS request_id,\n" +
                        "       r.updated_time,\n" +
                        "       r.cheque_number,\n" +
                        "       r.status           AS request_status,\n" +
                        "       a.id               AS activity_id,\n" +
                        "       e.bank_id          AS bank_receiver_id,\n" +
                        "       ex.id              AS expense_account_id,\n" +
                        "       ex.code            AS expense_account_code,\n" +
                        "       ex.title           AS expense_account_title\n" +
                        "FROM people.rka_salary_receipt AS rka_salary_receipt\n" +
                        "         LEFT JOIN people.rka_request_item AS rri ON rri.id = rka_salary_receipt.request_item_id\n" +
                        "         LEFT JOIN people.rka_request AS r ON r.id = rri.request_id\n" +
                        "         LEFT JOIN people.ref_salary_type AS rst ON rst.id = rri.salary_type_id\n" +
                        "         LEFT JOIN people.rka_activity AS a ON a.id = rri.activity_id\n" +
                        "         LEFT JOIN people.ref_expense_account AS ex ON ex.id = a.expense_account_id\n" +
                        "         LEFT JOIN people.rka_plan AS plan ON plan.id = a.plan_id\n" +
                        "         LEFT JOIN people.rka_manager AS manager ON manager.id = plan.manager_id\n" +
                        "         LEFT JOIN people.rka_salary_bank_account AS e ON e.unit_id = manager.unit_id AND e.salary_type_id = rst.id\n" +
                        "         LEFT JOIN simsdm.lecturer AS b ON b.lecturer_id = rka_salary_receipt.user_id\n" +
                        "         LEFT JOIN people.ref_group AS rg ON rg.id = b.type\n" +
                        "         LEFT JOIN people.unit AS d ON d.id = manager.unit_id\n" +
                        "WHERE r.status = 1\n" +
                        "  AND MONTH (r.updated_time) = 11\n" +
                        "ORDER BY rka_salary_receipt.id")) {
                    //position result to first
                    long end = System.currentTimeMillis();
                    System.out.println((end - start)+"ms.");
                    rs.first();
                    System.out.println(rs.getString(1)); //result is "Hello World!"
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        new BlastInput();
//        Logger logger = Logger.getLogger("ApiPajak");
//        FileHandler fh;
//        fh = new FileHandler("D:/ApiPajak3.log");
////            fh = new FileHandler("/home/developer/pajak/ApiPajak.log");
//        logger.addHandler(fh);
//        SimpleFormatter formatter = new SimpleFormatter();
//        fh.setFormatter(formatter);
//        for(int i=1; i<11;i++) {
////            new PnsApbn(i);
////            new PnsApbn(1);
//        new ApiRka(logger).asalData(Integer.toString(i),"2019");
////            new PnsApbn(2);
////        new ApiRka(logger).asalData("2","2019");
////            new PnsApbn(3);
//        }
//        new ApiRka(logger).asalData("3","2019");
       /* String fullNameWithDegree = "Dr.Vita Cita Ernia Tarigan, SH.LLM";
        String b = "Vita Cita Emia Tarigan";
        String result = "-";
//        System.out.println(a.matches(b));
        if(fullNameWithDegree.contains(".") && fullNameWithDegree.contains(",")){
            String[] splitDot = fullNameWithDegree.split("\\.");
            for(int i=0;i< splitDot.length;i++){
                if(splitDot[i].length() > 3){
                    String[] splitComma = splitDot[i].split("\\,");
                    for(int j=0;j<splitComma.length;j++){
                        if(splitComma[j].length()>3) {
                            result = splitComma[j];
//                            return;
                        }
                    }
                }
            }
        }else if(fullNameWithDegree.contains(".")){
            String[] splitDot = fullNameWithDegree.split("\\.");
            for(int i=0;i< splitDot.length;i++){
                if(splitDot[i].length() > 3){
                    result = splitDot[i];
//                    return;
                }
            }
        }else if(fullNameWithDegree.contains(",")){
            String[] splitDot = fullNameWithDegree.split("\\,");
            for(int i=0;i< splitDot.length;i++){
                if(splitDot[i].length() > 3){
                    result = splitDot[i];
//                    return;
                }
            }
        }else{
            result = fullNameWithDegree;
        }
        System.out.println(fullNameWithDegree.matches(".*"+b+".*"));
        System.out.println(b.contains(fullNameWithDegree));*/
    }

}

class Token{
    private String token;

    public String getToken() {
        return token;
    }

}
