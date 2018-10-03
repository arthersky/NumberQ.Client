package langotec.numberq.client.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import langotec.numberq.client.R;
import langotec.numberq.client.MainActivity;


public class Activity_GoogleMap extends AppCompatActivity {
    private MapView mapView;
    private GoogleMap gMap;
    private MyGoogleMapAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button bt1, bt2, bt3;
    Context context;
    double lat, lng;
    double tlat,tlng;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;

    private ArrayList<ItemStoreRow> itemStoreList;
    private HashMap<String,Object> newStoreItem = new HashMap<>();
    private ListView itemListView ;
    private SimpleAdapter adapter;
    private int id,position;
    Marker marker01,marker02,marker03,marker04,marker05;
    FloatingActionButton fab;
    MarkerOptions userLocMark;
    MarkerOptions storeMarkList[];

    //自訂商店物件
    public class ItemStoreRow{
        private HashMap<String,Object> StoreItem = new HashMap<String,Object>();
        public void itemStoreRow(){
            //StoreItem = new HashMap<String,Object>();
        }

        public void add(String key , Object obj)
        {
            StoreItem.put(key,obj);
        }

        public Object get(String key)
        {
            return StoreItem.get(key);
        }

        public int size(){
            try
            {
                return StoreItem.size();
            }catch(Exception e)
            {
                return 0;
            }
        }

        public HashMap<String,Object> getAll(){
          return StoreItem;
        }

    }

    private LinearLayout mapinfo_panel;
    private TextView mapinfo;
    //------------------------

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定在狀態列可以顯示處理中圖示 但是不知道為何沒出現
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        locationManager = MainActivity.lm;//(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();

        setContentView(R.layout.layout_googlemap);
        context = this;

        //取出上次位置
        try{
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lat=location.getLatitude();
            lng=location.getLongitude();
        }catch (SecurityException e){
            Log.e("Location",e.toString());
        }


        //連結Button功能
        bt1 = (Button) findViewById(R.id.gmap_button1);
        bt2 = (Button) findViewById(R.id.gmap_button2);
        bt3 = (Button) findViewById(R.id.gmap_button3);
        fab = (FloatingActionButton) findViewById(R.id.Map_fab_Location_btn);
        fab.setEnabled(false);

        bt1.setOnClickListener(new btnClickListener());

        //返回上一頁功能
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //GoogleMap啟動
        mapView = (MapView) findViewById(R.id.map_googlemap);
        mapView.onCreate(savedInstanceState);
        gMap = mapView.getMap();
        setGoogleMapSnippet(); //Map Mark可多行設定

        processViews();
        processControllers();

        //啟動顯示效果
        //getWindow().setEnterTransition(new Slide().setDuration(2000)); //滑入轉場動畫
        //getWindow().setEnterTransition(new Explode().setDuration(2000)); //轉場動畫
        getWindow().setEnterTransition(new Fade().setDuration(2000)); //淡入淡出轉場動畫

        addFloatingActionButton();
        refresh_Record();
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
            if (null !=storeMarkList) {
                gMap.clear();
                userLocMark = new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng)));
                gMap.addMarker(userLocMark);
            }
            storeMarkList = new MarkerOptions[itemStoreList.size()];
            for (int i = 0; i < itemStoreList.size(); i++) {
                //計算距離
                tlat = Double.parseDouble(((ItemStoreRow) itemStoreList.get(i)).get("lat").toString());
                tlng = Double.parseDouble(((ItemStoreRow) itemStoreList.get(i)).get("lng").toString());

                String range = "" + GetDistance(tlat, tlng, lat, lng);
                storeMarkList[i] = new MarkerOptions().position(new LatLng(tlat, tlng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(((ItemStoreRow) itemStoreList.get(position)).get("HeadName").toString() + " " + ((ItemStoreRow) itemStoreList.get(position)).get("BranchName").toString()).snippet("住址："+ ((ItemStoreRow)itemStoreList.get(position)).get("City")+((ItemStoreRow)itemStoreList.get(position)).get("Area")+((ItemStoreRow)itemStoreList.get(position)).get("Address") +"\n電話："+ ((ItemStoreRow)itemStoreList.get(position)).get("Phone")+"\n距離：" + range + " 公尺");
                gMap.addMarker((storeMarkList[i]));
            }
        }
    }

    //Google Map相關=====  end ==============================================

    private void makeRecyclerView() {
        //--------------------------RecyclerView------------------------------
        ArrayList<String> myDataset = new ArrayList<>();
        if (null==itemStoreList) {
            Log.e("makeRecyclerView","==============重大錯誤!!沒有資料!!=============");
            return;
        }

        //Log.e("RecycleView","Size:"+itemStoreList.size());
        for (int i = 0; i < itemStoreList.size(); i++) {
            //Log.e("test",(((HashMap<String, Object>)(itemStoreList.get(i)).get("HeadName")).toString()+" "+((HashMap<String, Object>)(itemStoreList.get(i)).get("BranchName")).toString()));

            //計算距離
            tlat = Double.parseDouble(((ItemStoreRow) itemStoreList.get(i)).get("lat").toString());
            tlng = Double.parseDouble(((ItemStoreRow) itemStoreList.get(i)).get("lng").toString());

            String range =""+GetDistance(tlat,tlng,lat,lng);
            //Log.e("makeRecyclerView","range:"+tlng+","+tlat+" / " +lng +","+lat +"=" + range);
            myDataset.add("店名：" + ((ItemStoreRow)itemStoreList.get(i)).get("HeadName") + " "+ ((ItemStoreRow)itemStoreList.get(i)).get("BranchName") +"\n住址："+ ((ItemStoreRow)itemStoreList.get(i)).get("City")+((ItemStoreRow)itemStoreList.get(i)).get("Area")+((ItemStoreRow)itemStoreList.get(i)).get("Address") +"\n電話："+ ((ItemStoreRow)itemStoreList.get(i)).get("Phone")+"\n距離："+range + " 公尺"  );
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view_GoogleMap);
        //mRecyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.VERTICAL));
        mAdapter = new MyGoogleMapAdapter(myDataset);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    //button的點選事件
    class btnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //附近店家
                case R.id.gmap_button1:
                    if (null != mRecyclerView && mRecyclerView.getVisibility() == View.VISIBLE) {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    }else{
                        makeRecyclerView();
                        makeStoreListOnMap();}
                    break;
                //所有店家
                case R.id.gmap_button2:
                    break;

                //搜尋店家
                case R.id.gmap_button3:
                    break;
            }
        }
    }

    private void addFloatingActionButton()
    {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userLocMark = new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng)));
                gMap.addMarker(userLocMark);
                setProgressBarIndeterminateVisibility(true); // turn progress on
                setProgressBarVisibility(true);
                fab.setEnabled(false);
                try{
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, myLocationListener, null);
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, myLocationListener, null);
                }catch (SecurityException e){
                    Log.e("TAG",e.toString());
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // 顯示處理中圖示
        setProgressBarIndeterminateVisibility(true);
        setProgressBarVisibility(true);
        // 執行註冊，設備是GPS，只會讀取一次位置資訊
        try{
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, myLocationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, myLocationListener, null);
        }catch (SecurityException e){
            Log.e("TAG",e.toString());
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
                lng =  location.getLongitude();

                userLocMark = new MarkerOptions().position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("現在位置").snippet(getAddress(new LatLng(lat,lng)));
                gMap.addMarker(userLocMark);
                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(16).bearing(0).tilt(25).build()));
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
                    ((ItemStoreRow)itemStoreList.get(position)).get("HeadName");
                    //Toast.makeText(context, ((ItemStoreRow)itemStoreList.get(position)).get("HeadName") + " " +((ItemStoreRow)itemStoreList.get(position)).get("lat") +","+ ((ItemStoreRow)itemStoreList.get(position)).get("lng") + " Item " + position + " is clicked.", Toast.LENGTH_SHORT).show();


                    tlat = Double.parseDouble(((ItemStoreRow)itemStoreList.get(position)).get("lat").toString());
                    tlng = Double.parseDouble(((ItemStoreRow)itemStoreList.get(position)).get("lng").toString());

                    String range =""+GetDistance(tlat,tlng,lat,lng);
                    gMap.addMarker(new MarkerOptions().position(new LatLng (tlat,tlng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title( ((ItemStoreRow)itemStoreList.get(position)).get("HeadName").toString() +" "+ ((ItemStoreRow)itemStoreList.get(position)).get("BranchName").toString()  ).snippet("距離："+range + " 公尺"));
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

    private void processViews() {

        // 顯示拖拉Marker訊息
        mapinfo_panel = (LinearLayout) findViewById(R.id.mapinfo_panel);
        mapinfo = (TextView) findViewById(R.id.info);

        // 先建立一個設定Marker用的MarkerOptions物件
        MarkerOptions markerOptions = new MarkerOptions();

        // 建立設定Marker圖示用的物件
        //BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.station);
        // 設定Marker的地點、標題、說明和圖示
        /*markerOptions.position(station01)
                .title("臺北車站")
                .snippet(station01.latitude+","+station01.longitude)
                .icon(bitmapDescriptor);
        // 加入Marker到地圖並取得傳回的Marker物件
        marker01 = gMap.addMarker(markerOptions);

        // 加入Marker到地圖並同時設定地點、標題、說明和圖示
        marker02 = gMap.addMarker(new MarkerOptions()
                .position(station02)
                .title("萬華車站")
                .snippet(station02.latitude+","+station02.longitude)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.station)));

        // 加入Marker到地圖並同時設定地點、標題和說明
        marker03 = gMap.addMarker(new MarkerOptions()
                .position(station03)
                .title("西門站")
                .snippet(station03.latitude+","+station03.longitude));

        // 加入Marker到地圖並同時設定地點、標題、說明和藍色預設圖示
        marker04 = gMap.addMarker(new MarkerOptions()
                .position(station04)
                .title("中山站")
                .snippet(station04.latitude+","+station04.longitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // 加入Marker到地圖並同時設定地點、標題、說明、黃色預設圖示和可以拖拉
        marker05 = gMap.addMarker(new MarkerOptions()
                .position(station05)
                .title("善導寺站")
                .snippet(station05.latitude+","+station05.longitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .draggable(true));*/
    }

    private void processControllers() {
        // 建立點擊Marker事件
        GoogleMap.OnMarkerClickListener myMarkerClientListener;
        myMarkerClientListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.equals(marker01)) {
                    Toast.makeText(context, marker.getTitle(), Toast.LENGTH_SHORT).show();
                    // 回傳true處理點擊事件
                    return true;
                }
                else {
                    // 回傳false不處理點擊事件，執行預設的點擊工作，顯示訊息視窗
                    return false;
                }
            }
        };


        // 註冊點擊Marker事件
        gMap.setOnMarkerClickListener(myMarkerClientListener);

        // 建立Marker拖拉事件
        GoogleMap.OnMarkerDragListener myMarkDragListerer;
        myMarkDragListerer = new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // Marker正在移動，參數是操作中的Marker物件，
                // 可以經由它取得最新的資訊，例如位置
                if(marker.equals(marker05)){
                    //顯示目前位置
                    LatLng position = marker.getPosition();
                    mapinfo.setText(marker.getTitle()+"："+position.latitude+","+position.longitude);
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // Marker結束拖拉，參數是操作的Marker物件
                if(marker.equals(marker05)){
                    //關閉顯示提示元件
                    mapinfo_panel.setVisibility(View.INVISIBLE);
                    marker05.setTitle("New Place");
                    //取得mark目前的經緯度座標
                    LatLng point = marker.getPosition();
                    //取得目前位置
                    marker05.setSnippet(getAddress(point));
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Marker開始拖拉，參數是操作的Marker物件
                if(marker.equals(marker05)){
                    // 開啟顯示訊息元件
                    mapinfo_panel.setVisibility(View.VISIBLE);
                    mapinfo.setText("Drag Start..");
                }
            }
        };
        // 註冊拖拉Marker事件
        gMap.setOnMarkerDragListener(myMarkDragListerer);
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
    { new InsertSQL().start();}

    private void UpdateSQLRecord()
    {new UpdateSQL().start();	}

    private void refresh_Record()
    {	new SelectSQL().start();}

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
                        new DeleteSQL().start();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private class SelectSQL extends Thread
    {
        @Override
        public void run()
        {
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLPwdFunction),getResources().getString(R.string.phpSQLPwd)));
            pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLFunction),getResources().getString(R.string.phpSQLstoreList)));
            pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLNumLimit),""+10));
            pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLLat),""+lat)); //lat
            pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLLng),""+lng)); //lng
            pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLShowColumns),"true")); //顯示欄位

            AndroidHttpClient androidHttpClient =null;

            try {
                //https://flashmage.000webhostapp.com/query.php?p=pass&w=storeList&n=10&lat=25&lng=121

                String user_agent = System.getProperty("http_agent");
                if (androidHttpClient == null) androidHttpClient = AndroidHttpClient.newInstance(user_agent);
                HttpPost httppost = new HttpPost(getResources().getString(R.string.server) + getResources().getString(R.string.select));
                httppost.setEntity(new UrlEncodedFormEntity(pairs));
                Log.e("SelectSQL",pairs.toString());

                HttpResponse response = androidHttpClient.execute(httppost);

                final String status = response.getStatusLine().toString();
                Log.e("=======",status.toString());
                itemStoreList = new ArrayList<ItemStoreRow>();
                if (status.split(" ")[1]	.equals("200")) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String readLine;

                    //第一次是欄位名稱
                    readLine = br.readLine();
                    Log.e("Result", readLine);
                    String[] item_Field_String =readLine.split(",");

                    while (((readLine = br.readLine()) != null))
                    {
                        Log.e("Result", readLine);
                        HashMap<String,Object> item = new HashMap<String,Object>();
                        String[] item_String =readLine.split(",");
                        ItemStoreRow itemStoreRow = new ItemStoreRow();
                        Log.e("ItemStoreRow","Add");

                        for (int i =0;i<item_String.length;i++)
                        {
                            itemStoreRow.add(item_Field_String[i], item_String[i]);
                        }
                        Log.e("itemStoreList","Add itemStoreRow");
                        itemStoreList.add(itemStoreRow);
                    }
                    Log.e("SelectSQL:","ItemStoreList.Size:"+itemStoreList.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*adapter = new SimpleAdapter(
                                    context,itemStoreList,
                                    R.layout.row,
                                    new String[]{"_id","sid","sname","sage"},
                                    new int[]{R.id._id,R.id.sid,R.id.sname,R.id.sage} );
                            itemListView.setAdapter(adapter);*/

                            //Toast.makeText(context, "查詢成功" + status, Toast.LENGTH_LONG).show();
                        }
                    });
                }else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, "查詢失敗" + status, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }catch( final Exception e)
            {
                Log.e("error","Catch HTTP error:" + e.toString());
                e.printStackTrace();
            }finally{
                if (androidHttpClient != null) {
                    androidHttpClient.close();
                    androidHttpClient = null;
                }
                Log.e("error","Finally HTTP error");
            }
        }
    } //Select Thread end

    private class InsertSQL extends Thread {

        @Override
        public void run() {
            Log.e("Insert","Run_Start");
            if (null != newStoreItem) {
                ArrayList<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("p", getResources().getString(R.string.phpSQLPwd)));
                pairs.add(new BasicNameValuePair("sid", "" + newStoreItem.get("sid")));
                pairs.add(new BasicNameValuePair("sname", "" + newStoreItem.get("sname")));
                pairs.add(new BasicNameValuePair("sage", "" + newStoreItem.get("sage")));
                Log.e("Insert","變數設定完畢");
                AndroidHttpClient androidHttpClient = null;
                try {
                    String user_agent = System.getProperty("http_agent");
                    if (androidHttpClient == null)
                        androidHttpClient = AndroidHttpClient.newInstance(user_agent);
                    HttpPost httpost = new HttpPost(getResources().getString(R.string.server) + getResources().getString(R.string.insert));
                    httpost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
                    Log.e("InsertSQL1",pairs.toString());
                    Log.e("InsertSQL2",httpost.getParams().toString());

                    HttpResponse response = androidHttpClient.execute(httpost);
                    final String status = response.getStatusLine().toString();

                    if (status.split(" ")[1].equals("200")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "新增成功" + status, Toast.LENGTH_LONG).show();
                                refresh_Record();
                                newStoreItem = null;
                            }
                        });
                    } else {
                        Log.e("InsertSQL","Null");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "新增失敗" + status, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    if (androidHttpClient != null) {
                        androidHttpClient.close();
                        androidHttpClient = null;
                    }
                }
            }
        }
    }

    private class UpdateSQL extends Thread {

        @Override
        public void run() {
            if (null != newStoreItem) {
                ArrayList<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("p", getResources().getString(R.string.phpSQLPwd)));
                pairs.add(new BasicNameValuePair("id", "" + newStoreItem.get("_id")));
                pairs.add(new BasicNameValuePair("sid", "" + newStoreItem.get("sid")));
                pairs.add(new BasicNameValuePair("sname", "" + newStoreItem.get("sname")));
                pairs.add(new BasicNameValuePair("sage", "" + newStoreItem.get("sage")));
                AndroidHttpClient androidHttpClient = null;
                try {
                    String user_agent = System.getProperty("http_agent");
                    if (androidHttpClient == null)
                        androidHttpClient = AndroidHttpClient.newInstance(user_agent);
                    HttpPost httpost = new HttpPost(getResources().getString(R.string.server) + getResources().getString(R.string.update));
                    httpost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));

                    HttpResponse response = androidHttpClient.execute(httpost);
                    final String status = response.getStatusLine().toString();

                    if (status.split(" ")[1].equals("200")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "更新成功" + status, Toast.LENGTH_LONG).show();
                                refresh_Record();
                                newStoreItem = null;
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "更新失敗" + status, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    if (androidHttpClient != null) {
                        androidHttpClient.close();
                        androidHttpClient = null;
                    }
                }
            }
        }
    }

    private class DeleteSQL extends Thread {
        @Override
        public void run() {
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("p", getResources().getString(R.string.phpSQLPwd)));
            pairs.add(new BasicNameValuePair("id", "" + id));
            AndroidHttpClient androidHttpClient = null;


            try {
                String user_agent = System.getProperty("http_agent");
                if (androidHttpClient == null)
                    androidHttpClient = AndroidHttpClient.newInstance(user_agent);
                HttpPost httppost = new HttpPost(getResources().getString(R.string.server) + getResources().getString(R.string.delete));
                httppost.setEntity(new UrlEncodedFormEntity(pairs));
                HttpResponse response = androidHttpClient.execute(httppost);
                final String status = response.getStatusLine().toString();
                if (status.split(" ")[1].equals("200")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "刪除成功" + status, Toast.LENGTH_SHORT).show();
                            refresh_Record();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "刪除失敗" + status, Toast.LENGTH_SHORT).show();
                            refresh_Record();
                        }
                    });
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (androidHttpClient != null) {
                    androidHttpClient.close();
                    androidHttpClient = null;
                }
            }

        }
    }

    //===========計算兩經緯度之間距離==========================================
    private static final double EARTH_RADIUS = 6378.137;//赤道半徑(單位km)

    private static double rad(double d) {  return d * Math.PI / 180.0;  }

    public static double GetDistance(double lat1,double lon1, double lat2,double lon2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = Math.abs(radLat1 - radLat2);
        double b = Math.abs(rad(lon1) - rad(lon2));
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+ Math.cos(radLat1)* Math.cos(radLat2)* Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = (double) Math.round(s * 10000)/10;
        return s;
    }
    //===========計算兩經緯度之間距離 End ==========================================
}