package langotec.numberq.client.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.dbConnect.CustomerDBConn;

public class AccRegiActivity extends AppCompatActivity {

    EditText etEmail,etNickName,etPass,etPass1,etPhone,pw1,pw2;
    Button btnSubmit;

    private MyHandler handler;
    private Context context;
    private CustomerDBConn user;
    private String email, pwd;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_regi);
        context = this;
        setLayout();
    }

    private void setLayout(){
        etEmail=findViewById(R.id.etEmail);
        etNickName=findViewById(R.id.etNickName);
        pw1=findViewById(R.id.etPass);
        pw2=findViewById(R.id.etPass1);
        etPhone=findViewById(R.id.etPhone);
        btnSubmit=findViewById(R.id.btnSubmit);
    }

    public void createNewAccount(View v){
        final String email = etEmail.getText().toString();
        final String nickname = etNickName.getText().toString();
        final String password = pw1.getText().toString();
        String password2 = pw2.getText().toString();
        String phone = etPhone.getText().toString();
        Boolean datacheck = true;
        if(!isVaildEmailFormat(email)){
            Toast.makeText(this,"請輸入正確的email帳號",Toast.LENGTH_LONG).show();
            datacheck = false;
        } else if(nickname.isEmpty()){
            Toast.makeText(this,"請輸入暱稱",Toast.LENGTH_LONG).show();
            datacheck = false;
        } else if(password.isEmpty()){
            Toast.makeText(this,"請輸入密碼8-10碼",Toast.LENGTH_LONG).show();
            datacheck = false;
        } else if(password.length() <8 || password.length()>10) {
            Toast.makeText(this, "密碼長度最小8,最大10", Toast.LENGTH_LONG).show();
            datacheck = false;
        } else if(password2.isEmpty()) {
            Toast.makeText(this, "請再輸入同密碼", Toast.LENGTH_LONG).show();
            datacheck = false;
        } else if(!(password.equals(password2))){
            Toast.makeText(this,"密碼不一致請重新輸入",Toast.LENGTH_LONG).show();
            datacheck = false;
        } else if(!isVaildPhoneFormat(phone)){
            Toast.makeText(this,"請輸入正確手機號碼",Toast.LENGTH_LONG).show();
            datacheck = false;
            //}else{
            //    datacheck = true;
        }

        if(datacheck){
            loading = ProgressDialog.show(context,"註冊會員","Loading...", false);
            handler = new MyHandler();
            user = CustomerDBConn.getInstance();
            user.insert(handler, getFilesDir(), nickname,phone, email, password);
        }
    }

    // 判斷是否為 E-mail 格式
    private boolean isVaildEmailFormat(String email)
    {
        if (email == null)
            return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // 判斷是否為 Mobile Phone Number 格式
    private boolean isVaildPhoneFormat(String phone) {
        if(phone.isEmpty() || phone == null ){
            return false;
        }
        return Patterns.PHONE.matcher(phone).matches();
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bd = msg.getData();
            Boolean isOk = bd.getBoolean("isOk");
            Boolean isConn = bd.getBoolean("isConn");
            Log.e("register isOk", String.valueOf(isOk));
            Log.e("register isConn", String.valueOf(isConn));
            loading.dismiss();
            if (isConn) { //連線成功
                if (isOk) { // 使用者註冊成功
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("註冊")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage("註冊成功!請重新登入")
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .create().show();
                } else {  // 使用者註冊失敗
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("註冊")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage("註冊失敗!\n請確認帳號未註冊。")
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();
                }
            } else { // 連線失敗,未開啟網路
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("註冊")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage("網路未連線!\n請確認網路您的連線狀態。")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        }
    }
}
