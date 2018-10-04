package langotec.numberq.client.login;

import android.content.Context;

import java.io.Serializable;

import langotec.numberq.client.dbConnect.ReadFile;

public class Member implements Serializable {
    private static Member member;
    private int id;
    private String customerUserId;
    private String userName;
    private String userPhone;
    private String email;
    private String password;
    private String google_email;
    private String line_email;
    private String FB_email;

    private Member(){   }

    public static Member getInstance(){
        if(member == null){
            synchronized(Member.class){
                if(member == null){
                    member = new Member();
                }
            }
        }
        return member;
    }

    public void add(int id, String customerUserId, String userName, String userPhone, String email, String password, String google_email, String line_email, String FB_email){
        setId(id);
        setCustomerUserId(customerUserId);
        setUserName(userName);
        setUserPhone(userPhone);
        setEmail(email);
        setPassword(password);
        setGoogle_email(google_email);
        setLine_email(line_email);
        setFB_email(FB_email);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerUserId() {
        return customerUserId;
    }

    public void setCustomerUserId(String customerUserId) {
        this.customerUserId = customerUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGoogle_email() {
        return google_email;
    }

    public void setGoogle_email(String google_email) {
        this.google_email = google_email;
    }

    public String getLine_email() {
        return line_email;
    }

    public void setLine_email(String line_email) {
        this.line_email = line_email;
    }

    public String getFB_email() {
        return FB_email;
    }

    public void setFB_email(String FB_email) {
        this.FB_email = FB_email;
    }

    public Boolean checkLogin(Context context){
        return new ReadFile(context.getFilesDir(), "customer.txt", member).read();
    }

    public void delete(){
        member = null;
    }
}
