package langotec.numberq.client.menu;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Order extends ArrayList<Menu> implements Serializable{
    private String orderId;
    private boolean payCheck;
    private int totalPrice;
    private Calendar orderDT;
    private Calendar finishTime;

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
                "\n下訂日期:" + orderDT.getTime().toString() +
                "\n訂單完成時間:" + finishTime.getTime().toString());
    }

    public void setOrderDT(Calendar orderDT) {
        this.orderDT = orderDT;
    }

    public Calendar getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Calendar finishTime) {
        this.finishTime = finishTime;
    }

    private String setDigit(int str){
        String value = String.valueOf(str);
        return value.length() == 2 ? value : "0"+value;
    }

    public void setFinishTime(){
        finishTime = Calendar.getInstance();
        finishTime.setTime(orderDT.getTime());
        for(Menu m : this){
            finishTime.add(Calendar.MINUTE,
                    Integer.parseInt(m.getWaitTime()) * m.getQuantityNum());
        }
    }
}
