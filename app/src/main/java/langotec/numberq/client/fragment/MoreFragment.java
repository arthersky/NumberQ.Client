package langotec.numberq.client.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import langotec.numberq.client.R;
import langotec.numberq.client.dbConnect.CustomerDBConn;
import langotec.numberq.client.login.AccInfoActivity;
import langotec.numberq.client.login.LoginActivity;
import langotec.numberq.client.login.Member;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment {

    public Context context = null;
    private Button btnCheck;
    private TextView status;
    private View view;
    private Member member;
    private CustomerDBConn user;
    private MyHandler handler = new MyHandler();
    private ProgressDialog loading;

    public MoreFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity().getApplicationContext();
        view = inflater.inflate(R.layout.fragment_more, container, false);
        setLayout();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        member = Member.getInstance();
        Boolean ismember = member.checkLogin(context);
        if (!ismember) {
            status.setText("未登入");
            btnCheck.setText("Login");
        } else {
            status.setText(member.getEmail());
            btnCheck.setText("我的檔案");
        }

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnCheck:
                        if (btnCheck.getText() == "Login") {
                            Intent intent = new Intent(context, LoginActivity.class);
                            getActivity().startActivity(intent);
                        } else if (btnCheck.getText() == "我的檔案") {
                            loading = ProgressDialog.show(getActivity(),"載入會員資料","Loading...", false);
                            user = CustomerDBConn.getInstance();
                            user.query(handler, context.getFilesDir(), member.getEmail(), member.getPassword());
                        }
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //在此fragment拿掉search_button這個item
        menu.findItem(R.id.search_button).setVisible(false);
    }

    private void setLayout() {
        status = view.findViewById(R.id.mystatus);
        btnCheck = view.findViewById(R.id.btnCheck);
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bd = msg.getData();
            Boolean isOk = bd.getBoolean("isOk");
            Boolean isConn = bd.getBoolean("isConn");
//            Log.e("login.isOk", String.valueOf(isOk));
//            Log.e("login.isConn", String.valueOf(isConn));
            loading.dismiss();
            if (isConn && isOk) { //連線成功
                // 使用者已註冊
                member = user.getData();
                Intent intent = new Intent(context, AccInfoActivity.class);
                intent.putExtra("myfile", member);
                //Log.e("member.email",member.getEmail());
                getActivity().startActivity(intent);
            } else { // 連線失敗,未開啟網路
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("登入")
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
