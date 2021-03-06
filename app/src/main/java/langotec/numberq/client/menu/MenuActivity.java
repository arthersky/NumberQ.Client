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
        ArrayList<Menu> menus = (ArrayList) getIntent().getSerializableExtra("menuList");
        setTitle(menus.get(0).getHeadName() + " - " + menus.get(0).getBranchName());
        listView.setEmptyView(findViewById(R.id.emptyView));
        MenuBaseAdapter adapter = new MenuBaseAdapter(context, menus);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        menu.findItem(R.id.order_refresh).setVisible(false);
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
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                System.gc();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
