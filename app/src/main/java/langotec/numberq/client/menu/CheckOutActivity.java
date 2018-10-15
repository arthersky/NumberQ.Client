package langotec.numberq.client.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.dbConnect.parseJSON;
import langotec.numberq.client.login.Member;
import langotec.numberq.client.map.PhpDB;
import langotec.numberq.client.service.OrderCountDown;

public class CheckOutActivity extends AppCompatActivity {

    private Cart cart;
    private Context context;
    private static LoadingDialog loadingDialog;
    private static AlertDialog alertDialog;
    private static WeakReference<Context> weakReference;
    private static PhpDB db;
    private static Member member;
    public static boolean orderCreated, allowBack;
    public static int orderIndex, menuIndex;
    public static ArrayList<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        context = this;
        weakReference = new WeakReference<>(context);
        cart = Cart.getInstance(context);
        orderList = splitOrders();
        allowBack = true;
        member = findMemberFile();
        setLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orderCreated)
            CheckOutActivity.showDialog("createFinish");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadingDialog != null)
            loadingDialog.closeDialog();
        if (allowBack)
            cart.saveCartFile(context);
    }

    @Override
    public void onBackPressed() {
        if (allowBack)
            super.onBackPressed();
    }

    //  region 例行性的OptionsMenu設定
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        menu.findItem(R.id.order_refresh).setVisible(false);
        menu.findItem(R.id.search_button).setVisible(false);
        menu.findItem(R.id.menu_cart_clear).setVisible(false);
        menu.findItem(R.id.menu_cart_createOrder).setVisible(false);
        menu.findItem(R.id.menu_backHome).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_backHome:
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("currentPage", 2);
                startActivity(intent);
                System.gc();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
//  endregion

    private void setLayout() {
        setTitle(getString(R.string.checkOut_title));
        ListView listView = findViewById(R.id.order_list);
        MenuBaseAdapter adapter = new MenuBaseAdapter(context, orderList);
        listView.setAdapter(adapter);
    }

    //  region拆單
    private ArrayList<Order> splitOrders() {
        HashMap<String, Order> orderMap = new HashMap<>();
        //先加入第0筆cart資料進入HashMap(用Map純粹為了方便比較key)
        orderMap.put(cart.get(0).getHeadName() + cart.get(0).getBranchName(), new Order());
        orderMap.get(cart.get(0).getHeadName() + cart.get(0).getBranchName()).getMenuList().add(cart.get(0));

        //再比較key值是否與Cart內的店名相同
        for (int i = 0; i < cart.size(); i++) {
            int index = 0;
            for (String key : orderMap.keySet()) {
                index++;
                if (key.equals(cart.get(i).getHeadName() + cart.get(i).getBranchName()) && i != 0) {
                    orderMap.get(key).getMenuList().add(cart.get(i));
                    break;

                    //如果已經跑到最後一筆key而且key不相符時，即新增一筆Order進入Map
                } else if (index == orderMap.size() &&
                        !key.equals(cart.get(i).getHeadName() + cart.get(i).getBranchName())) {
                    orderMap.put(cart.get(i).getHeadName() + cart.get(i).getBranchName(), new Order());
                    orderMap.get(cart.get(i).getHeadName() + cart.get(i).getBranchName())
                            .getMenuList().add(cart.get(i));
                    break;
                }
            }
        }
        ArrayList<Order> orderList = new ArrayList<>(orderMap.values());
        //順便設定Order物件的一些參數
        for (Order order : orderList){
            order.setHeadName(order.getMenuList().get(0).getHeadName());
            order.setBranchName(order.getMenuList().get(0).getBranchName());
            order.setFrom("fromCheckOutActivity");
        }
        return orderList;
    }

//  endregion

    //  region 處理訂單至資料庫
    //  TODO 判斷建立訂單失敗的邏輯似乎不OK
    public void onCheckOutClick(View view) {
        showDialog("createOrder");
    }

    public static void showDialog(String type) {
        final Context dialogContext = weakReference.get();
        final AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false);
        if (type.equals("createOrder")) {//按下GPay結帳的提醒
            builder.setTitle(dialogContext.getString(R.string.checkOut_dialog_title))
                    .setMessage(dialogContext.getString(R.string.checkOut_dialog_message))
                    .setPositiveButton(dialogContext.getString(R.string.menu_confirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    createOrders();
                                    loadingDialog = new LoadingDialog(weakReference);
                                    loadingDialog.setMessage(dialogContext.
                                            getString(R.string.checkOut_creatingOrders));
                                    allowBack = false;
                                }
                            })
                    .setNeutralButton(dialogContext.getString(R.string.menu_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alertDialog.dismiss();
                                }
                            });
        } else if (type.equals("createFinish")) {//順利建立訂單後的顯示
            builder.setTitle(dialogContext.getString(R.string.checkOut_createOrderSuccess))
                    .setPositiveButton(dialogContext.getString(R.string.menu_confirm),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    allowBack = true;
                                    orderCreated = false;
                                    alertDialog.dismiss();
                                    loadingDialog.closeDialog();
                                    Intent intent = new Intent(dialogContext, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("currentPage", 1);
                                    dialogContext.startActivity(intent);
                                    Cart.getInstance(dialogContext).clear();
                                    ((Activity) dialogContext).finish();
                                }
                            });
        }else if (type.equals("createFailure")) {//建立訂單失敗的顯示
            builder.setTitle(dialogContext.getString(R.string.checkOut_createOrderFailure))
                    .setMessage(dialogContext.getString(R.string.checkOut_createOrderRetry))
                    .setPositiveButton(dialogContext.getString(R.string.menu_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    loadingDialog.closeDialog();
                                    allowBack = true;
                                    orderCreated = false;
                                    alertDialog.dismiss();
                                }
                            });
        }
        alertDialog = builder.create();
        if (!alertDialog.isShowing())
            alertDialog.show();
    }

    private static void createOrders() {
        orderIndex = 0;
        db = new PhpDB(weakReference, new OrderHandler());
        getOrderID();
        Log.e("等待資料回應:", new Date().toString());
    }

    public static void getOrderID() {
        db.getPairSet().setPairFunction(db.pairSet.phpSQLgetOrderNewId); //取新訂單
        db.getPairSet().setPairSearch(1, member.getCustomerUserId());
        Log.e("批次新增訂單開始時間:", new Date().toString());
        Log.e("會員編號", member.getCustomerUserId() + "");
        new Thread(db).start();
    }

    public static void setOrderDetail() {
        //已經建立訂單後設定Order物件的結束時間
        for (Order order : orderList) {
            order.setOrderGetDT();
        }
        for (int i = 0; i < orderList.size(); i++) {
            for (int i2 = 0; i2 < orderList.get(i).getMenuList().size(); i2++) {
                db = new PhpDB(weakReference, new OrderHandler());
                //新增訂單內部份資料
                db.getPairSet().setPairFunction(db.pairSet.phpSQLsetOrderUpdate);
                //OrderID
                db.getPairSet().setPairSearch(1, orderList.get(i).getOrderId());
                //HeadID
                db.getPairSet().setPairSearch(3, orderList.get(i).getMenuList().get(i2).getHeadId());
                //BranchID
                db.getPairSet().setPairSearch(4, String.valueOf(orderList.get(i).
                        getMenuList().get(i2).getBranchId()));
                //UserPhone
                db.getPairSet().setPairSearch(6, member.getUserPhone());
                //PayCheck設定已付款
                db.getPairSet().setPairSearch(10, "1");
                //TotalPrice
                db.getPairSet().setPairSearch(11, String.valueOf(orderList.get(i).getTotalPrice()));
                //orderGetDT(finishTime)
                db.getPairSet().setPairSearch(14,
                        orderList.get(orderIndex).getOrderGetDT("whatever"));
                Log.e("批次新增第" + i + "筆訂單資料，開始時間", new Date().toString());
                new Thread(db).start();
            }
        }
    }

    public static void setMenuDetail() {
        menuIndex = 0;
        for (int i = 0; i < orderList.size(); i++) {
            for (int i2 = 0; i2 < orderList.get(i).getMenuList().size(); i2++) {
                db = new PhpDB(weakReference, new OrderHandler());
                //訂單菜單新增
                db.getPairSet().setPairFunction(db.pairSet.phpSQLnewOrderSub);
                //OrderID
                db.getPairSet().setPairSearch(1, orderList.get(i).getOrderId());
                //productId 產品代號 同一個訂單不允許產品代碼重複
                db.getPairSet().setPairSearch(2, orderList.get(i).getMenuList().
                        get(i2).getProductId());
                //quantity 數量
                db.getPairSet().setPairSearch(3, String.valueOf(
                        orderList.get(i).getMenuList().get(i2).getQuantityNum()));
                //price 價格
                db.getPairSet().setPairSearch(4, orderList.get(i).getMenuList().get(i2).getPrice());
                Log.e("批次新增第" + i + "筆菜單，開始時間", new Date().toString());
                new Thread(db).start();
            }
        }
    }

    private static class OrderHandler extends Handler {
        Context context = weakReference.get();
        @Override
        public synchronized void handleMessage(Message msg) {
            Log.e("Handler 發送過來的訊息", msg.obj.toString());
            if (db.getState()) {
                Log.e("資料回應時間", new Date().toString());
                Log.e("回應副程式", db.getPairFunction());
                String tmp = "";
                for (int y = 0; y < db.getRowSize(); y++) {
                    for (Object key : ((PhpDB.ItemListRow) db.getDataSet().get(y)).getAll().keySet()) {
                        if (tmp.length() > 0)
                            tmp += ",";
                        tmp += ((PhpDB.ItemListRow) db.getDataSet().get(y)).get(key.toString()).toString();
                    }
                    Log.e("=========Debug=======", tmp);
                    String[] orderIDandTime = tmp.split(",");
                    if (db.getPairFunction().equals(db.getPairSet().phpSQLgetOrderNewId)) {
                        Log.e("orderIndex", orderIndex + "");
                        orderList.get(orderIndex).setOrderDT(orderIDandTime[0]);
                        orderList.get(orderIndex).setOrderId(orderIDandTime[1]);
                        if (orderIndex == orderList.size() - 1) {
                            orderIndex = 0;
                            setOrderDetail();
                            return;
                        }
                    } else if (db.getPairFunction().equals(db.getPairSet().phpSQLsetOrderUpdate)) {
                        if (tmp.equals("true")) {
                            setMenuDetail();
                        }else {
                            createOrderFailure("phpSQLsetOrderUpdate");
                        }
                    } else if (db.getPairFunction().equals(db.getPairSet().phpSQLnewOrderSub)) {
                        menuIndex++;
                        Cart cart = Cart.getInstance(context);
                        if (menuIndex == cart.size() && tmp.equals("true")) {
                            loadingDialog.closeDialog();
                            showDialog("createFinish");
                            showOrdersDetail();
                            orderCreated = true;
                            Intent intent = new Intent(context, OrderCountDown.class);
                            intent.putExtra("orderList", orderList);
                            context.startService(intent);
                        }else if (menuIndex == cart.size() && !tmp.equals("true")){
                            createOrderFailure("phpSQLnewOrderSub");
                        }
                    }
                }
            }
//            else {
//                Log.e("db.getState()", db.getState() + "");
//                createOrderFailure("db.getState() != true");
//            }
            //批次新增訂單
            if (db.getPairFunction().equals(db.getPairSet().phpSQLgetOrderNewId) &&
                    orderIndex < orderList.size() - 1) {
                orderIndex++;
                Log.e("orderIndex", orderIndex + "");
                getOrderID();
            }
        }
    }
    private static void createOrderFailure(String where){
        loadingDialog.closeDialog();
        showDialog("createFailure");
        Log.e("createOrderFailure", where);
    }
    //  endregion

    private static void showOrdersDetail() { //debug用
        for (int i = 0; i < orderList.size(); i++) {
            Log.e("Order NO", i + "\n");
            Log.e("OrderID", orderList.get(i).getOrderId() + "\n");
            Log.e("OrderTotal", orderList.get(i).getTotalPrice() + "\n");
            Log.e("OrderDT", orderList.get(i).getOrderDT("read") + "\n");
            Log.e("orderGetDT", orderList.get(i).getOrderGetDT("read") + "\n\n");
        }
    }

    private Member findMemberFile() {
        File fileDir = new File(String.valueOf(context.getFilesDir()) +
                "/customer.txt");
        String json = "";
        Member member = Member.getInstance();
        try {
            FileReader fileReader = new FileReader(fileDir);
            BufferedReader bReader = new BufferedReader(fileReader);
            json = bReader.readLine();
            bReader.close();
            Log.e("Member_json", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new parseJSON(json, member).parse();
    }
}
