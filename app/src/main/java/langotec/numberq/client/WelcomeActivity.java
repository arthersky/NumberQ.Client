package langotec.numberq.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import langotec.numberq.client.dbConnect.StoreDBConn_OkhttpEnqueue;

public class WelcomeActivity extends AppCompatActivity {

    //Location
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    public static LocationManager lm;
    public static Location currentLocation = null;
    public static Double lat = null, lng = null;
    public LocationListener ll;
    private CountDownTimer timer;
    private Context context;
    private StoreDBConn_OkhttpEnqueue storeDBConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        context = this;
        GPSinitSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ll = new MyLocationListener();
        int minTime = 1000; // 毫秒
        float minDistance = 1; // 公尺
        try {  // 註冊更新的傾聽者物件
            lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, ll);
            lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime, minDistance, ll);
        } catch (SecurityException sex) {
            Log.e("GPS", "GPS權限失敗" + sex.getMessage());
            Toast.makeText(this, "GPS權限失敗...使用預設位置", Toast.LENGTH_SHORT).show();
        }
        storeDBConn = new StoreDBConn_OkhttpEnqueue(context);
        countDownTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {  // 取消註冊更新的傾聽者物件
            lm.removeUpdates(ll);
        } catch (SecurityException sex) {
            Log.e("GPS", "GPS權限失敗..." + sex.getMessage());
            Toast.makeText(this, "GPS權限失敗...", Toast.LENGTH_SHORT).show();
        }
        timer.cancel();
        if (storeDBConn.call != null && storeDBConn.call.isExecuted())
            storeDBConn.call.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 已經取得權限
                Toast.makeText(this, "取得權限取得GPS資訊",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "直到取得權限, 否則無法取得GPS資訊",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void GPSinitSetting() {
        //Location
        //檢查版本和權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
        // 取得定位服務的LocationManager物件
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 顯示對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.locationManager)
                    .setMessage(R.string.locationMessage)
                    .setPositiveButton(R.string.setPositiveButton, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 使用Intent物件啟動設定程式來更改GPS設定
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(R.string.setNegativeButton, null).create().show();
        }
    }

    private void countDownTimer() {
        //計算10秒內沒衛星定位就用預設位置(台北車站)
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                Log.e("lat", String.valueOf(lat));
                Log.e("lng", String.valueOf(lng));
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onFinish() {
                if (lat == null || lng == null){
                    lat = 25.0451;
                    lng = 121.517;
                    Log.e("lat", String.valueOf(lat));
                    Log.e("lng", String.valueOf(lng));
                }
                //DBConn
                storeDBConn.setLatLng(String.valueOf(lat), String.valueOf(lng));
                storeDBConn.okhttpConn();
                lm.removeUpdates(ll);
            }
        }.start();
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location current) {
            if (current != null) {
                currentLocation = current;
                // 取得經緯度
                lat = current.getLatitude();
                lng = current.getLongitude();
                Toast.makeText(context, "經緯度座標變更....", Toast.LENGTH_SHORT).show();
                Log.e("GPS","緯度: " + lat + " 經度: " + lng);
                timer.onFinish();
                timer.cancel();
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
