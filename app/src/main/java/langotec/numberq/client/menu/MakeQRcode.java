package langotec.numberq.client.menu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import langotec.numberq.client.R;

public class MakeQRcode extends AppCompatActivity {

// 產生QR code
//========================================================================================
    private ImageView ivQR;
    private Myhandler handler = new Myhandler();

    private class Myhandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            Bitmap bitmap = (Bitmap)msg.obj;
            ivQR.setImageBitmap(bitmap);
//            showStr(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_qrcode);
        setLayout();
        Intent intent = getIntent();
        String tmp = intent.getStringExtra("orderId");

        //String tmp = "http://www.pchome.com.tw/";
        //getBitmapFromURL(tmp); // 方法一
        zxingQRcode(tmp);  //方法二

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLayout(){
        // 設定actionbar標題與產生返回鍵圖示
        setTitle("訂單QR code");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
            // 設定 QR code 容錯率為 H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // 產生QR code
            Bitmap bitmap = encoder.encodeBitmap(src, BarcodeFormat.QR_CODE, 500, 500);
            ivQR.setImageBitmap(addLogo(bitmap));
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
