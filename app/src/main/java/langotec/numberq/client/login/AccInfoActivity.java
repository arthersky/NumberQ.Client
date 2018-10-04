package langotec.numberq.client.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import langotec.numberq.client.R;
import langotec.numberq.client.dbConnect.CustomerDBConn;

public class AccInfoActivity extends AppCompatActivity {

    private TextView myid, myemail;
    private EditText myphone, myname;
    private Button btnlogout, btnupdate;
    private Context context;
    private Member mymb;
    private CustomerDBConn user;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_info);
        context = this;

        mymb = (Member)getIntent().getSerializableExtra("myfile");
        setLayout();

        myid.setText(mymb.getCustomerUserId());
        myemail.setText(mymb.getEmail());
        myname.setText(mymb.getUserName());
        myphone.setText(mymb.getUserPhone());

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("登出")
                        .setIcon(android.R.drawable.stat_sys_warning)
                        .setMessage("確定登出?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File file = new File(context.getFilesDir(), "customer.txt");
                                file.delete();
                                mymb.delete();
                                mymb = null;
                                dialogInterface.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();
            }
        });

        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = myid.getText().toString();
                String email = myemail.getText().toString();
                String name = myname.getText().toString();
                String phone = myphone.getText().toString();
                loading = ProgressDialog.show(context,"修改會員資料","Loading...", false);
                user = CustomerDBConn.getInstance();
                MyHandler handler1 = new MyHandler();
                user.update(handler1, getFilesDir(), uid, name, phone, email);
                //MyHandler handler2 = new MyHandler();
                //user.query(handler2, getFilesDir(), email, mymb.getPassword());
                //finish();
            }
        });
    }

    private void setLayout(){
        myid = (TextView)findViewById(R.id.myid);
        myemail = (TextView)findViewById(R.id.myemail);
        myname = (EditText)findViewById(R.id.myname);
        myphone = (EditText)findViewById(R.id.myphone);
        btnlogout = (Button)findViewById(R.id.btnlogout);
        btnupdate = (Button)findViewById(R.id.btnUpdate);

    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bd = msg.getData();
            Boolean isOk = bd.getBoolean("isOk");
            Boolean isConn = bd.getBoolean("isConn");
            Log.e("update isOk", String.valueOf(isOk));
            Log.e("update isConn", String.valueOf(isConn));
            loading.dismiss();
            if (isConn && isOk) { //連線成功 & 使用者修改成功
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("修改")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage("修改成功!")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        })
                        .create().show();
            } else { // 連線失敗,未開啟網路
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("修改")
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
}
