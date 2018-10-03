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
import okhttp3.Request;
import okhttp3.Response;

public class CustomerDBConn {

    private static CustomerDBConn customer;
    private static final String Q_SERVER ="https://ivychiang0304.000webhostapp.com/numberq/";
    private static final String USERLOGIN_PHP = "userlogin2.php";

    private String email, password;
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
//        try{
//            dbquery.join();
//        }catch (InterruptedException e){
//            Log.e("query interrupted", "query interrupted!!!");
//        }finally {
//            Log.e("query.isUser", String.valueOf(isUser));
//            Message msg = new Message();
//            Bundle bd = new Bundle();
//            bd.putBoolean("isUser", isUser);
//            msg.setData(bd);
//            handler.sendMessage(msg);
//        }
    }

    // 1.使用OKHTTP連線到database,查詢資料
    private class DBQuery extends Thread {
        private OkHttpClientSingleton okHttpClient;
        private Handler hd;

        public DBQuery(Handler handler){
            this.hd = handler;
        }
        @Override
        public void run() {
            // 使用okhttp3建立連線
            // 建立OkHttpClient
            okHttpClient = OkHttpClientSingleton.getInstance();

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
                    Boolean isUser = false;
                    if (response.code() == 200) {   // response.code() return the HTTP status
                        qResult = response.body().string().trim();
                        Log.e("qResult", qResult);
                        if (qResult.equals("no record")) {
                            isUser = false;
                            Log.e("norecord.isUser", String.valueOf(isUser));
                        } else {
                            isUser = true;
                            Log.e("correct.isUser", String.valueOf(isUser));
                            Log.d("OkHttp result", qResult);
                        }
                        createFile(qResult);
                        response.close();
                        Message msg = new Message();
                        Bundle bd = new Bundle();
                        Boolean isConn = true;
                        bd.putBoolean("isConn", isConn);
                        bd.putBoolean("isUser", isUser);
                        msg.setData(bd);
                        hd.sendMessage(msg);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    // 連線失敗,網路未開啟
                    Log.e("failed", " no Data!");
                    Boolean isUser = false;
                    Message msg = new Message();
                    Bundle bd = new Bundle();
                    Boolean isConn = false;
                    bd.putBoolean("isConn", isConn);
                    bd.putBoolean("isUser", isUser);
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
                //Toast.makeText(context, "File saved successfully!", Toast.LENGTH_SHORT).show();
                // 讀取文擋資料
                //readFile(new File(context.getFilesDir().getAbsolutePath(),fName));
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public Member getData(){
        return parseJSON(qResult);
    }

    private Member parseJSON(String s) {
        Log.e("jsonArray","Enter parseJSON");
        try {
            JSONObject jsObj = new JSONObject(s);
            int id = Integer.parseInt(jsObj.getString("id"));
            String userid = jsObj.getString("customerUserId");
            String name = jsObj.getString("userName");
            String phone = jsObj.getString("userPhone");
            String email = jsObj.getString("email");
            String passwd = jsObj.getString("password");
            String gmail = jsObj.getString("google_email");
            String lmail = jsObj.getString("line_email");
            String Femail = jsObj.getString("FB_email");
            member.add(id, userid, name, phone, email, passwd, gmail, lmail, Femail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return member;
    }
}