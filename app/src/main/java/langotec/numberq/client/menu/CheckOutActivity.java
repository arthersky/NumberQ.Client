package langotec.numberq.client.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;

public class CheckOutActivity extends AppCompatActivity {

    private Cart cart;
    private Context context;
    private ArrayList<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        context  = this;
        cart = Cart.getInstance(context);
        orderList = new ArrayList<>();
        makeOrders();
        setLayout();
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
                System.gc();
                startActivity(new Intent(context, MainActivity.class));
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

    private void makeOrders(){
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
        Log.e("orderList Total", orderMap.size()+"");
        for (Order o : orderMap.values()){
            orderList.add(o);
        }
    }

    public void onCheckOutClick(View view){

    }
}
