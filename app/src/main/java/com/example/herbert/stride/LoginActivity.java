package com.example.herbert.stride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.suitebuilder.TestMethod;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textViewRegister;

    private EditText mEmail;
    private EditText mPassword;

    private Button mLoginButton;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

        textViewRegister = (TextView) findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(this);

        mEmail = (EditText) findViewById(R.id.editTextEmail);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mLoginButton = (Button) findViewById(R.id.buttonLogin);
        mLoginButton.setOnClickListener(this);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(),"tusj.ttf");
        TextView mytitle = (TextView) findViewById(R.id.textViewTitle);
        mytitle.setTypeface(myTypeface);


    }

    @Override
    public void onClick(View view) {
        if (view == textViewRegister){
            startActivity(new Intent(this, SignUpActivity.class));
        }

        else if (view == mLoginButton){
//            Toast.makeText(this, "Ready to login", Toast.LENGTH_SHORT).show();
            checkLogin();
        }
    }

    private void checkLogin() {

        mProgress.setMessage("Logging in...");
        mProgress.show();

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        mProgress.dismiss();
                        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainActivity);
                    }else{
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "Error Login: Check e-mail and password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
