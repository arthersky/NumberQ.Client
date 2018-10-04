package langotec.numberq.client.dbConnect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import langotec.numberq.client.Store;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class StoreDBConn {

    private static final String Q_SERVER ="https://ivychiang0304.000webhostapp.com/numberq/";
    private static final String USERLOGIN_PHP = "storequery.php";

    private ArrayList<Store> storeList = new ArrayList<Store>(); // 袋子放所有抓出來的資料
    private String lat, lng;
    private File dir;
    private String qResult;

    public void storeDBConn(){   }

    public synchronized void query(Handler handler, File dir, double lat, double lng){
        this.dir = dir;
        this.lat = String.valueOf(lat);
        this.lng = String.valueOf(lng);

        DBQuery dbquery = new DBQuery(handler);
        dbquery.start();
    }

    private class DBQuery extends Thread {
        OkHttpClientSingleton okHttpClient;
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
            FormBody formBody = new FormBody.Builder()
                    .add("p", "pass")
                    .add("w", "storeList")
                    .add("n","10")
                    .add("lat", lat )
                    .add("lng", lng)
                    .build();
            // 建立Request，設置連線資訊
            Request request = new Request.Builder()
                    .url(Q_SERVER + USERLOGIN_PHP)
                    .post(formBody) // 使用post連線
                    .build();
            // 建立Call
            Call call = okHttpClient.newCall(request);
//region

            // 使用okhttp異步方式送出request
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 連線成功
                    Boolean isOk = false;
                    if (response.code() == 200) {   // response.code() return the HTTP status
                        qResult = response.body().string().trim();
                        Log.e("qResult", qResult);
                        Boolean isConn = true;
                        if (qResult.equals("no record")) {
                            isOk = false;
                            Log.e("norecord.isOk", String.valueOf(isOk));
                        } else {
                            isOk = true;
                            Log.e("correct.isOk", String.valueOf(isOk));
                            Log.d("OkHttp result", qResult);
                        }
                        createFile(qResult);
                        response.close();
                        Message msg = new Message();
                        Bundle bd = new Bundle();
                        bd.putBoolean("isOk", isOk);
                        bd.putBoolean("isConn", isConn);
                        msg.setData(bd);
                        hd.sendMessage(msg);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    // 連線失敗
                    Log.e("failed", " no Data!");
                    Boolean isOk = false;
                    Boolean isConn = false;
                    Message msg = new Message();
                    Bundle bd = new Bundle();
                    bd.putBoolean("isOk", isOk);
                    bd.putBoolean("isConn", isConn);
                    msg.setData(bd);
                    hd.sendMessage(msg);
                }
            });
        }
//endregion

        private void createFile(String result) {
            File file = new File(dir, "store.txt");
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

    public ArrayList<Store> getData(){
        return parseJSON(qResult);
    }

    private ArrayList<Store> parseJSON(String s) {
        Log.e("jsonArray","Enter parseJSON");
        Log.e("jsonArray", s);
        try {
            JSONArray jsArray = new JSONArray(s);
            Log.e("jsonArray", String.valueOf(jsArray.length()));
            Log.e("jsonArray", String.valueOf(jsArray.get(0)));
            for (int i=0; i<jsArray.length(); i++) {
                JSONObject jsObj = jsArray.getJSONObject(i);
                Log.e("jsobj", String.valueOf(jsObj));
                String HeadName = jsObj.getString("HeadName");
                int id = Integer.parseInt(jsObj.getString("id"));
                String HeadId = jsObj.getString("HeadId");
                int BranchId = Integer.parseInt(jsObj.getString("BranchId"));
                String BranchName = jsObj.getString("BranchName");
                String City = jsObj.getString("City");
                String Area = jsObj.getString("Area");
                String Address = jsObj.getString("Address");
                String Phone = jsObj.getString("Phone");
                String FAX = jsObj.getString("FAX");
                String opening = jsObj.getString("opening");
                int inService = Integer.parseInt(jsObj.getString("inService"));
                int waitingNumber = Integer.parseInt(jsObj.getString("waitingNumber"));
                double lat = Double.parseDouble(jsObj.getString("lat"));
                double lng = Double.parseDouble(jsObj.getString("lng"));
                Store store = new Store(HeadName, id, HeadId, BranchId, BranchName, City, Area, Address, Phone, FAX, opening, inService, waitingNumber, lat, lng);
                storeList.add(store);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return storeList;
    }
}