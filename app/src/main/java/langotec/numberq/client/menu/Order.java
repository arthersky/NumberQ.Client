package langotec.numberq.client.menu;

import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Order implements Serializable {

    private String headImg, orderId, userId, headId, branchId, headName, branchName, deliveryType,
            contactPhone, deliveryAddress, taxId, payWay, comment, userName, from;
    private int totalPrice, payCheck;
    private Calendar orderDT, orderGetDT;
    private ArrayList<Menu> menuList;
    private ArrayList<String> productName, quantity, sumPrice;

    public Order(){
        menuList = new ArrayList<>();
        productName = new ArrayList<>();
        quantity = new ArrayList<>();
        sumPrice = new ArrayList<>();
    }

    public Order(
            String headImg,
            String orderId,
            String userId,
            String headId,
            String branchId,
            String headName,
            String branchName,
            String deliveryType,
            String contactPhone,
            String deliveryAddress,
            String taxId,
            String payWay,
            int payCheck,
            int totalPrice,
            String comment,
            String userName,
            String orderDT,
            String orderGetDT){

        menuList = new ArrayList<>();
        productName = new ArrayList<>();
        quantity = new ArrayList<>();
        sumPrice = new ArrayList<>();
        setHeadImg(headImg);
        setOrderId(orderId);
        setUserId(userId);
        setHeadId(headId);
        setBranchId(branchId);
        setHeadName(headName);
        setBranchName(branchName);
        setDeliveryType(deliveryType);
        setContactPhone(contactPhone);
        setDeliveryAddress(deliveryAddress);
        setTaxId(taxId);
        setPayWay(payWay);
        setPayCheck(payCheck);
        setTotalPrice(totalPrice);
        setComment(comment);
        setUserName(userName);
        setOrderDT(orderDT);
        setOrderGetDT(orderGetDT);
    }

//  region 一般的getter & setter
    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHeadId() {
        return headId;
    }

    public void setHeadId(String headId) {
        this.headId = headId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int isPayCheck() {
        return payCheck;
    }

    public void setPayCheck(int payCheck) {
        this.payCheck = payCheck;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getHeadName() {
        return headName;
    }

    public void setHeadName(String headName) {
        this.headName = headName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ArrayList<Menu> getMenuList() {
        return menuList;
    }

    public void setMenuList(ArrayList<Menu> menuList) {
        this.menuList = menuList;
    }

    public ArrayList<String> getProductName() {
        return productName;
    }

    public void setProductName(ArrayList<String> productName) {
        this.productName = productName;
    }

    public ArrayList<String> getQuantity() {
        return quantity;
    }

    public void setQuantity(ArrayList<String> quantity) {
        this.quantity = quantity;
    }

    public ArrayList<String> getSumPrice() {
        return sumPrice;
    }

    public void setSumPrice(ArrayList<String> sumPrice) {
        this.sumPrice = sumPrice;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }
//  endregion

//  region有關Calendar的getter & setter
    public Calendar getOrderDT() {
        return orderDT;
    }

    public String getOrderDT(String str){
        return String.valueOf(
                orderDT.get(Calendar.YEAR) + "-" +
                        setDigit(orderDT.get(Calendar.MONTH) + 1) + "-" +
                        setDigit(orderDT.get(Calendar.DAY_OF_MONTH)) + " " +
                        setDigit(orderDT.get(Calendar.HOUR_OF_DAY)) + ":" +
                        setDigit(orderDT.get(Calendar.MINUTE)) + ":" +
                        setDigit(orderDT.get(Calendar.SECOND))
        );
    }

    public void setOrderDT(Calendar orderDT){
        this.orderDT = orderDT;
    }

    public void setOrderDT(String orderDT) {
        this.orderDT = parseStringToCalendar(orderDT);
    }

    public Calendar getOrderGetDT() {
        return orderGetDT;
    }

    public String getFinishTime(String str){
        return String.valueOf(
                orderGetDT.get(Calendar.YEAR) + "-" +
                setDigit(orderGetDT.get(Calendar.MONTH) + 1) + "-" +
                setDigit(orderGetDT.get(Calendar.DAY_OF_MONTH)) + " " +
                setDigit(orderGetDT.get(Calendar.HOUR_OF_DAY)) + ":" +
                setDigit(orderGetDT.get(Calendar.MINUTE)) + ":" +
                setDigit(orderGetDT.get(Calendar.SECOND))
        );
    }

    public void setOrderGetDT(String orderGetDT) {
        this.orderGetDT = parseStringToCalendar(orderGetDT);
    }

    private String setDigit(int str){
        String value = String.valueOf(str);
        return value.length() == 2 ? value : "0"+value;
    }

    public void setFinishTime(){
        orderGetDT = Calendar.getInstance();
        orderGetDT.setTime(orderDT.getTime());
        for(Menu m : menuList){
            orderGetDT.add(Calendar.MINUTE,
                    Integer.parseInt(m.getWaitTime()) * m.getQuantityNum());
        }
    }

    private Calendar parseStringToCalendar(String DT){
        Calendar calendar = Calendar.getInstance();
        //先分" "
        String[] split1 = DT.split(" ");
        //再分"-"
        String[] split2 = split1[0].split("-");
        //再分":"
        String[] split3 = split1[1].split(":");
        calendar.set(Calendar.YEAR, Integer.parseInt(split2[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(split2[1]));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(split2[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split3[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(split3[1]));
        calendar.set(Calendar.SECOND, Integer.parseInt(split2[2]));
        return calendar;
    }
//  endregion

    public void setImageView(ImageView imageView){
        Picasso picasso = Picasso.get();
        if (from.equals("fromCheckOutActivity")) {
            picasso.load(menuList.get(0).getHeadImageURL())
                    .resize(100, 100)
                    .centerCrop()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
        }
        else if (from.equals("fromDB")) {
            picasso.load(headImg)
                    .resize(100, 100)
                    .centerCrop()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
        }
    }
}
