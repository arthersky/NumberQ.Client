package langotec.numberq.client.map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import langotec.numberq.client.R;


public class Activity_GoogleMap extends AppCompatActivity {
    public static final int FUNCTION_STORENEAR = 1;
    public static final int FUNCTION_STORELIST = 2;
    public static final int FUNCTION_STORESEARCH = 3;
    public static final int FUNCTION_MAP_LOCATION = 4;
    private int functionWork =-1;
    private MapView mapView;
    private GoogleMap gMap;
    private MyGoogleMapAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button bt1; private Button bt2; private Button bt3;
    private ProgressBar progressBar;
    Context context;
    boolean bLoc = false;
    double lat=-1, lng=-1;
    double tlat=-1,tlng=-1;
    boolean showRecycleView=true;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    Animation animation;

    private ArrayList<PhpDB.ItemListRow> itemStoreList;
    private HashMap<String,Object> newStoreItem = new HashMap<>();
    private ListView itemListView ;
    private SimpleAdapter adapter;
    private int id,position;
    FloatingActionButton fab;
    Marker userLocMark = null;
    Marker storeMarkList[] = null;

    private LinearLayout mapinfo_panel;
    private TextView mapinfo;
    private String searchString;
    //------------------------

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定在狀態列可以顯示處理中圖示 但是不知道為何沒出現
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        setProgressBarVisibility(true);
        setProgress(4500);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();

        setContentView(R.layout.layout_googlemap);
        context = this;

        //取出上次位置
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lat = location.getLatitude();
        lng = location.getLongitude();

        //連結Button功能
        bt1 = (Button) findViewById(R.id.gmap_button1);
        bt2 = (Button) findViewById(R.id.gmap_button2);
        bt3 = (Button) findViewById(R.id.gmap_button3);
        animation= AnimationUtils.loadAnimation(Activity_GoogleMap.this, R.anim.myanim);
        animation.setDuration(20000); //20秒的動畫!!

        fab = (FloatingActionButton) findViewById(R.id.Map_fab_Location_btn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fab.setEnabled(false);
        fab.startAnimation(animation); //開始動畫
        progressBar.setVisibility(ProgressBar.VISIBLE);

        bt1.setOnClickListener(new btnClickListener());
        bt2.setOnClickListener(new btnClickListener());
        bt3.setOnClickListener(new btnClickListener());

        //返回上一頁功能
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //GoogleMap啟動
        mapView = (MapView) findViewById(R.id.map_googlemap);
        mapView.onCreate(savedInstanceState);
        gMap = mapView.getMap();
        setGoogleMapSnippet(); //Map Mark可多行設定

        //啟動顯示效果
        //getWindow().setEnterTransition(new Slide().setDuration(2000)); //滑入轉場動畫
        //getWindow().setEnterTransition(new Explode().setDuration(2000)); //轉場動畫
        getWindow().setEnterTransition(new Fade().setDuration(2000)); //淡入淡出轉場動畫

        addFloatingActionButton();

        //取得上一頁傳來命令 work=1 附近定位
        Bundle bundle = getIntent().getExtras();

        //取出上次位置 上一頁有傳過來則使用
        if (bundle.getDouble("lat") == 0 || bundle.getDouble("lng") == 0 )
        {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lat = location.getLatitude();
            lng = location.getLongitude();
            Log.e("上次經緯度資料","Lat:" + lat+" lng:"+lng);
        }else{
            lat =bundle.getDouble("lat");
            lng =bundle.getDouble("lng");
            Log.e("使用上一頁傳來經緯度資料","Lat:" + lat+" lng:"+lng);
        }
        moveToCurrentLocation();

        //取得上一頁傳來命令 work=1 附近定位
        if (bundle.getInt("work") == FUNCTION_STORENEAR) {
            bLoc = true;
            Log.e("onCreate","執行上一頁傳來搜尋附近店家命令");
            functionWork = FUNCTION_STORENEAR;
            refresh_Record(10, FUNCTION_STORENEAR, false);

        }else{
            fab.setEnabled(false);
            fab.startAnimation(animation); //開始動畫
            bLoc = false;

        }
    }

    //Google Map相關===================================================
    //重新設定Map Mark備註資料使其可以印出多行
    public void setGoogleMapSnippet()
    {
        gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    //在地圖上標示店家位置
    public void makeStoreListOnMap()
    {
        if (null != itemStoreList) {
            //已經有mark的話作清除動作
            if (userLocMark != null) {userLocMark.remove(); userLocMark=null;}
            userLocMark = gMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng))));

            //移除已經標示的商店
            if (storeMarkList != null) {for(int i=0;i< storeMarkList.length;i++) {storeMarkList[i].remove();storeMarkList[i]=null;}}
            storeMarkList = new Marker[itemStoreList.size()];

            for (int i = 0; i < itemStoreList.size(); i++) {
                //計算距離
                tlat = Double.parseDouble(((PhpDB.ItemListRow) itemStoreList.get(i)).get("lat").toString());
                tlng = Double.parseDouble(((PhpDB.ItemListRow) itemStoreList.get(i)).get("lng").toString());

                String range = "" + GetDistance(tlat, tlng, lat, lng);
                storeMarkList[i] = gMap.addMarker(new MarkerOptions().position(new LatLng(tlat, tlng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(((PhpDB.ItemListRow) itemStoreList.get(position)).get("HeadName").toString() + " " + ((PhpDB.ItemListRow) itemStoreList.get(position)).get("BranchName").toString()).snippet("住址："+ ((PhpDB.ItemListRow)itemStoreList.get(position)).get("City")+((PhpDB.ItemListRow)itemStoreList.get(position)).get("Area")+((PhpDB.ItemListRow)itemStoreList.get(position)).get("Address") +"\n電話："+ ((PhpDB.ItemListRow)itemStoreList.get(position)).get("Phone")+"\n距離：" + range + " 公尺"));
            }
        }
    }

    //Google Map相關=====  end ==============================================

    private void makeRecyclerView() {
        //--------------------------RecyclerView------------------------------
        ArrayList<String> myDataset = new ArrayList<>();
        if (null==itemStoreList) {Log.e("makeRecyclerView","==============重大錯誤!!沒有資料!!=============");return;}

        Log.e("RecycleView","Size:"+itemStoreList.size());
        for (int i = 0; i < itemStoreList.size(); i++) {
            //Log.e("test",(((HashMap<String, Object>)(itemStoreList.get(i)).get("HeadName")).toString()+" "+((HashMap<String, Object>)(itemStoreList.get(i)).get("BranchName")).toString()));
            //Log.e("makeRecyclerView","itemStoreList(i): "+i +" = " + ((PhpDB.ItemListRow) itemStoreList.get(i)).getAll().toString());

            //計算距離
            tlat = Double.parseDouble(((PhpDB.ItemListRow) itemStoreList.get(i)).get("lat").toString());
            tlng = Double.parseDouble(((PhpDB.ItemListRow) itemStoreList.get(i)).get("lng").toString());

            String range =""+GetDistance(tlat,tlng,lat,lng);
            //Log.e("makeRecyclerView","range:"+tlng+","+tlat+" / " +lng +","+lat +"=" + range);
            myDataset.add("店名：" + ((PhpDB.ItemListRow)itemStoreList.get(i)).get("HeadName") + " "+ ((PhpDB.ItemListRow)itemStoreList.get(i)).get("BranchName") +"\n住址："+ ((PhpDB.ItemListRow)itemStoreList.get(i)).get("City")+((PhpDB.ItemListRow)itemStoreList.get(i)).get("Area")+((PhpDB.ItemListRow)itemStoreList.get(i)).get("Address") +"\n電話："+ ((PhpDB.ItemListRow)itemStoreList.get(i)).get("Phone")+"\n距離："+range + " 公尺"  );
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view_GoogleMap);
        //mRecyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.VERTICAL));
        mAdapter = new MyGoogleMapAdapter(myDataset);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        if (showRecycleView==true) mRecyclerView.setVisibility(View.VISIBLE);
        else mRecyclerView.setVisibility(View.INVISIBLE);
        Log.e("ShowRecycle",""+showRecycleView);
    }

    //button的點選事件
    class btnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //附近店家
                case R.id.gmap_button1:
                    Log.e("button1","button1");
                    bLoc =false;
                    if (null != mRecyclerView && mRecyclerView.getVisibility() == View.VISIBLE)
                    {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        functionWork = FUNCTION_STORENEAR;
                        refresh_Record(10,FUNCTION_STORENEAR,true);
                    }

                    break;
                //所有店家
                case R.id.gmap_button2:
                    Log.e("button2","button2");
                    bLoc =false;
                    if (null != mRecyclerView && mRecyclerView.getVisibility() == View.VISIBLE)
                    {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        functionWork = FUNCTION_STORELIST;
                        refresh_Record(FUNCTION_STORELIST);
                    }
                    break;

                //搜尋店家
                case R.id.gmap_button3:
                    Log.e("button3","button3");
                    bLoc =false;
                    if (null != mRecyclerView && mRecyclerView.getVisibility() == View.VISIBLE)
                    {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        functionWork = FUNCTION_STORESEARCH;
                        refresh_Record(FUNCTION_STORESEARCH);
                    }
                    break;
                default:
                    Log.e("buttonX","沒有觸發對應功能!!");
                    break;
            }
        }
    }

    private void addFloatingActionButton()
    {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(userLocMark != null) {userLocMark.remove();userLocMark = null;} //移除mark
                userLocMark = gMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng))));
                setProgressBarIndeterminateVisibility(true); // turn progress on
                setProgressBarVisibility(true);
                fab.setEnabled(false);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, myLocationListener, null);
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, myLocationListener, null);
                fab.startAnimation(animation);

            }
        });
    }

    //移到目前座標
    private void moveToCurrentLocation(){
        Log.e("移動地圖攝影機","lat=" +lat+",lng="+lng);
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(16).bearing(0).tilt(25).build()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // 顯示處理中圖示
        setProgressBarIndeterminateVisibility(true);
        setProgressBarVisibility(true);
        //讀取定位權限
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        // 執行註冊，設備是GPS，只會讀取一次位置資訊
        //強制不執行定位
        if (bLoc ==true )  {
            moveToCurrentLocation();
            return;
        }


        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, myLocationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, myLocationListener, null);
        }
        else
        {
            //取出上次位置
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lat = location.getLatitude();
            lng = location.getLongitude();

            if (userLocMark != null) {userLocMark.remove(); userLocMark=null;}
            userLocMark = gMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng))));
            moveToCurrentLocation();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        // 移除LocationListener
        locationManager.removeUpdates(myLocationListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        moveToCurrentLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //到目前位置
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            StringBuffer sb = new StringBuffer("Location Information:\n");
            if(location != null){
                lat =location.getLatitude();
                lng =location.getLongitude();
                Log.e("定位取得座標","lat=" + lat+ " ,lng="+lng);
                bLoc =true;

                if (userLocMark != null) {userLocMark.remove(); userLocMark=null;}

                userLocMark = gMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng))));
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(16).bearing(0).tilt(25).build()));
                fab.clearAnimation();;
                fab.setEnabled(true);

            }
            // 關閉處理中圖示
            setProgressBarVisibility(false);
            setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onProviderDisabled(String provider) {   }

        @Override
        public void onProviderEnabled(String provider) {    }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {     }

    }

    public class MyGoogleMapAdapter extends RecyclerView.Adapter<MyGoogleMapAdapter.ViewHolder> {
        private List<String> mData;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ViewHolder(View v) {
                super(v);

                this.mTextView = (TextView) v.findViewById(R.id.info_text);
            }
        }

        public MyGoogleMapAdapter(List<String> data) {
            this.mData = data;

        }

        @Override
        public MyGoogleMapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_googlemap_listview_row, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        //mRecyclerView 上面項目點選事件
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.mTextView.setText(mData.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PhpDB.ItemListRow)itemStoreList.get(position)).get("HeadName");
                    //Toast.makeText(context, ((PhpDB.ItemStoreRow)itemStoreList.get(position)).get("HeadName") + " " +((PhpDB.ItemStoreRow)itemStoreList.get(position)).get("lat") +","+ ((PhpDB.ItemStoreRow)itemStoreList.get(position)).get("lng") + " Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();

                    tlat = Double.parseDouble(((PhpDB.ItemListRow)itemStoreList.get(position)).get("lat").toString());
                    tlng = Double.parseDouble(((PhpDB.ItemListRow)itemStoreList.get(position)).get("lng").toString());

                    String range =""+GetDistance(tlat,tlng,lat,lng);
                    gMap.addMarker(new MarkerOptions().position(new LatLng (tlat,tlng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title( ((PhpDB.ItemListRow)itemStoreList.get(position)).get("HeadName").toString() +" "+ ((PhpDB.ItemListRow)itemStoreList.get(position)).get("BranchName").toString()  ).snippet("距離："+range + " 公尺"));
                    gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(tlat,tlng)).zoom(16).bearing(0).tilt(25).build()));

                    mRecyclerView.setVisibility(View.INVISIBLE);

                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Toast.makeText(context, "Item " + position + " is long clicked.", Toast.LENGTH_SHORT).show();

                    mRecyclerView.setVisibility(View.INVISIBLE);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.mData.size();
        }
    }


    private String getAddress(LatLng point) {
        String result ="Unknow";
        List<Address> addresses =null;
        // 建立Geocoder物件
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            // 查詢地址
            addresses = geocoder.getFromLocation(point.latitude,point.longitude,1);
            if(addresses.size()>0) {
                // 取得第一個地址物件
                Address addr =addresses.get(0);
                // 設定回傳內容為地址
                result = addr.getAddressLine(0);
            }
        }
        catch(IOException e)
        {
            Log.d("Geocoder01",e.toString());
        }
        return result;
    }

    //===========================資料庫=======================================

    private void insertSQLRecord()
    {
        //    new InsertSQL().start();
    }

    private void UpdateSQLRecord()
    {
        //    new UpdateSQL().start();
    }

    PhpDB db;
    @SuppressLint("HandlerLeak")
    protected Handler hd = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.e("呼叫方Handler 收到" ,"狀態:" + db.getState() + "\nHandler 發送過來的訊息：" + msg.obj);

            if(db.getState() == true) {

                itemStoreList = db.getDataSet();

                //實驗性質
                switch(functionWork) {
                    case FUNCTION_STORENEAR:
                        Log.e("執行 makeRecyclerView", "" + functionWork);
                        makeRecyclerView();
                        makeStoreListOnMap();
                        break;
                    case FUNCTION_STORELIST:
                        Log.e("執行 makeRecyclerView", "" + functionWork);
                        makeRecyclerView();
                        makeStoreListOnMap();
                        break;
                    case FUNCTION_STORESEARCH:
                        Log.e("執行 makeRecyclerView", "" + functionWork);
                        makeRecyclerView();
                        makeStoreListOnMap();
                        break;
                    default:
                        Log.e("handleMessage","未知的命令");
                        break;
                }

            }
        }
    };



    private void refresh_Record()
    {
        refresh_Record(10,0,true);
    }

    private void refresh_Record(int functionWork)
    {
        refresh_Record(-1,functionWork,true);
    }

    private void refresh_Record(int num,int functionWork,boolean showRecord)
    {
        db = new PhpDB(new WeakReference(context), hd);
        showRecycleView = showRecord;
        if (!bLoc)
        {


        }

        switch(functionWork)
        {
            case FUNCTION_STORENEAR:
                db.getPairSet().setPairLatLng(lat, lng);
                db.getPairSet().setPairFunction(db.pairSet.phpSQLstoreList);
                db.getPairSet().setPairNumLimit(num);
                break;
            case FUNCTION_STORELIST:
                db.getPairSet().setPairFunction(db.pairSet.phpSQLstoreList);
                db.getPairSet().setPairNumLimit(-1);
                break;
            case FUNCTION_STORESEARCH:
                db.getPairSet().setPairFunction(db.pairSet.phpSQLstoreSearchByName);
                db.getPairSet().setPairNumLimit(10);
                db.getPairSet().setPairSearch(1,searchString);
                break;

        }

        new Thread(db).start();
    }

    private void deleteSQLRecord()
    {
        new AlertDialog.Builder(context)
                .setCancelable(false).
                setTitle("刪除"+(String)(itemStoreList.get(position)).get("sname")+" ？")
                .setIcon(android.R.drawable.ic_delete)
                .setMessage("學號："+(String)(itemStoreList.get(position).get("sid"))+
                        "\n年齡："+(String)(itemStoreList.get(position).get("sage")))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //new DeleteSQL().start();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }


    //===========計算兩經緯度之間距離==========================================
    private static final double EARTH_RADIUS = 6378.137;//赤道半徑(單位km)

    private static double rad(double d)
    {  return d * Math.PI / 180.0;  }

    public static double GetDistance(double lat1,double lon1, double lat2,double lon2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = Math.abs(radLat1 - radLat2);
        double b = Math.abs(rad(lon1) - rad(lon2));
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = (double)Math.round(s * 10000)/10;
        return s;
    }
    //===========計算兩經緯度之間距離 End ==========================================
}