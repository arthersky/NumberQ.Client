package langotec.numberq.client.dbConnect;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.Store;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StoreDBConn_OkhttpEnqueue {

    public Call call;
    private static final String Q_SERVER =
            "https://ivychiang0304.000webhostapp.com/numberq/storequery.php";
    private String qResult, lat, lng;
    private ArrayList<Store> storeList = new ArrayList<>();
    private WeakReference<Context> activityReference;

    public StoreDBConn_OkhttpEnqueue(Context context){
        activityReference = new WeakReference<>(context);
    }

    public void setLatLng(String lat, String lng){
        this.lat = lat;
        this.lng = lng;
    }

    public void okhttpConn(){
        final Context context = activityReference.get();
        OkHttpClient okHttpClient = new OkHttpClient();
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
                .url(Q_SERVER)
                .post(formBody) // 使用post連線
                .build();
        // 建立Call
        call = okHttpClient.newCall(request);

        // 使用okhttp異步方式送出request
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 連線成功
                if (response.code() == 200) {   // response.code() return the HTTP status
                    qResult = response.body().string().trim();
                    if (qResult.equals("no record")) {
                        // enqueue時是在background Thread上，無法直接叫用main Thread
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog();
                            }
                        });
                    } else{
                        Intent intent = new Intent();
                        intent.putExtra("storeList", parseJSON(qResult));
                        intent.setClass(context, MainActivity.class);
                        context.startActivity(intent);
                        ((Activity)context).finish();
                    }
                    response.close();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                // 連線失敗
                if (!((Activity) context).isFinishing()) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                        }
                    });
                }
            }
        });
    }

    private void showDialog() {
        Context context = activityReference.get();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.connFail_noConn))
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(context.getString(R.string.connFail_check))
                .setPositiveButton(context.getString(R.string.connFail_retry),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        call.cancel();
                        okhttpConn();
                    }
                })
                .setNegativeButton("離開", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((Activity)activityReference.get()).finish();
                    }
                }).create().show();
    }

    private ArrayList<Store> parseJSON(String s) {
        try {
            JSONArray jsArray = new JSONArray(s);
            for (int i=0; i<jsArray.length(); i++) {
                JSONObject jsObj = jsArray.getJSONObject(i);
                String HeadName = jsObj.getString("HeadName");
                String headImg = jsObj.getString("HeadImg");
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
                Store store = new Store(HeadName, headImg, id, HeadId, BranchId, BranchName, City, Area, Address, Phone, FAX, opening, inService, waitingNumber, lat, lng);
                storeList.add(store);
//                Log.e("Store.ImgURL",headImg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return storeList;
    }
}
