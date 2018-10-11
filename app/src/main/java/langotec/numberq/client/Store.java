package langotec.numberq.client;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;

public class Store implements Serializable {
    private String headName;
    private String headImg;
    private int id;
    private String headId;
    private int branchId;
    private String branchName;
    private String city;
    private String area;
    private String address;
    private String phone;
    private String fax;
    private String opening;
    private int inService;
    private int waitingNumber;
    private double lat;
    private double lng;

    public Store(){

    }

    public Store(String HeadName, String headImg, int id, String HeadId, int BranchId, String BranchName, String City, String Area, String Address,
                 String Phone, String fax, String opening, int inService, int waitingNumber, double lat, double lng){
        setHeadName(HeadName);
        setHeadImg(headImg);
        setHeadId(HeadId);
        setBranchId(BranchId);
        setBranchName(BranchName);
        setCity(City);
        setArea(Area);
        setAddress(Address);
        setPhone(Phone);
        setFax(fax);
        setOpening(opening);
        setInService(inService);
        setWaitingNumber(waitingNumber);
        setLat(lat);
        setLng(lng);
    }

    public String getHeadName() {
        return headName;
    }

    public void setHeadName(String headName) {
        this.headName = headName;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHeadId() {
        return headId;
    }

    public void setHeadId(String headId) {
        this.headId = headId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public int getInService() {
        return inService;
    }

    public void setInService(int inService) {
        this.inService = inService;
    }

    public int getWaitingNumber() {
        return waitingNumber;
    }

    public void setWaitingNumber(int waitingNumber) {
        this.waitingNumber = waitingNumber;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setImageView(ImageView imageView){
        Picasso
                .get()
                .load(headImg)
                .resize(400,400)
                .placeholder(R.drawable.placeholder)
                .into(imageView);
    }


}
