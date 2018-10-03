package langotec.numberq.client.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.Store;
import langotec.numberq.client.WelcomeActivity;
import langotec.numberq.client.adapter.MainSliderAdapter;
import langotec.numberq.client.adapter.RecyclerViewAdapter;
import langotec.numberq.client.adapter.PicassoImageLoadingService;
import langotec.numberq.client.R;
import langotec.numberq.client.map.Activity_GoogleMap;
import ss.com.bannerslider.Slider;

public class RecommendFragment extends Fragment {

    //store array
    private ArrayList<Store> storeList;

    private FloatingActionButton fb;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Slider slider;
    private MainSliderAdapter adapter;
    Context context = null;

    public RecommendFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        storeList = (ArrayList<Store>) getArguments().getSerializable("storeList");
        storeList = MainActivity.storeList;
        Log.e("datafrg",""+storeList.get(1).getHeadName());
        context = getActivity().getApplicationContext(); //android.app.Application@ce249d0
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
//        String[] mDataset = context.getResources().getStringArray(R.array.store);
//        String[] storeHeadName = new String[storeList.size()];
//        for (int i=0;i<storeList.size();i++){
//            String storeName = storeList.get(i).getHeadName();
//            storeHeadName[i] = storeName;
//        }
//        ArrayList myDataset = new ArrayList();
//        for (int i = 0; i < storeHeadName.length; i++){
//            myDataset.add(storeHeadName[i]);
//        }

        // 設定適配器
//        mAdapter = new RecyclerViewAdapter(myDataset);
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
                    getActivity().startActivity(intent);
                }catch (Exception e){
                    Log.e("TAG",e.toString());
                }
                Toast.makeText(context, "Open Map", Toast.LENGTH_SHORT).show();
            }
        });

        return view;

    }
}
