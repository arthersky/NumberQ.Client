package langotec.numberq.client;

import java.io.Serializable;

public class Store implements Serializable {
    private String HeadName;
    private int id;
    private String HeadId;
    private int BranchId;
    private String BranchName;
    private String City;
    private String Area;
    private String Address;
    private String Phone;
    private String FAX;
    private String opening;
    private int inService;
    private int waitingNumber;
    private double lat;
    private double lng;

    public Store(){

    }

    public Store(String HeadName, int id, String HeadId, int BranchId, String BranchName, String City, String Area, String Address,
                 String Phone, String FAX, String opening, int inService, int waitingNumber, double lat, double lng){
        setHeadName(HeadName);
        setHeadId(HeadId);
        setBranchId(BranchId);
        setBranchName(BranchName);
        setCity(City);
        setArea(Area);
        setAddress(Address);
        setPhone(Phone);
        setFAX(FAX);
        setOpening(opening);
        setInService(inService);
        setWaitingNumber(waitingNumber);
        setLat(lat);
        setLng(lng);
    }

    public String getHeadName() {
        return HeadName;
    }

    public void setHeadName(String headName) {
        HeadName = headName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHeadId() {
        return HeadId;
    }

    public void setHeadId(String headId) {
        HeadId = headId;
    }

    public int getBranchId() {
        return BranchId;
    }

    public void setBranchId(int branchId) {
        BranchId = branchId;
    }

    public String getBranchName() {
        return BranchName;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getFAX() {
        return FAX;
    }

    public void setFAX(String FAX) {
        this.FAX = FAX;
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




}
