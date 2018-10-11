package langotec.numberq.client.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.Store;
import langotec.numberq.client.dbConnect.MenuDBConn;
import langotec.numberq.client.menu.Cart;
import langotec.numberq.client.menu.Menu;
import langotec.numberq.client.menu.SelectedActivity;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    //先用ArrayList拿東西進來再轉型
    private ArrayList data;
    private Context context;
    public MenuDBConn menuDBConn;

    public RecyclerViewAdapter(ArrayList data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (data.get(0) instanceof Store) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.cardview_store, viewGroup, false);

        }else if (data instanceof Cart){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.cardview_cart, viewGroup, false);
        }
        // ViewHolder參數一定要是Layout的根節點。
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder,
                                 @SuppressLint("RecyclerView") final int position) {
        View view = viewHolder.view;
        context = view.getContext();

        if (data.get(0) instanceof Store) {
            final Store store = (Store) data.get(position);
            TextView textStoreName = (TextView) view.findViewById(R.id.textView1);
            TextView textBranchName = (TextView) view.findViewById(R.id.textView2);
            TextView textNumber = (TextView)view.findViewById(R.id.textView4);
            TextView textMinute = (TextView)view.findViewById(R.id.textView6);
            TextView textIntroduction = (TextView)view.findViewById(R.id.textView8);
            ImageView storeIconImage = (ImageView) view.findViewById(R.id.store_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Store s = (Store) data.get(position);
                    menuDBConn = new MenuDBConn(s. getHeadName(),
                            s.getBranchName(), s.getBranchId(), context);
                    menuDBConn.execute();
                }
            });

            if (store.getHeadName().equals("八方雲集")){
                storeIconImage.setImageDrawable(context.getDrawable(R.drawable.bafun));
                textIntroduction.setText("鍋貼、水餃專賣店");
            }else if (store.getHeadName().equals("鼎泰豐")){
                storeIconImage.setImageDrawable(context.getDrawable(R.drawable.ding));
                textIntroduction.setText("世界知名小籠包");
            }
            textStoreName.setText(store.getHeadName());
            textBranchName.setText(store.getBranchName());
        }

        else if (data instanceof Cart){
            Menu menu = (Menu) data.get(position);

            ImageView cartIconImage = (ImageView) view.findViewById(R.id.cart_imageView);
            TextView cartStoreName = (TextView) view.findViewById(R.id.cartStoreName_textView);
            TextView cartDesc = (TextView) view.findViewById(R.id.cartDesc_textView);
            TextView cartTotal = (TextView) view.findViewById(R.id.cartTotal_textView);

            menu.setImageView(cartIconImage);
            cartStoreName.setText(menu.getHeadName() + "-" + menu.getBranchName());
            cartDesc.setText(menu.getProductName());
            cartTotal.setText(context.getResources().getString(R.string.menu_quantity) +
                    menu.getQuantityNum() + "\t\t\t" +
                    context.getResources().getString(R.string.menu_singlePrice) + menu.getPrice());

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDialog(position);
                    return false;
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, context.getResources().getString(R.string.cart_click),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View view;

        ViewHolder(View view){
            super(view);
            this.view = view;
        }
    }

    //修改購物車內容的對話框功能
    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.cart_modify))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(context.getResources().getString(R.string.cart_deleteMenu),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //移除Cart內選定的商品
                                data.remove(position);
                                notifyDataSetChanged();
                                Fragment fragment = MainActivity.cartFragment;
                                fragment.getFragmentManager().beginTransaction().
                                        detach(fragment).attach(fragment).commit();
                            }
                        })
                .setNegativeButton(context.getResources().getString(R.string.cart_modifyQuantity),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Menu menu = (Menu) data.get(position);
                                menu.setFrom("fromCartFragment");
                                Intent intent = new Intent(context, SelectedActivity.class);
                                intent.putExtra("Menu", menu);
                                context.startActivity(intent);
                            }
                        })
                .setNeutralButton(context.getResources().getString(R.string.menu_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
    }
}

