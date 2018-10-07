package langotec.numberq.client.menu;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

//實作Serializable，讓intent或bundle可以丟此物件或存檔
public class Cart extends ArrayList<Menu> implements Serializable {

    private static final String CART_NAME = "cartData";
    //一位消費者應只有一台購物車，所以使用singleton模式
    private volatile static Cart singletonCart;
    //不讓外部呼叫建構式
    private Cart(){}

    public static Cart getInstance(Context context){
        //先檢查是否已經有購物車的存檔，如果有就讀出來回傳
        if (hadCartFile(context)){
            File fileDir = new File(String.valueOf(context.getFilesDir()) +
                    "/" + CART_NAME);
            try {
                FileInputStream fIn = new FileInputStream(fileDir);
                ObjectInputStream oIn = new ObjectInputStream(fIn);
                singletonCart = new Cart();
                singletonCart = (Cart) oIn.readObject();
                fileDir.delete();
                oIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //沒有存檔紀錄才進入singleton建構模式
            //先檢查一次是否為null, 如果是才進入同步化區間
            if (singletonCart == null) {
                //鎖定Cart只能被一個Thread執行
                synchronized (Cart.class) {
                    //在同步化狀態下檢查static的變數是否為null, 目的為避免不同Thread的狀態下建立實體
                    if (singletonCart == null)
                        singletonCart = new Cart();
                }
            }
        }
        return singletonCart;
    }

    private static boolean hadCartFile(Context context){
        File fileDir = new File(String.valueOf(context.getFilesDir()));
        String[] existingFiles = fileDir.list();
        for (String file : existingFiles){
            //如果已經有此檔案就離開method不再新增檔案
            if (file.equals(CART_NAME))
                return true;
        }
        return false;
    }

    public void saveCartFile(Context context){
        if (this.size() > 0) {
            //儲存在data/data/packageName/files/cartData
            File fileDir = new File(String.valueOf(context.getFilesDir()) +
                    "/" + CART_NAME);
            try {
                FileOutputStream fOut = new FileOutputStream(fileDir);
                ObjectOutputStream oOut = new ObjectOutputStream(fOut);
                oOut.writeObject(this);
                oOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
