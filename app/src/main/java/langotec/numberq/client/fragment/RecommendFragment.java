package langotec.numberq.client.fragment;


import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.Store;
import langotec.numberq.client.WelcomeActivity;
import langotec.numberq.client.adapter.MainSliderAdapter;
import langotec.numberq.client.adapter.PicassoImageLoadingService;
import langotec.numberq.client.adapter.RecyclerViewAdapter;
import langotec.numberq.client.map.Activity_GoogleMap;
import ss.com.bannerslider.Slider;

public class RecommendFragment extends Fragment {

    //store array
    private ArrayList<Store> storeList;

    private FloatingActionButton fb;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Slider slider;
    private MainSliderAdapter adapter;
    Context context = null;

    public RecommendFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        storeList = MainActivity.storeList;
        context = getContext(); //android.app.Application@ce249d0
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        // Inflate the layout for this fragment
        //setupViews();

        Slider.init(new PicassoImageLoadingService(context));
        slider = (Slider)view.findViewById(R.id.banner_slider);
        adapter = new MainSliderAdapter();
        slider.setAdapter(adapter);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.store_recyclerView);

        // 若設為FixedSize可以增加效率不過就喪失了彈性
        mRecyclerView.setHasFixedSize(true);

        // 選擇一種Layout管理器這邊是選擇（linear layout manager）
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // 設定適配器
        mAdapter = new RecyclerViewAdapter(storeList);
        mRecyclerView.setAdapter(mAdapter);

        //Initializing floating button
        fb = (FloatingActionButton) view.findViewById(R.id.floatingActionButton);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //取得經緯度座標
                    float latitude = (float) WelcomeActivity.currentLocation.getLatitude();
                    float longitude = (float) WelcomeActivity.currentLocation.getLongitude();
                    //建立URI字串
                    String uri = String.format("geo:%f,%f?z=18", latitude, longitude);
                    //建立Intent物件
                    //Intent geoMap = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    //startActivity(geoMap);  // 啟動活動
                    Intent intent = new Intent(getActivity(), Activity_GoogleMap.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("work", 1);//傳遞Int 直接呼叫顯示附近店家
                    intent.putExtras(bundle);
                    getActivity().startActivity(intent, ActivityOptions.
                            makeSceneTransitionAnimation((AppCompatActivity) context).toBundle());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter.menuDBConn != null) {
            mAdapter.menuDBConn.cancel(true);
            mAdapter.menuDBConn.loadingDialog.closeDialog();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_button:
                showDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(){
        final EditText editText = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_searchTitle)
                .setMessage(R.string.dialog_searchMessage)
                .setView(editText)
                .setPositiveButton(R.string.menu_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString().length() != 0) {
                            Intent intent = new Intent(getActivity(), Activity_GoogleMap.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("work", 3);//傳遞Int 直接呼叫顯示附近店家
                            bundle.putString("searchString", editText.getText().toString());
                            intent.putExtras(bundle);
                            getActivity().startActivity(intent, ActivityOptions.
                                    makeSceneTransitionAnimation((AppCompatActivity) context).toBundle());
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.menu_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

}
