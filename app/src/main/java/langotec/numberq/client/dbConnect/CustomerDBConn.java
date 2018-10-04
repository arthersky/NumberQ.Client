package langotec.numberq.client.dbConnect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import langotec.numberq.client.login.Member;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomerDBConn {

    private static CustomerDBConn customer;
    private static final String Q_SERVER ="https://ivychiang0304.000webhostapp.com/numberq/";
    private static final String USERLOGIN_PHP = "userlogin2.php";
    private static final String USERINSERT_PHP = "userinsert.php";
    private static final String USERUPDATE_PHP = "userupdate.php";

    private String uid, email, password, name, phone;
    private File dir;
    private Handler handler;
    private String qResult;
    private Member member = Member.getInstance();

    //==  Constructor   =============================================================
    private CustomerDBConn(){   }

    public static CustomerDBConn getInstance(){
        if(customer == null){
            synchronized(CustomerDBConn.class){
                if(customer == null)
                    customer = new CustomerDBConn();
            }
        }
        return customer;
    }

    //==  Method =================================================================
    public synchronized void query(Handler handler, File dir, String email, String password){
        this.handler = handler;
        this.dir = dir;
        this.email = email;
        this.password = password;
        DBQuery dbquery = new DBQuery(handler);
        dbquery.start();
    }

    public synchronized void insert(Handler handler, File dir, String sname, String phone, String email, String pwd){
        this.handler = handler;
        this.dir = dir;
        this.name = sname;
        this.phone = phone;
        this.email = email;
        this.password = pwd;
        new DBinsert(handler).start();
    }

    public synchronized void update(Handler handler, File dir, String uid, String sname, String phone, String email){
        this.handler = handler;
        this.dir = dir;
        this.uid = uid;
        this.name = sname;
        this.phone = phone;
        this.email = email;
        //this.password = pwd;
        DBupdate dBupdate = new DBupdate(handler);
        dBupdate.start();
        try{
            dBupdate.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }


    // 1.使用OKHTTP連線到database,查詢資料
    private class DBQuery extends Thread {
        private OkHttpClient okHttpClient;
        private Handler hd;

        public DBQuery(Handler handler){
            this.hd = handler;
        }
        @Override
        public void run() {
            // 使用okhttp3建立連線
            // 建立OkHttpClient
            okHttpClient = new OkHttpClient();

            // FormBody放要傳的參數和值
            Log.e("email + pwd", email + "+" + password);
            FormBody formBody = new FormBody.Builder()
                    .add("email", email)
                    .add("pwd", password)
                    .build();
            // 建立Request，設置連線資訊
            Request request = new Request.Builder()
                    .url(Q_SERVER + USERLOGIN_PHP)
                    .post(formBody) // 使用post連線
                    .build();
            // 建立Call
            Call call = okHttpClient.newCall(request);
            // 執行Call連線到網址
//region
           /*
                        //  使用okhttp同步方式下得到返回结果
                        try {
                            Response response = call.execute();
                            if(response.isSuccessful()){
                                if (response.code() == 200) {   // response.code() return the HTTP status
                                        qResult = response.body().string().trim();
                                        Log.e("qResult", qResult);
                                        if (qResult.equals("no record")) {
                                            isUser = false;
                                            Log.e("norecord.isUser", String.valueOf(isUser));
                                            member=null;
                                        } else {
                                            isUser = true;
                                            Log.e("correct.isUser", String.valueOf(isUser));
                                            Log.d("OkHttp result", qResult);
                                            parseJSON(qResult);
                                        }
                                        createFile(qResult);
                                        //response.close();
                                }
                            }else{
                                Log.e("failed", " no Data!");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                     */
//endregion
//region
            // 使用okhttp異步方式送出request
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 連線成功
                    Boolean isOk = false;
                    if (response.code() == 200) {   // response.code() return the HTTP status
                        qResult = response.body().string().trim();
                        Log.e("query.qResult", qResult);
                        if (qResult.equals("no record")) {
                            isOk = false;
                            Log.e("query.isOk", String.valueOf(isOk));
                        } else {
                            isOk = true;
                            Log.e("query.isOk", String.valueOf(isOk));
                            createFile(qResult);
                            //Log.d("OkHttp result", qResult);
                        }
                        response.close();
                        Message msg = new Message();
                        Bundle bd = new Bundle();
                        Boolean isConn = true;
                        bd.putBoolean("isConn", isConn);
                        bd.putBoolean("isOk", isOk);
                        msg.setData(bd);
                        hd.sendMessage(msg);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    // 連線失敗,網路未開啟
                    Log.e("failed", " no Data!");
                    Boolean isOk = false;
                    Message msg = new Message();
                    Bundle bd = new Bundle();
                    Boolean isConn = false;
                    bd.putBoolean("isConn", isConn);
                    bd.putBoolean("isOk", isOk);
                    msg.setData(bd);
                    hd.sendMessage(msg);
                }
            });

            //endregion
        }

        private void createFile(String result) {
            File file = new File(dir, "customer.txt");
            Log.e("outFile", String.valueOf(file));
            if(file.exists()){
                Log.e("file.exists()", "yes");
                file.delete();
            }
            try{
                // 建立應用程式私有文件
                FileOutputStream fOut = new FileOutputStream(file, false);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);  // 若為文字檔,則需多宣告此物件
                // 寫入資料
                osw.write(result);
                osw.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private class DBinsert extends Thread {
        private OkHttpClient okHttpClient;
        private Handler hd;

        public DBinsert(Handler handler) { this.hd = handler; }

        @Override
        public void run() {
            // 使用okhttp3建立連線
            // 建立OkHttpClient
            okHttpClient = new OkHttpClient();

            // FormBody放要傳的參數和值
            Log.e("userdata", name + "+" + phone + "+" + email + "+" + password);
            //Charset charset = Charset.forName(StandardCharsets.UTF_8.name());
            final MediaType FORM_CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
            String param = "sname="+name+"&phone="+phone+"&email="+email+"&pwd="+password;
            // FormBody放要傳的參數和值
            RequestBody formBody = RequestBody.create(FORM_CONTENT_TYPE, param);
//            FormBody formBody = new FormBody.Builder()
//                    .add("sname", name)
//                    .add("phone", phone)
//                    .add("email", email)
//                    .add("pwd", password)
//                    .build();

            // 建立Request，設置連線資訊
            Request request = new Request.Builder()
                    .url(Q_SERVER + USERINSERT_PHP)
                    .post(formBody) // 使用post連線
                    .build();
            // 建立Call
            Call call = okHttpClient.newCall(request);

            // 執行Call連線到網址
            // 使用okhttp異步方式送出request
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 連線成功
                    Boolean isOk = false;
                    if (response.code() == 200) {   // response.code() return the HTTP status
                        qResult = response.body().string().trim();
                        Log.e("insert.qResult", qResult);
                        if (qResult.equals("1")) {
                            isOk = true;
                            Log.e("insertOk", String.valueOf(isOk));
                        } else {
                            isOk = false;
                            Log.e("insertOk", String.valueOf(isOk));
                        }
                        response.close();
                        Message msg = new Message();
                        Bundle bd = new Bundle();
                        Boolean isConn = true;
                        bd.putBoolean("isConn", isConn);
                        bd.putBoolean("isOk", isOk);
                        msg.setData(bd);
                        hd.sendMessage(msg);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    // 連線失敗,網路未開啟
                    Log.e("failed", " no Data!");
                    Boolean isOk = false;
                    Message msg = new Message();
                    Bundle bd = new Bundle();
                    Boolean isConn = false;
                    bd.putBoolean("isConn", isConn);
                    bd.putBoolean("isOk", isOk);
                    msg.setData(bd);
                    hd.sendMessage(msg);
                }
            });
        }
    }

    private class DBupdate extends Thread {
        private Handler hd;

        public DBupdate(Handler handler) { this.hd = handler; }

        @Override
        public void run() {
            // 使用okhttp3建立連線
            // 建立OkHttpClient
            OkHttpClient okHttpClient = new OkHttpClient();

            // FormBody放要傳的參數和值
            //Log.e("userdata", name + "+" + phone + "+" + email + "+" + password);
            final MediaType FORM_CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
            String param = "uid=" + uid + "&sname=" + name + "&phone=" + phone + "&email=" + email; //+ "&pwd=" + password;
            // FormBody放要傳的參數和值
            RequestBody formBody = RequestBody.create(FORM_CONTENT_TYPE, param);
//            FormBody formBody = new FormBody.Builder()
//                    .add("sname", name)
//                    .add("phone", phone)
//                    .add("email", email)
//                    .add("pwd", password)
//                    .build();

            // 建立Request，設置連線資訊
            Request request = new Request.Builder()
                    .url(Q_SERVER + USERUPDATE_PHP)
                    .post(formBody) // 使用post連線
                    .build();
            // 建立Call
            Call call = okHttpClient.newCall(request);

            // 執行Call連線到網址
            // 使用okhttp異步方式送出request
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 連線成功
                    Boolean isOk = false;
                    if (response.code() == 200) {   // response.code() return the HTTP status
                        qResult = response.body().string().trim();
                        Log.e("update.qResult", qResult);
                        if (qResult.equals("1")) {
                            isOk = true;
                            Log.e("updateOk", String.valueOf(isOk));
                        } else {
                            isOk = false;
                            Log.e("updateOk", String.valueOf(isOk));
                        }
                        response.close();
                        Message msg = new Message();
                        Bundle bd = new Bundle();
                        Boolean isConn = true;
                        bd.putBoolean("isConn", isConn);
                        bd.putBoolean("isOk", isOk);
                        msg.setData(bd);
                        hd.sendMessage(msg);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    // 連線失敗,網路未開啟
                    Log.e("failed", " no Data!");
                    Boolean isOk = false;
                    Message msg = new Message();
                    Bundle bd = new Bundle();
                    Boolean isConn = false;
                    bd.putBoolean("update isConn", isConn);
                    bd.putBoolean("update isOk", isOk);
                    msg.setData(bd);
                    hd.sendMessage(msg);
                }
            });
        }
    }

    private void createFile(String result) {
        File file = new File(dir, "customer.txt");
        Log.e("outFile", String.valueOf(file));
        if (file.exists()) {
            Log.e("file.exists()", "yes");
            file.delete();
        }
        try {
            // 建立應用程式私有文件
            FileOutputStream fOut = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);  // 若為文字檔,則需多宣告此物件
            // 寫入資料
            osw.write(result);
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Member getData(){
        return new parseJSON(qResult, member).parse();
    }

//    private Member parseJSON(String s) {
//        Log.e("jsonArray","Enter parseJSON");
//        try {
//            JSONObject jsObj = new JSONObject(s);
//            int id = Integer.parseInt(jsObj.getString("id"));
//            String userid = jsObj.getString("customerUserId");
//            String name = jsObj.getString("userName");
//            String phone = jsObj.getString("userPhone");
//            String email = jsObj.getString("email");
//            String passwd = jsObj.getString("password");
//            String gmail = jsObj.getString("google_email");
//            String lmail = jsObj.getString("line_email");
//            String Femail = jsObj.getString("FB_email");
//            member.add(id, userid, name, phone, email, passwd, gmail, lmail, Femail);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return member;
//    }
}