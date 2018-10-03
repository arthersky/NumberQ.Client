package langotec.numberq.client.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;

import langotec.numberq.client.R;

public class Activity_PaySelect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_pay_select);
        getWindow().setEnterTransition(new Fade().setDuration(2000)); //淡入淡出轉場動畫
        //返回上一頁功能
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //取得上一頁傳來價格
        Bundle bundle = getIntent().getExtras();
        Log.e("Bundle Price:",""+bundle.getInt("price"));
    }

    //Activity 生命週期===============================
    @Override
    protected void onResume() { super.onResume();}
    @Override
    protected void onPause() { super.onPause(); }
    @Override
    protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onDestroy() { super.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); }
    //Activity 生命週期===============================END
}
