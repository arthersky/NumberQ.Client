package langotec.numberq.client.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

public class OrderCountDown extends Service{

    private ArrayList orderList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("myLog","onStartCommand");
        orderList = (ArrayList)intent.getSerializableExtra("orderList");
        new Thread(new RefreshOrderTime()).start();
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
            int i = 0;
            while (true){
                i++;
                Log.e("ThreadIsRunning", new Date().toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == 30){
                    stopSelf();
                    break;
                }
            }
        }
    }
}
