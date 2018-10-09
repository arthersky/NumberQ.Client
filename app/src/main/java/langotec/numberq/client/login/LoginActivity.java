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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import langotec.numberq.client.MainActivity;
import langotec.numberq.client.R;
import langotec.numberq.client.dbConnect.CustomerDBConn;
import langotec.numberq.client.menu.CheckOutActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPass;

    private Button btnPassforget;
    private Button btnReg;
    private Button btnLogin;

    private MyHandler handler;
    private Context context;
    private CustomerDBConn user;
    private String email, pwd, startFrom;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        setLayout();

        //如果是從CartFragment來的就必須跳轉回去
        startFrom = getIntent().getStringExtra("startFrom");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = etEmail.getText().toString();
                pwd = etPass.getText().toString();
                if(!isVaildEmailFormat(email)){
                    Toast.makeText(context,"請輸入正確的e-mail", Toast.LENGTH_LONG).show();
                }else{
                    loading = ProgressDialog.show(context, "登入中","Loading...", false);
                    user = CustomerDBConn.getInstance();
                    handler = new MyHandler();
                    user.query(handler, getFilesDir(), email, pwd);
                }

            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, AccRegiActivity.class);
                startActivity(intent);

            }

        });

        btnPassforget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SendPassActivity.class);
                startActivity(intent);
                //boolean isok = member.query(etEmail,etPass);

            }
        });
    }

    private void setLayout(){
        etEmail=findViewById(R.id.etEmail);
        etPass=findViewById(R.id.etPass);
        btnPassforget=findViewById(R.id.btnPassforget);
        btnReg=findViewById(R.id.btnReg);
        btnLogin=findViewById(R.id.btnLogin);
    }

    private boolean isVaildEmailFormat(String email)
    {
        if (email == null)
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bd = msg.getData();
            Boolean isOk = bd.getBoolean("isOk");
            Boolean isConn = bd.getBoolean("isConn");
            Log.e("login.isOk", String.valueOf(isOk));
            Log.e("login.isConn", String.valueOf(isConn));
            loading.dismiss();
            if (isConn) { //連線成功
                if (isOk) { // 使用者已註冊
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("登入")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage("登入成功!")
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    loginSuccessGoTo();
                                }
                            })
                            .create().show();

                } else {  // 使用者未註冊
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("登入")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage("登入失敗!\n請確認帳號或密碼是否正確。")
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
                builder.setTitle("登入")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage("網路未連線!\n請確認您的網路連線狀態。")
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

    private void loginSuccessGoTo(){
        Intent intent = new Intent();
        if (startFrom != null && startFrom.equals("fromCartFragment")) {
            intent.setClass(context, CheckOutActivity.class);
        }
        else{
            intent.setClass(context, MainActivity.class);
            intent.putExtra("currentPage", 3);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
