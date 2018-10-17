package langotec.numberq.client.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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

public class MenuBaseAdapter extends BaseAdapter {

	private Context context;
	private ArrayList data;
	private LayoutInflater mInflater;

	public MenuBaseAdapter(Context context, ArrayList data) {
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
                    Intent intent = new Intent(context, SelectedActivity.class);
                    //把被按下的Menu物件放進intent
                    intent.putExtra("Menu", menu);
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
				holder.textStatus = (TextView) convertView.findViewById(R.id.order_status);
				holder.textFinishTime = (TextView) convertView.findViewById(R.id.order_finishTime);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
            final Order order = (Order) getItem(position);
            String str[] = setTextLoop(order);
            holder.textMenuName.setText(str[0]);
            holder.textQuantity.setText(str[1]);
            holder.textPrice.setText(str[2]);

            if (order.getFrom().equals("fromCheckOutActivity")) {
                //順便設定此筆訂單的總額
                order.setTotalPrice(Integer.parseInt(str[3]));
                //設定顯示項目
                order.setImageView(holder.imageView);
                holder.textName.setText(order.getHeadName() + " - " + order.getBranchName());
                holder.textTotal.setText(context.getString(R.string.menu_totalPrice) + str[3]);
                holder.textFinishTime.setVisibility(View.GONE);
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
                        showDialog(position);
                        return false;
                    }
                });
                CheckOutActivity.orderList = data;
            }
            else if (order.getFrom().equals("fromDB")){
                order.setImageView(holder.imageView);
                holder.textName.setText(order.getHeadName() + " - " + order.getBranchName());
                holder.textTotal.setText(String.valueOf(
                        context.getString(R.string.menu_totalPrice) + order.getTotalPrice()));
                holder.textFinishTime.setText(context.getString(R.string.order_finishTime) +
                        "\t" + order.getOrderGetDT("whatever"));
                String[] orderStatus = context.getResources().getStringArray(R.array.order_statusArray);
                String status;
                switch (order.getPayCheck()){
                    case 1:
                        status = orderStatus[1];
                        break;
                    case 2:
                        status = orderStatus[2];
                        break;
                    case 3:
                        status = orderStatus[3];
                        break;
                    case 4:
                        status = orderStatus[4];
                        break;
                    default:
                        status = "unknown";
                        break;
                }
                holder.textStatus.setText(context.getString(R.string.order_status) + "\t" + status);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("orderId", order.getOrderId());
                        intent.setClass(context, MakeQRcode.class);
                        context.startActivity(intent);
                    }
                });
            }
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
	    if (order.getFrom().equals("fromCheckOutActivity")) {
            int total = 0;
            for (int i = 0; i < order.getMenuList().size(); i++) {
                Menu menu = order.getMenuList().get(i);
                str[0] += String.valueOf(i + 1) + ": " + menu.getProductName() + "\n";
                str[1] += context.getString(R.string.menu_quantity) + menu.getQuantityNum() + "\n";
                str[2] += context.getString(R.string.menu_singlePrice) + menu.getPrice() + "\n";
                total += Integer.parseInt(menu.getPrice()) * menu.getQuantityNum();
            }
            str[3] += String.valueOf(total);
        }else if (order.getFrom().equals("fromDB")){
            for (int i = 0; i < order.getProductName().size(); i++) {
                str[0] += String.valueOf(i + 1) + ": " + order.getProductName().get(i) + "\n";
                str[1] += context.getString(R.string.menu_quantity) + order.getQuantity().get(i) + "\n";
                str[2] += context.getString(R.string.menu_singlePrice) + order.getSumPrice().get(i) + "\n";
            }
        }
	    return str;
    }

    //修改購物車內容的對話框功能
    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.cart_modify))
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(context.getResources().getString(R.string.checkOut_delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //移除Cart內被選中的店家的Menu
                        Cart cart = Cart.getInstance(context);
                        Menu menu = ((Order)data.get(position)).getMenuList().get(0);
                        for (int i = 0 ; i < cart.size(); i++) {
                            if (cart.get(i).getHeadName().equals(menu.getHeadName()) &&
                                    cart.get(i).getBranchName().equals(menu.getBranchName())) {
                                cart.remove(i);
                                i--;
                            }
                        }
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
        TextView textStatus;
        TextView textFinishTime;
    }
}