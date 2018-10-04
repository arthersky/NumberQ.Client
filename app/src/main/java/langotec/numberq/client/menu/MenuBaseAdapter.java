package langotec.numberq.client.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (data.get(0) instanceof Menu) {
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
			Menu menu = (Menu) getItem(position);
			holder.textPrice.setText("NT $" + menu.getPrice());
			holder.textName.setText(menu.getProductName());
			menu.setImageView(holder.imageView);
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
            Order order = (Order) getItem(position);
			Menu menu = order.get(0);
            String str[] = setTextLoop(order);
			//設定單一訂單標題
			if (menu.getHeadName().equals("鼎泰豐"))
				holder.imageView.setImageResource(R.drawable.ding);
			else
				holder.imageView.setImageResource(R.drawable.bafun);
			holder.textName.setText(menu.getHeadName() + " - " + menu.getBranchName());
            holder.textMenuName.setText(str[0]);
            holder.textQuantity.setText(str[1]);
            holder.textPrice.setText(str[2]);
            holder.textTotal.setText(str[3]);

		}
		return convertView;
	}

	private String[] setTextLoop(Order order){
	    String str[] = new String[]{"", "", "", ""};
	    int total = 0;
//	    StringBu
//        Formatter formatter = new Formatter()
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