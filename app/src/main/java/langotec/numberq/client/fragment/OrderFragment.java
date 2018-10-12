package langotec.numberq.client.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import langotec.numberq.client.R;
import langotec.numberq.client.menu.CheckOutActivity;
import langotec.numberq.client.menu.Order;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment {

    private ArrayList<Order> orderList;

    public OrderFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderList = CheckOutActivity.orderList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.e("orderList.size", orderList.size() + "");
        View orderView;
        if (orderList == null || orderList.size() == 0){
            orderView = inflater.inflate(R.layout.fragment_empty, container, false);
            TextView emptyText = (TextView) orderView.findViewById(R.id.emptyText);
            emptyText.setText(getString(R.string.order_emptyOrders));
        }else {
            orderView = inflater.inflate(R.layout.fragment_order, container, false);
        }
        // Inflate the layout for this fragment
        return orderView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //在此fragment拿掉search_button這個item
        menu.findItem(R.id.search_button).setVisible(false);
    }
}
