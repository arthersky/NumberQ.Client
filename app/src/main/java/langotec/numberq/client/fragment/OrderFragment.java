package langotec.numberq.client.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.dbConnect.parseJSON;
import langotec.numberq.client.login.LoginActivity;
import langotec.numberq.client.map.PhpDB;
import langotec.numberq.client.login.Member;
import langotec.numberq.client.menu.CheckOutActivity;
import langotec.numberq.client.menu.MenuBaseAdapter;
import langotec.numberq.client.menu.Order;

public class OrderFragment extends Fragment {

    private static ArrayList<Order> orderList;
    private static PhpDB phpDB;
    private static WeakReference<Context> weakReference;
    private static Member member;
    public static OrderFragment orderFragment;

    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weakReference = new WeakReference<>(getContext());
        member = findMemberFile();
        setHasOptionsMenu(true);
        queryOrder();
//        orderList = CheckOutActivity.orderList;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View orderView;
        if (orderList == null){
            orderView = inflater.inflate(R.layout.fragment_empty, container, false);
            TextView emptyText = (TextView) orderView.findViewById(R.id.emptyText);
            emptyText.setText(getString(R.string.order_queryProcessing));
        }else if (orderList.size() == 0){
            orderView = inflater.inflate(R.layout.fragment_empty, container, false);
            TextView emptyText = (TextView) orderView.findViewById(R.id.emptyText);
            emptyText.setText(getString(R.string.order_emptyOrders));
        }else {
            orderView = inflater.inflate(R.layout.fragment_order, container, false);
            ListView listView = orderView.findViewById(R.id.orderFragment_list);
            MenuBaseAdapter adapter = new MenuBaseAdapter(getContext(), orderList);
            listView.setAdapter(adapter);
        }
        return orderView;
    }

    @Override
    public void onResume() {
        super.onResume();
        orderFragment = this;
    }

//  region
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.order_refresh).setVisible(true);
        menu.findItem(R.id.search_button).setVisible(false);
        menu.findItem(R.id.menu_cart_clear).setVisible(false);
        menu.findItem(R.id.menu_cart_createOrder).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.order_refresh:
                orderList = null;
                queryOrder();
                refreshOrder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Member findMemberFile() {
        File fileDir = new File(String.valueOf(getContext().getFilesDir()) +
                "/customer.txt");
        String json = "";
        Member member = Member.getInstance();
        try {
            FileReader fileReader = new FileReader(fileDir);
            BufferedReader bReader = new BufferedReader(fileReader);
            json = bReader.readLine();
            bReader.close();
//            Log.e("Member_json", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new parseJSON(json, member).parse();
    }

    private void queryOrder(){
        phpDB = new PhpDB(weakReference, new OrderHandler(weakReference));
        phpDB.getPairSet().setPairFunction(phpDB.pairSet.phpSQLorderMSList); //查詢完整定單資料
        phpDB.getPairSet().setPairSearch(2, member.getCustomerUserId()); //使用者ID查詢
        phpDB.getPairSet().setPairJSON();
        Log.e("等待資料回應:", new Date().toString());
        new Thread(phpDB).start();
    }

    private static class OrderHandler extends Handler {
        WeakReference weakReference;

        OrderHandler(WeakReference weakReference) {
            this.weakReference = weakReference;
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            Log.e("Handler 發送過來的訊息", msg.obj.toString());
            if (phpDB.getState()) {
                Log.e("資料回應時間", new Date().toString());
                Log.e("回應副程式", phpDB.getPairFunction());
                if (phpDB.getPairFunction().equals(phpDB.getPairSet().phpSQLorderMSList)) {
                    parseOrderJSON(phpDB.getJSONData());
                    refreshOrder();
                }
            }
        }
    }

    public static void refreshOrder(){
        Fragment fragment = OrderFragment.orderFragment;
        fragment.getFragmentManager().beginTransaction().detach(fragment)
                .attach(fragment).commit();
    }

    public static void parseOrderJSON(JSONArray ja){
        orderList = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            boolean flag = false;
            try {
                JSONObject jsObj = ja.getJSONObject(i);
                Log.e("jsObj", jsObj.toString());
                String orderId = jsObj.getString("orderId");
                String productName = jsObj.getString("productName");
                String quantity = jsObj.getString("quantity");
                String sumPrice = jsObj.getString("sumprice");

                for (int i2 = 0; i2 < orderList.size(); i2++){
                    Order indexOrder = orderList.get(i2);
                    if (indexOrder.getOrderId().equals(orderId)){
                        indexOrder.getProductName().add(productName);
                        indexOrder.getQuantity().add(quantity);
                        indexOrder.getSumPrice().add(sumPrice);
                        flag = true;
                        break;
                    }
                }
                if (flag)
                    continue;
                String headName = jsObj.getString("HeadName");
                String branchName = jsObj.getString("BranchName");
                String headImg = jsObj.getString("Headimg");
                String userId = jsObj.getString("userId");
                String HeadId = jsObj.getString("HeadId");
                String BranchId = jsObj.getString("BranchId");
                String deliveryType = jsObj.getString("deliveryType");
                String contactPhone = jsObj.getString("contactPhone");
                String deliveryAddress = jsObj.getString("deliveryAddress");
                String taxId = jsObj.getString("taxId");
                String payWay = jsObj.getString("payWay");
                int payCheck = Integer.parseInt(jsObj.getString("payCheck"));
                int totalPrice = Integer.parseInt(jsObj.getString("totalPrice"));
                String comment = jsObj.getString("comment");
//                String userName = jsObj.getString("userName");
                String orderDT = jsObj.getString("orderDT");
                String orderGetDT = jsObj.getString("orderGetDT");
                Order order = new Order(
                        headImg, orderId, userId, HeadId, BranchId, headName, branchName,
                        deliveryType,
                        contactPhone, deliveryAddress, taxId, payWay, payCheck,
                        totalPrice, comment, "whatever", orderDT, orderGetDT);
                order.setFrom("fromDB");
                order.getProductName().add(productName);
                order.getQuantity().add(quantity);
                order.getSumPrice().add(sumPrice);
                orderList.add(order);
            } catch (JSONException e) {
                Log.e("JSON ERROR", e.toString());
            }
        }

        Log.e("orderlist","order List size:" + orderList.size());
    }
}
