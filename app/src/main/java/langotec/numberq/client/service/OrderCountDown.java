package langotec.numberq.client.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import langotec.numberq.client.menu.Order;

public class OrderCountDown extends Service{

    private ArrayList<Order> orderList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("myLog","onStartCommand");
        if (intent != null) {
            orderList = (ArrayList<Order>) intent.getSerializableExtra("orderList");
            new Thread(new RefreshOrderTime()).start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("orderCountDown", "onDestroy()");
    }

    private class RefreshOrderTime implements Runnable{
        @Override
        public void run() {
            while (true){
                Log.e("Service", "CountDown Thread is Running, orderList.size = " + orderList.size());
                for(int i = 0; i < orderList.size(); i++) {
                    Order order = orderList.get(i);
                    if (Calendar.getInstance().compareTo(order.getOrderGetDT()) > 0) {
                        Log.e("現在時間", new Date().toString());
                        Log.e("單號時間", order.getOrderGetDT().getTime().toString());
                        Log.e("單號", order.getOrderId() + "的訂單完成時間已經到了");
                        orderList.remove(i);
                    }
                }
                if (orderList.isEmpty()){
                    stopSelf();
                    break;
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
