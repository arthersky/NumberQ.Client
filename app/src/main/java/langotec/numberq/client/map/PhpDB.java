package langotec.numberq.client.map;

import android.app.Application;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

public class PhpDB extends Application
{
    private boolean bFlag=false;

    private ArrayList<ItemListRow> itemListSet;
    private HashMap<String,Object> newItem = new HashMap<>();
    //自訂物件
    public class ItemListRow{
        private HashMap<String,Object> subItem = new HashMap<String,Object>();
        public void ItemListRow(){}

        public void add(String key , Object obj) { subItem.put(key,obj); }
        public Object get(String key) {return subItem.get(key);}
        public int size(){ try { return subItem.size();
            }catch(Exception e) {return 0; } }
        public HashMap<String,Object> getAll(){ return subItem; }
    }

    //分成 參數設定  //連線   //字串解析   //輸出輸入
    //建構子做必要變數初始化
    public void PhpDB()  { itemListSet = new ArrayList<ItemListRow>(); }

    //======================================參數設置==============================================
    public class  pairSet
    {
        private ArrayList<NameValuePair> pairs;

        //慣例建構子
        public void pairSet()       { ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>(); }

        public void init ()
        {

        }

    public void setPair(String workName, String value)
    {
        pairs.add(new BasicNameValuePair(workName, value));
    }
        /*
        pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLPwdFunction),getResources().getString(R.string.phpSQLPwd)));
        pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLFunction),getResources().getString(R.string.phpSQLstoreList)));
        pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLNumLimit),""+10));
        pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLLat),""+lat)); //lat
        pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLLng),""+lng)); //lng
        pairs.add(new BasicNameValuePair(getResources().getString(R.string.phpSQLShowColumns),"true")); */

}
    //======================================參數設置 END==========================================}

}