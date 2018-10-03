package langotec.numberq.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WelcomeActivity extends AppCompatActivity{

    private static final String MENU_SERVER =
            "https://ivychiang0304.000webhostapp.com/numberq/menuquery.php";
    private static final String STORE_SERVER =
            "https://flashmage.000webhostapp.com/query.php?p=pass&w=storeList&n=10";
    public static Context context;
    boolean isFirst;
    private String qResult = "no record";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        context = this;
        isFirst = true;
        isFirst = getIntent().getBooleanExtra("isFirst", isFirst);
        if (isFirst) {
            new OkHttpHandler().execute(MENU_SERVER);
//        new OkHttpHandler().execute(STORE_SERVER);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提醒訊息")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("目前無法連線，請檢查您的網路設定，謝謝您")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new OkHttpHandler().execute(MENU_SERVER );
                    }
                })
                .setNegativeButton("離開", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .create().show();
    }

    private class OkHttpHandler extends AsyncTask <String, Void, Void> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected Void doInBackground(String... urls) {

            OkHttpClient okHttpClient = new OkHttpClient();

            //String... urls傳進來的是陣列，所以用for迴圈跑完全部的內容
            for (String url : urls) {

                // FormBody放要傳的參數和值
                FormBody formBody = new FormBody.Builder()
                        .add("sname", "鼎泰豐")
                        .build();

                // 建立Request，設置連線資訊
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody) // 使用post連線
                        .build();

                // 建立Call
                Call call = okHttpClient.newCall(request);

                // 執行Call連線到網址
                try {
                    Response response = call.execute();//call.execute為同步工作
                    if (response.isSuccessful() && response.code() == 200) {
                        //同步方式下得到返回结果
                        // response.code() return the HTTP status
                        qResult = response.body().string().trim();
                        if (qResult.equals("no record")) {
                            Log.d("OkHttp result", "no record");
                        } else {
//                            Log.d("OkHttp result", qResult);
                        }
//                    createFile(qResult);
                        response.close();
                    } else {
                        Log.e("failed", " no Data!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (!qResult.equals("no record")) {
                startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
                finish();
            }else{
                showDialog();
            }

        }
    }
}
