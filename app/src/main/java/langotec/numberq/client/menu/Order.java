package langotec.numberq.client.menu;

import java.util.ArrayList;
import java.util.Calendar;

public class Order extends ArrayList<Menu> {
    private String orderId;
    private boolean payCheck;
    private int totalPrice;
    private Calendar orderDT;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isPayCheck() {
        return payCheck;
    }

    public void setPayCheck(boolean payCheck) {
        this.payCheck = payCheck;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Calendar getOrderDT() {
        return orderDT;
    }

    public String getOrderDT(String str){
        return String.valueOf(
                "下訂日期" + orderDT.get(Calendar.YEAR) +
                "/" + setDigit(orderDT.get(Calendar.MONTH)) +
                "/" + setDigit(orderDT.get(Calendar.DAY_OF_MONTH)) +
                "   時間" + setDigit(orderDT.get(Calendar.HOUR_OF_DAY)) +
                ":" + setDigit(orderDT.get(Calendar.MINUTE)) +
                ":" + setDigit(orderDT.get(Calendar.SECOND)));
    }

    private String setDigit(int str){
        String value = String.valueOf(str);
        return value.length() == 2 ? value : "0"+value;
    }

    public void setOrderDT(Calendar orderDT) {
        this.orderDT = orderDT;
    }

}
