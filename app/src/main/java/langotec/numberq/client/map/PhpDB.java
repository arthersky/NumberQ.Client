package langotec.numberq.client.map;

import android.app.Application;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import langotec.numberq.client.R;

//2018-09-21 (Fri)
//分成 一.PhpDB主程序功能與傳入參數選擇  子物件 1.參數物件  子物件 2.連線+字串解析+輸出輸入物件
public class PhpDB implements Runnable
{
    static public final int PHP_SELECT = 0;
    static public final int PHP_INSERT = 1;
    static public final int PHP_UPDATE = 2;
    static public final int PHP_DELETE = 3;
    private WeakReference<Context> weakReference;
    private int workSelect=-1;
    private boolean blReady=false;
    private Handler dataHandler = null;
    private HttpDataFromPHP httpDataFromPHP;
    public PairSet pairSet = null;

    private ArrayList<ItemListRow> itemListSet;
    private HashMap<String,Object> newItem = new HashMap<>();

    private JSONArray jsonArray;

    //自訂物件 放置DB一整行
    public class ItemListRow{
        private HashMap<String,Object> subItem = new HashMap<String,Object>();
        public void ItemListRow(){}

        public void add(String key ,Object obj) { subItem.put(key,obj); }
        public Object get(String key) {return subItem.get(key);}
        public Object getkeySet() {return subItem.keySet();}

        public int size(){ try { return subItem.size();
        }catch(Exception e) {return 0; } }
        public HashMap<String,Object> getAll(){ return subItem; }
    }

    //取得設定的Function Name
    public String getPairFunction() {if (pairSet != null) {return pairSet.getPairFunction();} else return ""; }

    //傳回資料是否Ready true =資料已經準備完畢
    public boolean getState(){return blReady;}
    //傳回處理完的SQL資料
    public ArrayList<ItemListRow> getDataSet(){if (blReady) return itemListSet; else return null; }
    //回傳JSON
    public JSONArray getJSONData(){if(pairSet.isJSON()) return jsonArray;else return null; }
    //傳回資料筆數
    public int getRowSize()  { if (blReady) return itemListSet.size();else return -1; }
    public int getColSize()  { if (blReady) return ((ItemListRow)itemListSet.get(0)).size();else return -1; }
    //環境變數
    public PairSet getPairSet(){ if (pairSet == null) pairSet = new PairSet(); return pairSet; }
    public void setPairSet(PairSet tpairSet){ pairSet = tpairSet;}
    //返回是否JSON
    public boolean isJSON(){return pairSet.isJSON();}

    //Thread 依照初始設定執行模組
    public void run()
    {
        httpDataFromPHP = new HttpDataFromPHP(dataHandler);
        switch(workSelect)
        {
            case PHP_SELECT:
                Log.e("phpDB","資料查詢模組開始執行");
                httpDataFromPHP.start();
                break;
            case PHP_INSERT:
                break;
            case PHP_UPDATE:
                break;
            case PHP_DELETE:
                break;
        }
    }

    //PhpDB主要程序===============================
    //建構子做必要變數初始化 !一定要傳入PhpDB(context) 否則抓不到變數
    public PhpDB(){this(null);}
    public PhpDB(WeakReference<Context> weakReference){
        this(weakReference, PHP_SELECT,null); //預設使用查詢
    }
    public PhpDB(WeakReference<Context> weakReference, int work)  {
        this(weakReference, PHP_SELECT,null); //預設使用查詢
    }
    public PhpDB(WeakReference<Context> weakReference, Handler hand)  {
        this(weakReference, PHP_SELECT, hand); //預設使用查詢
    }
    public PhpDB(WeakReference<Context> weakReference, int work, Handler hand)  {
        blReady = false;
        this.weakReference = weakReference;
        Log.e("phpDB","初始化成功!");
        workSelect = work;
        dataHandler = hand;
    }


    //======================================參數設置==============================================
    public class PairSet
    {
        Context context = weakReference.get();
        //Http 相關
        final String SERVER = context.getResources().getString(R.string.server);
        final String SELECT = context.getResources().getString(R.string.select);
        //引入php上的參數免得一大串傷眼睛 1.基本參數
        final String phpSQLPwdFunction = context.getResources().getString(R.string.phpSQLPwdFunction);
        final String phpSQLFunction = context.getResources().getString(R.string.phpSQLFunction) ;
        final String phpSQLNumLimit  = context.getResources().getString(R.string.phpSQLNumLimit);
        final String phpSQLLat = context.getResources().getString(R.string.phpSQLLat) ;
        final String phpSQLLng  = context.getResources().getString(R.string.phpSQLLng);
        final String phpSQLShowColumns  = context.getResources().getString(R.string.phpSQLShowColumns);
        final String phpSQLtoJSON  = context.getResources().getString(R.string.phpSQLtoJSON);
        //引入php上的參數免得一大串傷眼睛 2.選擇性參數
        final String phpSQLPwd = context.getResources().getString(R.string.phpSQLPwd); //通關碼
        final String phpSQLstoreList  = context.getResources().getString(R.string.phpSQLstoreList); //商店清單
        final String phpSQLstoreSearchByName = context.getResources().getString(R.string.phpSQLstoreSearchByName); //商店搜尋
        final String phpSQLstoreProductList = context.getResources().getString(R.string.phpSQLstoreProductList); //產品搜尋
        final String phpSQLuserList = context.getResources().getString(R.string.phpSQLuserList); //人員清單
        final String phpSQLuserSearchByEMAIL = context.getResources().getString(R.string.phpSQLuserSearchByEMAIL); //Email人員搜尋
        final String phpSQLorderList = context.getResources().getString(R.string.phpSQLorderList); //訂單搜尋
        final String phpSQLorderMSList = context.getResources().getString(R.string.phpSQLorderMSList); //完整訂單搜尋
        public final String phpSQLgetOrderNewId = context.getResources().getString(R.string.phpSQLgetOrderNewId); //訂單取號
        public final String phpSQLsetOrderUpdate = context.getResources().getString(R.string.phpSQLsetOrderUpdate); //訂單更新
        final String phpSQLdelOrder = context.getResources().getString(R.string.phpSQLdelOrder); //訂單刪除
        public final String phpSQLnewOrderSub  = context.getResources().getString(R.string.phpSQLnewOrderSub); //訂單菜單新增
        final String phpSQLsetOrderSub  = context.getResources().getString(R.string.phpSQLsetOrderSub); //訂單菜單修改
        final String phpSQLdelOrderSub  = context.getResources().getString(R.string.phpSQLdelOrderSub); //訂單菜單刪除

        //設定用參數
        final String phpSQLNumDefault  = context.getResources().getString(R.string.phpSQLNumDefault); //預設資料筆數
        final String phpSQLLatDefault  = context.getResources().getString(R.string.phpSQLLatDefault); //預設Lat
        final String phpSQLLngDefault  = context.getResources().getString(R.string.phpSQLLngDefault); //預設Lng
        final String phpSQLSearch1  = context.getResources().getString(R.string.phpSQLSearch1); //s1
        final String phpSQLSearch2  = context.getResources().getString(R.string.phpSQLSearch2); //s2
        final String phpSQLSearch3  = context.getResources().getString(R.string.phpSQLSearch3); //s3
        final String phpSQLSearch4  = context.getResources().getString(R.string.phpSQLSearch4); //s4
        final String phpSQLSearch5  = context.getResources().getString(R.string.phpSQLSearch5); //s5
        final String phpSQLSearch6  = context.getResources().getString(R.string.phpSQLSearch6); //s6
        final String phpSQLSearch7  = context.getResources().getString(R.string.phpSQLSearch7); //s7
        final String phpSQLSearch8  = context.getResources().getString(R.string.phpSQLSearch8); //s8
        final String phpSQLSearch9  = context.getResources().getString(R.string.phpSQLSearch9); //s9
        final String phpSQLSearch10  = context.getResources().getString(R.string.phpSQLSearch10); //s10
        final String phpSQLSearch11  = context.getResources().getString(R.string.phpSQLSearch11); //s11
        final String phpSQLSearch12  = context.getResources().getString(R.string.phpSQLSearch12); //s12
        final String phpSQLSearch13  = context.getResources().getString(R.string.phpSQLSearch13); //s13
        final String phpSQLSearch14  = context.getResources().getString(R.string.phpSQLSearch14); //s14
        final String phpSQLSearch15  = context.getResources().getString(R.string.phpSQLSearch15); //s15

        //內部紀錄參數
        private ArrayList<NameValuePair> pairs;
        //紀錄最後呼叫 Function
        private String functionName="";
        //JSON格式
        private boolean blJSON  = false;

        public boolean isJSON() {
            return this.blJSON;}

        //慣例建構子
        public PairSet(){this("",true,-1,-1,-1);}
        public PairSet(double lat,double lng){this("",true,-1,lat,lng);}
        public PairSet(String func,boolean ShowCol,int NumLimit,double lat,double lng){
            pairs = new ArrayList<NameValuePair>();
            setPairPass();
            setPairNumLimit(NumLimit);
            setPairFunction(func);
            setPairShowColName(ShowCol);
            setPairLatLng(lat,lng);
        }

        public void setPairSearch(int index,String str) //列出筆數
        {
            switch(index) {
                case 1:
                    setPair(phpSQLSearch1, str);
                    break;
                case 2:
                    setPair(phpSQLSearch2, str);
                    break;
                case 3:
                    setPair(phpSQLSearch3, str);
                    break;
                case 4:
                    setPair(phpSQLSearch4, str);
                    break;
                case 5:
                    setPair(phpSQLSearch5, str);
                    break;
                case 6:
                    setPair(phpSQLSearch6, str);
                    break;
                case 7:
                    setPair(phpSQLSearch7, str);
                    break;
                case 8:
                    setPair(phpSQLSearch8, str);
                    break;
                case 9:
                    setPair(phpSQLSearch9, str);
                    break;
                case 10:
                    setPair(phpSQLSearch10, str);
                    break;
                case 11:
                    setPair(phpSQLSearch11, str);
                    break;
                case 12:
                    setPair(phpSQLSearch12, str);
                    break;
                case 13:
                    setPair(phpSQLSearch13, str);
                    break;
                case 14:
                    setPair(phpSQLSearch14, str);
                    break;
                case 15:
                    setPair(phpSQLSearch15, str);
                    break;
                default:
            }
        }

        public void setPairNumLimit(int num) //列出筆數 0抓預設 小於0 移除
        {  if (num >0 ) setPair(phpSQLNumLimit,""+num);
            else if (num == 0) setPair(phpSQLNumLimit,phpSQLNumDefault);
            else removePair(phpSQLNumLimit); }

        public void setPairFunction(String WorkFunction)  //呼叫功能
        {
            functionName = WorkFunction;
            if (!WorkFunction.equals("")) setPair(phpSQLFunction,WorkFunction);
        }

        public void setPairShowColName(boolean ShowColName)  //顯示欄位
        { if(ShowColName) setPair(phpSQLShowColumns,"true");
          else setPair(phpSQLShowColumns,"false"); }

        public void setPairLatLng(double lat,double lng){
            if(lat ==0) setPair(phpSQLLat,phpSQLLatDefault); //Lat
            else if(lat > 0)  setPair(phpSQLLat,""+lat); //Lat
            else removePair(phpSQLLat);

            if(lng == 0) setPair(phpSQLLng,phpSQLLngDefault); //Lng
            else if (lng > 0) setPair(phpSQLLng,""+lng); //Lng
            else removePair(phpSQLLng);
        }

        public void setPairPass() ////通關
        { setPair(phpSQLPwdFunction,phpSQLPwd); }

        public void setPairJSON() //設定輸出JSON
        {   this.blJSON = true;
            setPair(phpSQLtoJSON,"1");}

        public void setPairDefault() //預設值
        {
            setPairPass();
            //setPair(phpSQLFunction,phpSQLstoreList); //列出商店
            //setPair(phpSQLNumLimit,phpSQLNumDefault); //列出10筆數
            //setPair(phpSQLLat,phpSQLLatDefault); //Lat
            //setPair(phpSQLLng,phpSQLLngDefault); //Lng
            //setPair(phpSQLShowColumns,"true"); //顯示欄位
        }

        public ArrayList<NameValuePair> getAll()
        { return pairs; }

        public void setPair(String workName,String value) //若新增前發現存在相同變數先移除之
        {
            Log.e("設定參數",workName + "=" +value);
            try {
                if (pairs.size()>0) {for(int i=0;i< pairs.size();i++)  {if (pairs.get(i).getName().equals(workName)) pairs.remove(i); }}
                pairs.add(new BasicNameValuePair(workName, value));
            }
            catch(Exception ex){Log.e("參數設定發生錯誤:",ex.toString());ex.printStackTrace();}
        }

        public void removePair(String pName)
        {
            Log.e("移除參數",pName);
            try { if (pairs.size()>0) { for(int i=0;i< pairs.size();i++)  {if (pairs.get(i).getName().equals(pName)) pairs.remove(i); }}}
            catch(Exception ex){Log.e("參數移除發生錯誤:",ex.toString());ex.printStackTrace();}
        }

        public String getServerURL()
        { return SERVER;}
        public String getSelectURL()
        { return SELECT;}
        public String getPairFunction()
        { return functionName; }


    }
    //======================================參數設置 END==========================================
    //======================================Http Data==============================================
    public class HttpDataFromPHP extends Thread
    {
        //private ArrayList<ItemListRow> itemListSet;
        //private HashMap<String,Object> newItem;
        //public ArrayList<ItemListRow> getItemListSet() { return itemListSet; }
        private Handler dataHandler = null;


        AndroidHttpClient androidHttpClient =null;

        //建構子
        public HttpDataFromPHP() { this(null,null); }
        public HttpDataFromPHP(Handler hand) {this(hand,null);}
        public HttpDataFromPHP(PairSet tpairSet) {this(null,tpairSet);}
        public HttpDataFromPHP(Handler hand,PairSet tpairSet) {
            if (pairSet == null && tpairSet==null) pairSet = new PairSet(); //參數集合
            else if (tpairSet!=null) pairSet=tpairSet;
            dataHandler = hand;
            newItem = new HashMap<String,Object>();
            Log.e("HttpDataFromPHP","初始化成功");
        }

        //Thread
        public void run()
        {
            Log.e("HttpDataFromPHP","執行序執行中");
            //JSON的輸出不同
            if (pairSet.isJSON()) {
                jsonArray = getJSON();
            }else {
                itemListSet = getData(); //有點多寫的
            }
            //有設定 Handler 則傳出資料

            if (null != dataHandler ) {
                Message msg = new Message();
                msg.obj = "ready";
                dataHandler.sendMessage(msg);
            }
        }
        //連線物件設定與回傳
        private HttpResponse getPHPConnection(String pairWork)
        {
            return getPHPConnection(pairSet.getServerURL(),pairSet.getSelectURL(),pairWork);
        }
        //連線物件設定與回傳 多形
        private HttpResponse getPHPConnection(String serverURL,String fileURL,String pairWork)
        {
            HttpPost httppost = null;
            try
            {
                if (androidHttpClient == null) androidHttpClient = AndroidHttpClient.newInstance(System.getProperty("http_agent"));
                httppost = new HttpPost(serverURL+fileURL);

                //參數選擇 空的就使用預設值(查詢)
                if (pairWork.equals("")) {pairSet.setPairDefault(); }
                else{pairSet.setPairFunction(pairWork);}

                Log.e("getPHPConnection",serverURL+fileURL);
                Log.e("getPHPConnection",pairSet.getAll().toString());

                httppost.setEntity(new UrlEncodedFormEntity(pairSet.getAll(),HTTP.UTF_8));

                return androidHttpClient.execute(httppost);

            }catch(Exception ex) {
                Log.e("getPHPConnection","getPHPConnection 資料回傳錯誤");
                return null;
            }
        }

        //回傳從PHP來的JSON
        public JSONArray getJSON()
        {
            try{
                jsonArray = getJSON(getPHPConnection(""));
                if (jsonArray.length() >0) blReady = true;
                Log.e("getJSON","取得長度:"+jsonArray.length());
            }catch(Exception ex){
                Log.e("getJSON()",ex.toString());
                jsonArray= null;
            }
            return jsonArray;
        }

        public JSONArray getJSON(HttpResponse response)
        {
            try {
                StringBuilder sb = new StringBuilder();
                final String status = response.getStatusLine().toString();

                Log.e("getDate","讀取狀態: "+status);
                if (status.split(" ")[1].equals("200"))
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String readLine;

                    while (((readLine = br.readLine()) != null)) {
                        sb.append(readLine);
                        //Log.e("getJSON",readLine);
                    }
                    jsonArray = new JSONArray(sb.toString());
                }
            }
            catch(Exception ex) {
                Log.e("getJSON","資料讀取失敗:" + ex.toString());
                androidHttpClient.close();
                androidHttpClient = null;
            }
            androidHttpClient.close();
            androidHttpClient = null;
            return jsonArray;
        }

        //這一段才是執行的核心
        public ArrayList<ItemListRow> getData()
        {
            try {
                //取連線物件 並從連線成功的物件中處理收到的資料
                itemListSet = getDate( getPHPConnection(""));
                if (itemListSet.size() >0) blReady = true;
                Log.e("getData 取得筆數",""+itemListSet.size());
            }catch(Exception ex)
            {
                ex.printStackTrace();
            }
            return itemListSet;
        }

        //多形 處理收到的資料讀入 ItemListRow 的集合中 預設第一欄讀取欄位名稱
        private ArrayList<ItemListRow> getDate(HttpResponse response)
        { return getDate(response,true); }

        //多形 處理收到的資料讀入 ItemListRow 的集合中 visTitle 第一欄讀取欄位名稱
        private ArrayList<ItemListRow> getDate(HttpResponse response,Boolean visTitle )
        {
            try {
                String[] item_Field_String = null;
                final String status = response.getStatusLine().toString();
                itemListSet = new ArrayList<ItemListRow>();

                Log.e("getDate","讀取狀態: "+status);
                if (status.split(" ")[1].equals("200"))
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String readLine;

                    //顯示欄位名稱
                    if (visTitle == true)
                    {
                        //第一次是取欄位名稱
                        readLine = br.readLine();
                        if (readLine == null) Log.e("讀取狀態","讀取不到資料!!");
                        item_Field_String = readLine.split(","); //欄位名稱
                    }

                    while (((readLine = br.readLine()) != null)) {
                        String[] item_String = readLine.split(",");
                        ItemListRow itemListRow = new ItemListRow();

                        for (int i = 0; i < item_String.length; i++) {
                            if (visTitle == true){itemListRow.add(item_Field_String[i], item_String[i]);}
                            else {itemListRow.add("" + i, item_String[i]);}
                        }
                        itemListSet.add(itemListRow);
                    }
                }
            }
            catch(Exception ex) {
                Log.e("getDate","資料讀取失敗:" + ex.toString());
                androidHttpClient.close();
                androidHttpClient = null;
            }
            androidHttpClient.close();
            androidHttpClient = null;
            return itemListSet;
        }
    }
}