package langotec.numberq.client.menu;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.fragment.OrderFragment;
import langotec.numberq.client.map.PhpDB;
import langotec.numberq.client.service.OrderCountDown;

public class MakeQRcode extends AppCompatActivity {

// 產生QR code
//========================================================================================
    private ImageView ivQR;
    private Myhandler handler = new Myhandler();
    private static String orderId;
    private static WeakReference<Context> weakReference;
    private static PhpDB phpDB;
    private static OrderHandler orderHandler;
    private static boolean queryState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_qrcode);
        Context context = this;
        weakReference = new WeakReference<>(context);
        queryState = false;
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        setLayout();

        //String tmp = "http://www.pchome.com.tw/";
        //getBitmapFromURL(tmp); // 方法一
        zxingQRcode(orderId);  //方法二
        queryOrder();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        queryState = true;
        finish();
    }

    private void setLayout(){
        // 設定actionbar標題與產生返回鍵圖示
        setTitle("訂單QR code");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ivQR = findViewById(R.id.qrcodeImg);
    }

    // 產生QRcode方法一:利用google提供的API將內容轉換成QR code
    public void getBitmapFromURL(String src) {
        // 利用google提供的API將內容轉換成QR code
        // src是想要轉換的內文,若是中文字需先轉換成Unicode
        final String urlStr = "http://chart.apis.google.com/chart?cht=qr&chs=300x300&chl="+src;
        new Thread(new Runnable(){
            @Override
            public void run() {
                // 從網路上抓取QRcode圖檔回來
                Bitmap myBitmap;
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection)
                            url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    myBitmap = BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                    myBitmap = null;
                }
                Message m=new Message();
                m.obj=myBitmap;
                handler.sendMessage(m);
            }
        }).start();

    }

    // 產生QRcode方法二:利用zxing BarcodeEncoder
    // 需在app的build gradle加入implementation 'com.journeyapps:zxing-android-embedded:3.4.0'
    public void zxingQRcode(String src){
        BarcodeEncoder encoder = new BarcodeEncoder();
        try{
            // QR code 內容編碼
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
            // 設定 QR code 容錯率為 H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // 產生QR code
            Bitmap bitmap = encoder.encodeBitmap(src, BarcodeFormat.QR_CODE, 200, 200);
            ivQR.setImageBitmap(bitmap);
//            showStr(bitmap);
        } catch(WriterException e){
            e.printStackTrace();
        }
    }

    // QRcode加入Logo
    private Bitmap addLogo(Bitmap bitmapCode) {
        Bitmap bitmapLogo = BitmapFactory.decodeResource(getResources(), R.drawable.q_icon_3);
        int qrCodeWidth = bitmapCode.getWidth();
        int qrCodeHeight = bitmapCode.getHeight();
        int logoWidth = bitmapLogo.getWidth();
        int logoHeight = bitmapLogo.getHeight();

        Bitmap blankBitmap = Bitmap.createBitmap(qrCodeWidth, qrCodeHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvas.drawBitmap(bitmapCode, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        float scaleSize = 1.0f;
        while ((logoWidth / scaleSize) > (qrCodeWidth / 5) || (logoHeight / scaleSize) > (qrCodeHeight / 5)) {
            scaleSize *= 2;
        }
        float sx = 1.0f / scaleSize;
        canvas.scale(sx, sx, qrCodeWidth / 2, qrCodeHeight / 2);
        canvas.drawBitmap(bitmapLogo, (qrCodeWidth - logoWidth) / 2, (qrCodeHeight - logoHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }

    private class Myhandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            Bitmap bitmap = (Bitmap)msg.obj;
            ivQR.setImageBitmap(bitmap);
//            showStr(bitmap);
        }
    }

    private static void queryOrder(){
//        orderHandler = null;
//        orderHandler = new OrderHandler();
        if (!queryState) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Looper.myLooper() == null)
                        Looper.prepare();
                    phpDB = new PhpDB(weakReference, new OrderHandler());
                    phpDB.getPairSet().setPairOkHTTP();
                    phpDB.getPairSet().setPairJSON();
                    phpDB.getPairSet().setPairFunction("orderList");
                    phpDB.getPairSet().setPairSearch(1, orderId); //orderID查詢
//                    Log.e("等待資料回應:", new Date().toString());
                    new Thread(phpDB).start();
                    Looper.loop();
                }
            }).start();
        }
    }

    private static class OrderHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
//            Log.e("Handler 發送過來的訊息", msg.obj.toString());
            if (phpDB.getState()) {
                Log.e("資料回應時間", new Date().toString());
                Log.e("回應副程式", phpDB.getPairFunction());
                JSONArray ja = phpDB.getJSONData();
                for (int i = 0; i < ja.length(); i++) {
                    try {
                        JSONObject jsObj = ja.getJSONObject(i);
                        int payCheck = Integer.parseInt(jsObj.optString("payCheck"));
                        Log.e("payCheck", "==================" + payCheck);
                        if (payCheck == 4) {
                            queryState = true;
                            showDialog();
                        }
                        else {
                            Objects.requireNonNull(Looper.myLooper()).quitSafely();
                            queryOrder();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON ERROR", e.toString());
                    }
                }
            }else {
                Objects.requireNonNull(Looper.myLooper()).quitSafely();
                queryOrder();
            }
        }
    }

    private static void showDialog(){
        final Context context = weakReference.get();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.order_scanSuccess))
                .setMessage(context.getString(R.string.order_finish))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(context.getString(R.string.menu_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ((Activity)context).finish();
                                OrderFragment.queryOrder();
                            }
                        }).create().show();
    }

// 反解譯QR code內容,並show出文字
// ==============================================================================================
//    public void showStr(Bitmap img){
//        String result = decodeQRImage(img);
//        TextView tv = findViewById(R.id.orderid_info);
//        tv.setText(result);
//    }

//    public String decodeQRImage(Bitmap image) {
//        String qrResult = "";
//        try {
//            QRCodeDecoder decoder = new QRCodeDecoder();
//            qrResult = new String(decoder.decode(new
//                    MyQRCodeImage(image)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return qrResult;
//    }
//
//    // 實作第三方API: qrcode.jar 所提供的介面
//    class MyQRCodeImage implements QRCodeImage {
//        Bitmap image;
//        public MyQRCodeImage(Bitmap image) {
//            this.image = image;
//        }
//        public int getWidth() {
//            return image.getWidth();
//        }
//        public int getHeight() {
//            return image.getHeight();
//        }
//        public int getPixel(int x, int y) {
//            return image.getPixel(x, y);
//        }
//    }

}
