package langotec.numberq.client.login;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class CustomerDBConn{
    private static final String server ="https://ivychiang0304.000webhostapp.com/numberq/";
    private static final String queryphp = "userlogin2.php";
    private ArrayList<HashMap<String, Object>> itemList; // 袋子放所有抓出來的資料
    private HashMap<String, Object> newItem;
    private String inemail, inpassword;
    private Context context;
    private boolean isUser = false;

    public CustomerDBConn(){

    }

    public CustomerDBConn(Context context) {
        Log.e("constructor", "Entering CustomerDBConn");
        this.context = context;

    }

    public boolean query(String email, String password){
        this.inemail = email;
        this.inpassword = password;
        DBQuery dbquery = new DBQuery();
        dbquery.start();
        try{
            dbquery.join();
        }catch (InterruptedException e){
            Log.e("query interrupted", "query interrupted!!!");
        }finally {
            Log.e("query.isUser", String.valueOf(isUser));
            return isUser;
        }
    }

    private class DBQuery extends Thread {
        AndroidHttpClient androidHttpClient = null;
        @Override
        public void run() {
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            //pairs.add(new BasicNameValuePair("p", "pass"));
            //pairs.add(new BasicNameValuePair("w", "userSearchByEMAIL"));
            pairs.add(new BasicNameValuePair("email", inemail )); //將參數名稱+參數值存入AyList
            pairs.add(new BasicNameValuePair("pwd", inpassword )); //將參數名稱+參數值存入AyList
            androidHttpClient = null;
            try {
                HttpResponse response = connectDB(pairs);
                final String status = response.getStatusLine().toString(); // 取得執行後的狀態

                if(status.split(" ")[1].equals("200")) {
                    // br每次讀取一行資料
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String readLine, qResult=null;
                    final StringBuilder result = new StringBuilder();
                    itemList = new ArrayList<HashMap<String, Object>>();
                    while ((readLine = br.readLine()) != null) {
                        //isUser = true;
                        //Log.e("Result", readLine);
                        result.append(readLine + "\n");
//                        HashMap<String, Object> item = new HashMap<String, Object>();
//                        String[] data = readLine.split(","); // 每行資料之間的值是以","分隔;現在要將值拆開分別存到string陣列中
//                        item.put("id", data[0]);
//                        item.put("customerUserId", data[1]);
//                        item.put("userName", data[2]);
//                        item.put("userPhone", data[3]);
//                        item.put("email", data[4]);
//                        item.put("password", data[5]);
//                        item.put("google_email", data[6]);
//                        item.put("line_email", data[7]);
//                        item.put("FB_email", data[8]);
//                        itemList.add(item);
                    }

                    qResult = result.toString().trim();
                    if (qResult.equals("no record")) {
                        isUser = false;
                        File dir = context.getFilesDir();
                        Log.e("context.getFilesDir()", String.valueOf(context.getFilesDir()));
                        File outFile = new File(dir, "customer.txt");
                        Log.e("outFile", String.valueOf(outFile));
                        createFile(outFile, qResult);
                    } else {
                        isUser = true;
                        File dir = context.getFilesDir();
                        Log.e("context.getFilesDir()", String.valueOf(context.getFilesDir()));
                        File outFile = new File(dir, "customer.txt");
                        Log.e("outFile", String.valueOf(outFile));
                        createFile(outFile, qResult);
                    }
                } else{
                    Log.e("failed"," no Data!");
                }
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                if(androidHttpClient != null){
                    androidHttpClient.close();
                    androidHttpClient = null;
                }
                Log.e("androidHttpClient", String.valueOf(androidHttpClient));
            }
        }

        public HttpResponse connectDB(ArrayList<NameValuePair> pairs){
            String user_agent = System.getProperty("http_agent"); // 取得瀏覽器
            HttpResponse response=null;
            if(androidHttpClient == null)  // 若無瀏覽器則建立一個新的
                androidHttpClient = AndroidHttpClient.newInstance(user_agent);
            HttpPost httppost = new HttpPost(server + queryphp);
            try{
                httppost.setEntity(new UrlEncodedFormEntity(pairs));
                response = androidHttpClient.execute(httppost);
                return response;
            }catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        private void createFile(File file, String result) {
            if(file.exists()){
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
}
