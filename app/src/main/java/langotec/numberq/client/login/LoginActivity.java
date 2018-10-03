package langotec.numberq.client.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import langotec.numberq.client.R;

public class LoginActivity extends AppCompatActivity {

    /*protected EditText etEmail;
    protected EditText etPass;


    protected Button btnPassforget;
    protected Button btnReg;
    protected Button btnLogin;
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etEmail=findViewById(R.id.etEmail);
        final EditText etPass=findViewById(R.id.etPass);
     /*   EditText etPass1=findViewById(R.id.etPass1);
        EditText etPhone=findViewById(R.id.etPhone);*/
        Button btnPassforget=findViewById(R.id.btnPassforget);
        Button btnReg=findViewById(R.id.btnReg);
        Button btnLogin=findViewById(R.id.btnLogin);
        CustomerDBConn member = new CustomerDBConn(this);
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
}
