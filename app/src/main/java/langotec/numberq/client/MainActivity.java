package langotec.numberq.client;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import langotec.numberq.client.adapter.BottomNavigationViewHelper;
import langotec.numberq.client.adapter.ViewPagerAdapter;
import langotec.numberq.client.fragment.CartFragment;
import langotec.numberq.client.fragment.MoreFragment;
import langotec.numberq.client.fragment.OrderFragment;
import langotec.numberq.client.fragment.RecommendFragment;


public class MainActivity extends AppCompatActivity {
    //location
    double lat = 0.0;
    double lng = 0.0;

    //store array
    public static ArrayList<Store> storeList;

    //title Array
    private String[] titles;

    //BottomNavigationView
    private BottomNavigationView bottomNavigationView;

    //viewPager
    private ViewPager viewPager;

    //Fragments
    private RecommendFragment recommendFragment;
    private OrderFragment orderFragment;
    private MoreFragment moreFragment;
    private MenuItem prevMenuItem;

    /*為了讓AlertDialog裡的static方法能夠拿到cartFragment變數來更新
    CartFragment的畫面，CartFragment只能設為public static了*/
    public static CartFragment cartFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //take data bundle
        try{
//            Bundle data = getIntent().getExtras();
//            if (lat == 0.0){
//                lat = data.getDouble("lat");
//            }
//            if (lng == 0.0){
//                lng = data.getDouble("lng");
//            }
//            Log.e("Location","Lat:"+lat+" Lng:"+lng);
            if (storeList == null){
                try{
                    storeList = new ArrayList<>();
                    storeList = (ArrayList<Store>)getIntent().getSerializableExtra("storeList");
                    Log.e("data",""+storeList.get(1).getHeadName());
                }catch (Exception e){
                    Log.e("dataE",""+e.toString());
                }
            }
        }catch (Exception e){
            Log.e("TAG",e.toString());
        }

        //Initializing the bottomNavigationView
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_recommend:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.action_order:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.action_cart:
                                viewPager.setCurrentItem(2);
                                break;
                            case R.id.action_more:
                                viewPager.setCurrentItem(3);
                                break;
                        }
                        return false;
                    }
                });

        //Initializing viewPager
        titles = getResources().getStringArray(R.array.page_indicators);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                Log.d("page", "onPageSelected: "+position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                //設定頁面標題
                switch (position){
                    case 0:
                        setTitle(titles[position]);
                        break;
                    case 1:
                        setTitle(titles[position]);
                        break;
                    case 2:
                        setTitle(titles[position]);
                        break;
                    case 3:
                        setTitle(titles[position]);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setupViewPager(viewPager);
        //如果是由修改購物車數量啟動MainActivity，需跳轉回到購物車頁面
        String extra = (String) getIntent().getStringExtra("from");
        if(extra != null && extra.equals("fromSelectActivity")) {
            viewPager.setCurrentItem(2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        recommendFragment =new RecommendFragment();
        orderFragment =new OrderFragment();
        cartFragment =new CartFragment();
        moreFragment = new MoreFragment();

//        Bundle bundle = new Bundle();
//        bundle.putSerializable("storeList",storeList);
//        recommendFragment.setArguments(bundle);

        adapter.addFragment(recommendFragment);
        adapter.addFragment(orderFragment);
        adapter.addFragment(cartFragment);
        adapter.addFragment(moreFragment);

        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_backHome).setVisible(false);
        menu.findItem(R.id.menu_cart_clear).setVisible(false);
        menu.findItem(R.id.menu_cart_createOrder).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search_button:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
