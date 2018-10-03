package langotec.numberq.client;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import langotec.numberq.client.dbConnect.StoreDBConn;

public class WelcomeActivity extends AppCompatActivity{

    //Location
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    public static LocationManager lm;
    public static Location currentLocation = null;
    public LocationListener ll;
    public static double lat, lng;

    //DBConn
    private static final String MENU_SERVER =
            "https://ivychiang0304.000webhostapp.com/numberq/menuquery.php";
    private static final String STORE_SERVER =
            "https://flashmage.000webhostapp.com/query.php?p=pass&w=storeList&n=10";
    private StoreDBConn search;
    private MyHandler handler;
    private ArrayList<Store> storeList = new ArrayList<>();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        context = this;

        //Location
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
        //檢查版本和權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        //DBConn
        if (lat == 0.0 || lng == 0.0){
            try{
                currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lat = currentLocation.getLatitude();
                lng = currentLocation.getLongitude();
            }catch (Exception e){
                Log.e("LocationErr",""+e.toString());
            }
        }
        if(handler == null) handler = new MyHandler();
        search = new StoreDBConn();
        search.query(handler, getFilesDir(), lat,lng);
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
        }
        catch(SecurityException sex) {
            Log.e("GPS", "GPS權限失敗..." + sex.getMessage());
            Toast.makeText(this, "GPS權限失敗...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {  // 取消註冊更新的傾聽者物件
            lm.removeUpdates(ll);
        }
        catch(SecurityException sex) {
            Log.e("GPS", "GPS權限失敗..." + sex.getMessage());
            Toast.makeText(this, "GPS權限失敗...", Toast.LENGTH_SHORT).show();
        }
        finish();
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

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location current) {
            if (current != null) {
                currentLocation = current;
                // 取得經緯度
                lat = current.getLatitude();
                lng = current.getLongitude();
                Toast.makeText(context, "經緯度座標變更....", Toast.LENGTH_SHORT).show();
                Log.e("GPS","緯度: " + lat + " 經度: " + lng);
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            Bundle db = msg.getData();
            Boolean isConn = db.getBoolean("isConn");
            Boolean isOk = db.getBoolean("isOk");
            Log.e("storeSearch isOk", String.valueOf(isOk));
            if(isConn){ // 網路已開啟
                if(isOk) {
                    storeList = search.getData();
                    Log.e("store1", storeList.get(1).getBranchName());
                    if (storeList.size() != 0) {
                        startActivity(new Intent()
                                .setClass(context, MainActivity.class)
                                .putExtra("storeList",storeList));
                        finish();
                    }
                }else{
                    Log.e("DBConn","No Data");
                }
            }else{
                // 連線失敗,未開啟網路
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.connFail_noConn))
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(getString(R.string.connFail_check))
                        .setPositiveButton(getString(R.string.connFail_retry), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                search.query(handler, getFilesDir(), lat,lng);
                            }
                        })
                        .setNegativeButton(getString(R.string.connFail_quit), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .create().show();
            }
        }
    }
}
