package langotec.numberq.client.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.adapter.RecyclerViewAdapter;

public class MenuBaseAdapter extends BaseAdapter {

	private Context context;
	private ArrayList data;
	private LayoutInflater mInflater;

	MenuBaseAdapter(Context context, ArrayList data) {
		this.context = context;
		this.data = data;
		mInflater = LayoutInflater.from(context);
	}

	@SuppressLint("SetTextI18n")
    @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (data.get(0) instanceof Menu) {
            final Menu menu = (Menu) getItem(position);
            if (convertView == null) {
				convertView = mInflater.inflate(R.layout.layout_menu_row, null);
				holder = new ViewHolder();
				holder.imageView = (ImageView) convertView.findViewById(R.id.image);
				holder.textPrice = (TextView) convertView.findViewById(R.id.price);
				holder.textName = (TextView) convertView.findViewById(R.id.productName);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textPrice.setText("NT $" + menu.getPrice());
			holder.textName.setText(menu.getProductName());
			menu.setImageView(holder.imageView);
			convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    //把被按下的Menu物件放進intent
                    intent.putExtra("Menu", menu);
                    intent.setClass(context, SelectedActivity.class);
                    context.startActivity(intent);
                }
            });
		}

		else if (data.get(0) instanceof Order){
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.cardview_order_row, null);
				holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.order_storeImage);
                holder.textName = (TextView) convertView.findViewById(R.id.order_storeName);
                holder.textMenuName = (TextView) convertView.findViewById(R.id.order_menuName);
                holder.textQuantity = (TextView) convertView.findViewById(R.id.order_quantity);
				holder.textPrice = (TextView) convertView.findViewById(R.id.order_singlePrice);
				holder.textTotal = (TextView) convertView.findViewById(R.id.order_totalPrice);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
            final Order order = (Order) getItem(position);
            String str[] = setTextLoop(order);

			//設定單一訂單標題
            final Menu menu = ((Order) data.get(position)).get(0);
			if (menu.getHeadName().equals("鼎泰豐"))
				holder.imageView.setImageResource(R.drawable.ding);
			else
				holder.imageView.setImageResource(R.drawable.bafun);

			holder.textName.setText(menu.getHeadName() + " - " + menu.getBranchName());
            holder.textMenuName.setText(str[0]);
            holder.textQuantity.setText(str[1]);
            holder.textPrice.setText(str[2]);
            holder.textTotal.setText(str[3]);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, context.getResources().
                            getString(R.string.checkOut_click),Toast.LENGTH_SHORT).show();
                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDialog(position, order);
                    return false;
                }
            });
		}
		return convertView;
	}

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.indexOf(getItem(position));
    }

	//製造可以正確排列顯示菜單內容的字串陣列
	private String[] setTextLoop(Order order){
	    String str[] = new String[]{"", "", "", ""};
	    int total = 0;
        for (int i = 0; i < order.size(); i++) {
            Menu menu = order.get(i);
            str[0] += String.valueOf(i + 1) + ": " + menu.getProductName() + "\n";
            str[1] += context.getString(R.string.menu_quantity) + menu.getQuantityNum() + "\n";
            str[2] += context.getString(R.string.menu_singlePrice) + menu.getPrice() + "\n";
            total += Integer.parseInt(menu.getPrice()) * menu.getQuantityNum();
        }
        str[3] += context.getString(R.string.menu_totalPrice) + String.valueOf(total);
	    return str;
    }

    //修改購物車內容的對話框功能
    private void showDialog(final int position, final Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.cart_modify))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(context.getResources().getString(R.string.checkOut_delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //移除Cart內被選中的店家的Menu
                        Cart cart = Cart.getInstance(context);
                        Menu menu = ((Order)data.get(position)).get(0);
//                        for (Menu m : cart) {
//                            if (m.getHeadName().equals(menu.getHeadName()) &&
//                                    m.getBranchName().equals(menu.getBranchName()))
//                                cart.remove(m);
//                        }
                        for (int i = 0 ; i < cart.size(); i++) {
                            if (cart.get(i).getHeadName().equals(menu.getHeadName()) &&
                                    cart.get(i).getBranchName().equals(menu.getBranchName())) {
                                cart.remove(i);
                                i--;
                            }
                        }
                        Log.e("Cart.size", cart.size()+"");
                        //移除選定的Order
                        data.remove(position);
                        //更新Adapter的畫面
                        MenuBaseAdapter.this.notifyDataSetChanged();
                        //如果訂單列表空了就回到購物車頁面
                        if (data.size() == 0){
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("currentPage", 2);
                            context.startActivity(intent);
                        }
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

    /*private view holder class*/
    private class ViewHolder {
		ImageView imageView;
		TextView textPrice;
        TextView textName;
        TextView textMenuName;
        TextView textQuantity;
        TextView textTotal;
    }
}