package langotec.numberq.client.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class OrderCountDown extends Service{

    private boolean isChecked = false;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isChecked){
            //無窮迴圈
            while(true){
                countDownTimer();
                // if DB return finish{
                //      stopSelf();
                // }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void countDownTimer(){
        //計算30秒
        countDownTimer = new CountDownTimer
                (30000,1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                //check DB return order status
            }
        }.start();
    }
}
