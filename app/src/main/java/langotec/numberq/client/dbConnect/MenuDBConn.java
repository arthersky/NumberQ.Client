package langotec.numberq.client.dbConnect;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import langotec.numberq.client.WelcomeActivity;
import langotec.numberq.client.menu.Menu;
import langotec.numberq.client.menu.MenuActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MenuDBConn extends AsyncTask<Void, Void, Void> {
    private String qResult = "no record";
    private String storeName;
    private WeakReference<Context> activityReference;
    private ArrayList<Menu> menuList = new ArrayList<>(); // 袋子放所有抓出來的資料
    private static final String Q_SERVER_MENU = "https://ivychiang0304.000webhostapp.com/numberq/menuquery.php";

    public MenuDBConn(String storeName, Context context) {
        this.storeName = storeName;
        activityReference = new WeakReference<>(context);
    }

    //  region AsyncTask Overrides
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = activityReference.get();
        Intent intent = new Intent();
        intent.putExtra("isFirst", false);
        intent.setClass(context, WelcomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(Void... values) {

        OkHttpClient okHttpClient = new OkHttpClient();

        // FormBody放要傳的參數和值
        FormBody formBody = new FormBody.Builder()
                .add("sname", storeName)
                .build();

        // 建立Request，設置連線資訊
        Request request = new Request.Builder()
                .url(Q_SERVER_MENU)
                .post(formBody) // 使用post連線
                .build();

        // 建立Call
        Call call = okHttpClient.newCall(request);

        // 執行Call連線到網址
        try {
            Response response = call.execute();//call.execute為同步工作
            if (response.isSuccessful() && response.code() == 200) {
                //同步方式下得到返回结果
                qResult = response.body().string().trim();
                if (qResult.equals("no record")) {
                    Log.d("OkHttp result", "no record");
                } else {
//                    Log.d("OkHttp result", qResult + "");
                }
                response.close();
            } else {
                Log.e("failed", " no Data!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Context context = activityReference.get();
        if (!qResult.equals("no record")) {
            Intent intent = new Intent();
            intent.putExtra("menuList", parseJSON());
            intent.setClass(context, MenuActivity.class);
            context.startActivity(intent);
        } else {
            ((Activity)WelcomeActivity.context).finish();
            showDialog();
        }
    }
//endregion

    private void showDialog() {
        Context context = activityReference.get();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提醒訊息")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("目前無法連線，請檢查您的網路設定，謝謝您")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private ArrayList<Menu> parseJSON() {
//        Log.e("jsonArray", "Enter parseJSON");
//        Log.e("jsonArray", qResult);
        try {
            JSONArray jsArray = new JSONArray(qResult);
//            Log.e("jsonArray", String.valueOf(jsArray.length()));
//            Log.e("jsonArray", String.valueOf(jsArray.get(0)));
            for (int i = 0; i < jsArray.length(); i++) {
                JSONObject jsObj = jsArray.getJSONObject(i);
//                Log.e("jsobj", String.valueOf(jsObj));
                String HeadName = jsObj.getString("HeadName");
                String HeadId = jsObj.getString("HeadId");
                String productId = jsObj.getString("productId");
                String productType = jsObj.getString("productType");
                String productName = jsObj.getString("productName");
                String price = jsObj.getString("price");
                String image = jsObj.getString("image");
                boolean available = Integer.parseInt(jsObj.getString("available")) != 0;
                String description = jsObj.getString("description");
                Menu menu = new Menu(HeadName, HeadId, productId, productType, productName, price, image, available, description);
                menuList.add(menu);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return menuList;
    }
}