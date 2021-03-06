package langotec.numberq.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

import langotec.numberq.client.adapter.BottomNavigationViewHelper;
import langotec.numberq.client.adapter.ViewPagerAdapter;
import langotec.numberq.client.fragment.CartFragment;
import langotec.numberq.client.fragment.MoreFragment;
import langotec.numberq.client.fragment.OrderFragment;
import langotec.numberq.client.fragment.RecommendFragment;
import langotec.numberq.client.menu.CheckOutActivity;


public class MainActivity extends AppCompatActivity {

    //store array
    public static ArrayList<Store> storeList;
    //BottomNavigationView
    private BottomNavigationView bottomNavigationView;
    //viewPager
    private ViewPager viewPager;
    //Fragments
    private RecommendFragment recommendFragment;
    public static OrderFragment orderFragment;
    private MoreFragment moreFragment;
    private MenuItem prevMenuItem;
    /*為了讓AlertDialog裡的static方法能夠拿到cartFragment變數來更新
    CartFragment的畫面，CartFragment只能設為public static了*/
    public static CartFragment cartFragment;
    public static boolean allowBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allowBack = true;

        //take data bundle
        try{
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
    }

    //Override onNewIntent才可以從onResume抓到最新Intent
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", viewPager.getCurrentItem());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager();
        viewPager.setCurrentItem(getIntent().getIntExtra("currentPage", 0));
        if (CheckOutActivity.orderCreated)
            CheckOutActivity.showDialog("createFinish");
    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().putExtra("currentPage", viewPager.getCurrentItem());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_backHome).setVisible(false);
        menu.findItem(R.id.menu_cart_clear).setVisible(false);
        menu.findItem(R.id.menu_cart_createOrder).setVisible(false);
        menu.findItem(R.id.order_refresh).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (allowBack)
            super.onBackPressed();
    }

    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        recommendFragment = new RecommendFragment();
        orderFragment = new OrderFragment();
        cartFragment = new CartFragment();
        moreFragment = new MoreFragment();

        adapter.addFragment(recommendFragment);
        adapter.addFragment(orderFragment);
        adapter.addFragment(cartFragment);
        adapter.addFragment(moreFragment);

        viewPager.setAdapter(adapter);
        //Initializing the bottomNavigationView
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_recommend:
                                MainActivity.this.viewPager.setCurrentItem(0);
                                break;
                            case R.id.action_order:
                                MainActivity.this.viewPager.setCurrentItem(1);
                                break;
                            case R.id.action_cart:
                                MainActivity.this.viewPager.setCurrentItem(2);
                                break;
                            case R.id.action_more:
                                MainActivity.this.viewPager.setCurrentItem(3);
                                break;
                        }
                        return false;
                    }
                });

        //Initializing viewPager
        final String[] TITLES = getResources().getStringArray(R.array.page_indicators);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                Log.d("page", "onPageSelected: " + position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                //設定頁面標題
                switch (position) {
                    case 0:
                        setTitle(TITLES[position]);
                        break;
                    case 1:
                        setTitle(TITLES[position]);
                        break;
                    case 2:
                        setTitle(TITLES[position]);
                        break;
                    case 3:
                        setTitle(TITLES[position]);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}
