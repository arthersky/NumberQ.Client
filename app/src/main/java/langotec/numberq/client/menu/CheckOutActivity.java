package langotec.numberq.client.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.login.Member;
import langotec.numberq.client.map.PhpDB;

public class CheckOutActivity extends AppCompatActivity {

    public static ArrayList<Order> orderList;
    private Cart cart;
    private Context context;
    private static PhpDB db;
    public static int orderIndex = 0, menuIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        context  = this;
        cart = Cart.getInstance(context);
        orderList = splitOrders();
        setLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cart.saveCartFile(context);
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        menu.findItem(R.id.search_button).setVisible(false);
        menu.findItem(R.id.menu_cart_clear).setVisible(false);
        menu.findItem(R.id.menu_cart_createOrder).setVisible(false);
        menu.findItem(R.id.menu_backHome).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_backHome:
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("currentPage", 2);
                startActivity(intent);
                System.gc();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLayout(){
        setTitle(getString(R.string.checkOut_title));
        ListView listView = findViewById(R.id.order_list);
        MenuBaseAdapter adapter = new MenuBaseAdapter(context, orderList);
        listView.setAdapter(adapter);

    }



//  region拆單
    private ArrayList<Order> splitOrders(){
        HashMap<String, Order> orderMap = new HashMap<>();
        //先加入第0筆cart資料進入HashMap(用Map純粹為了方便比較key)
        orderMap.put(cart.get(0).getHeadName() + cart.get(0).getBranchName(), new Order());
        orderMap.get(cart.get(0).getHeadName() + cart.get(0).getBranchName()).add(cart.get(0));

        //再比較key值是否與Cart內的店名相同
        for (int i = 0; i < cart.size(); i++) {
            int index = 0;
            for (String key : orderMap.keySet()) {
                index ++;
                if (key.equals(cart.get(i).getHeadName() + cart.get(i).getBranchName()) && i !=0) {
                    orderMap.get(key).add(cart.get(i));
                    break;

                //如果已經跑到最後一筆key而且key不相符時，即新增一筆Order進入Map
                } else if (index == orderMap.size() &&
                        !key.equals(cart.get(i).getHeadName() + cart.get(i).getBranchName())){
                    orderMap.put(cart.get(i).getHeadName() + cart.get(i).getBranchName(), new Order());
                    orderMap.get(cart.get(i).getHeadName() + cart.get(i).getBranchName()).add(cart.get(i));
                    break;
                }
            }
        }
        return new ArrayList<>(orderMap.values());
    }

//  endregion

//  region 處理訂單至資料庫
    public void onCheckOutClick(View view){
        createOrders();
    }

    private void createOrders(){
        db = new PhpDB(context, new OrderHandler(context));
        getOrderID();
//        setOrderDetail();
        Log.e("等待資料回應:", new Date().toString());
        new Thread(db).start();
    }

    public static void getOrderID(){
        db.getPairSet().setPairFunction(db.pairSet.phpSQLgetOrderNewId); //取新訂單
        db.getPairSet().setPairSearch(1, Member.getInstance().getCustomerUserId());
        Log.e("批次新增訂單開始時間:", new Date().toString());
        new Thread(db).start();
    }

//    public static void setOrderDetail(){
//        //訂單菜單新增
//        db.getPairSet().setPairFunction(db.pairSet.phpSQLnewOrderSub);
//        //OrderID
//        db.getPairSet().setPairSearch(1, orderList.get(orderIndex).getOrderId());
//        //productId 產品代號 同一個訂單不允許產品代碼重複
//        db.getPairSet().setPairSearch(2, orderList.get(orderIndex).get(menuIndex).getProductId());
//        //quantity 數量
//        db.getPairSet().setPairSearch(3, String.valueOf(
//                orderList.get(orderIndex).get(menuIndex).getQuantityNum()));
//        //price 價格
//        db.getPairSet().setPairSearch(4, orderList.get(orderIndex).get(menuIndex).getPrice());
//        Log.e("批次新增菜單開始時間" + orderIndex, new Date().toString());
//        new Thread(db).start();
//    }

    private static class OrderHandler extends Handler{
        Context context;
        OrderHandler(Context context){
            this.context = context;
        }
        @Override
        public void handleMessage(Message msg) {
            Log.e("Handler 發送過來的訊息", msg.obj.toString());
            if(db.getState()) {
                Calendar calendar = Calendar.getInstance();
                Log.e("資料回應時間", new Date().toString());
                Log.e("回應副程式", db.getPairFunction());
                String tmp = "";
                for (int y = 0; y < db.getRowSize(); y++) {
                    for (Object key : ((PhpDB.ItemListRow) db.getDataSet().get(y)).getAll().keySet()) {
                        tmp = tmp + key.toString() + "=" + ((PhpDB.ItemListRow) db.getDataSet().
                                get(y)).get(key.toString()).toString() + "  ";
                    }
                    Log.e("=========Debug=======",tmp);
                    orderList.get(orderIndex).setOrderId(tmp);
                    orderList.get(orderIndex).setOrderDT(calendar);
                    if (orderIndex == orderList.size() - 1) {
                        showOrdersDetail();
                        orderIndex = 0;
                        menuIndex = 0;
                        return;
                    }
                }
            }
            //批次新增訂單
            if (db.getPairFunction().equals(db.getPairSet().phpSQLgetOrderNewId) &&
                    orderIndex < orderList.size() - 1){
                db = new PhpDB(context, this);
                orderIndex ++;
                Log.e("orderIndex", orderIndex+"");
                getOrderID();
            }
//            //批次新增菜單
//            if (db.getPairFunction().equals(db.getPairSet().phpSQLnewOrderSub) &&
//                    orderIndex < orderList.size() - 1) {
//                if (menuIndex < orderList.get(orderIndex).size()) {
//                    db = new PhpDB(context, this);
//                    setOrderDetail();
//                    menuIndex++;
//                }
//                orderIndex++;
//            }
        }
    }
//  endregion

    private static void showOrdersDetail(){ //debug用
        for(int i = 0; i < orderList.size(); i ++){
            Log.e("Order NO", i + "\n");
            Log.e("OrderID", orderList.get(i).getOrderId() + "\n");
            Log.e("OrderTotal", orderList.get(i).getTotalPrice() + "\n");
            Log.e("OrderDT", orderList.get(i).getOrderDT("read") + "\n\n");
        }
    }
}
