package langotec.numberq.client.fragment;


import android.app.AlertDialog;
import android.app.FragmentContainer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.adapter.RecyclerViewAdapter;
import langotec.numberq.client.login.LoginActivity;
import langotec.numberq.client.login.Member;
import langotec.numberq.client.menu.Cart;
import langotec.numberq.client.menu.CheckOutActivity;

public class CartFragment extends Fragment {

    private Cart cart;
    private RecyclerViewAdapter cartAdapter;

    public CartFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //設定fragment自己有optionsMenu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cart = Cart.getInstance(getContext());
        //如果購物車是空的顯示fragment_empty頁面
        View cartView;
        if (cart.isEmpty()) {
            cartView = inflater.inflate(R.layout.fragment_empty, container, false);
            ImageView emptyView = (ImageView) cartView.findViewById(R.id.cart_emptyImage);
            TextView emptyText = (TextView) cartView.findViewById(R.id.cart_emptyText);
            emptyText.setText(getString(R.string.cart_isEmpty));
        //如果購物車有放東西時顯示fragment_cart頁面
        }else {
            cartView = inflater.inflate(R.layout.fragment_cart, container, false);
            cartAdapter = new RecyclerViewAdapter(cart);
            LinearLayoutManager manager = new LinearLayoutManager(getActivity());
            RecyclerView cartRecycler = (RecyclerView) cartView.findViewById(R.id.cart_recyclerView);
            cartRecycler.setAdapter(cartAdapter);
            cartRecycler.setLayoutManager(manager);
        }
        return cartView;
    }

    @Override
    public void onPause() {
        super.onPause();
        cart.saveCartFile(getContext());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //在此fragment拿掉search_button這個item
        menu.findItem(R.id.search_button).setVisible(false);
        menu.findItem(R.id.menu_cart_clear).setVisible(true);
        menu.findItem(R.id.menu_cart_createOrder).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!cart.isEmpty()) {
            switch (item.getItemId()) {
                case R.id.menu_cart_clear:
                    showDialog();
                    break;
                case R.id.menu_cart_createOrder:
                    if (Member.getInstance().checkLogin(getContext())) {
                        startActivity(new Intent(getContext(), CheckOutActivity.class));
                    }else {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.putExtra("startFrom", "fromCartFragment");
                        startActivity(intent);
                    }
                    break;
            }
        }else
            Toast.makeText(getContext(), getString(R.string.cart_isEmpty), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.cart_clearCart))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(getString(R.string.menu_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //清除購物車內資料，及更新CartFragment畫面
                                cart.clear();
                                getFragmentManager().beginTransaction().
                                    detach(CartFragment.this).attach(CartFragment.this).commit();
                            }
                        })
                .setNegativeButton(getString(R.string.menu_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .create().show();
    }
}
