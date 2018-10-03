package langotec.numberq.client.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;

public class MenuActivity extends AppCompatActivity {

    private Context context;
    private ArrayList<Menu> menus;
    private MenuBaseAdapter adapter;
    private String headName;
    private String branchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context = this;
        setLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
    }

    private void setLayout() {
        ListView listView = findViewById(R.id.list);
        menus = (ArrayList) getIntent().getSerializableExtra("menuList");
        headName = "鼎泰豐";
        branchName = "信義店";

        //Todo: 獲取從MainActivity的店家資料
        setTitle(headName + " - " + branchName);

        listView.setEmptyView(findViewById(R.id.emptyView));

        adapter = new MenuBaseAdapter(context, menus);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Menu menu = (Menu) adapterView.getItemAtPosition(i);
                Intent intent = new Intent();
                //把被按下的Menu物件放進intent
                intent.putExtra("Menu", menu);
                intent.setClass(context, SelectedActivity.class);
                startActivity(intent);
            }
        });
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

}
