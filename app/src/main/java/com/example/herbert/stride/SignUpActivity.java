package com.example.herbert.stride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mRePassword;

    private Button mRegisterButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        mName = (EditText) findViewById(R.id.textDuration);
        mEmail = (EditText) findViewById(R.id.editTextEmail);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mRePassword = (EditText) findViewById(R.id.editTextPassword2);
        mRegisterButton = (Button) findViewById(R.id.registerButton);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = mPassword.getText().toString().trim();
                String rePassword = mRePassword.getText().toString().trim();
                if (password.length() == rePassword.length() && (password.equals(rePassword)) && (password.length() >= 6)){
                    startRegister();
                }
                else if (!(password.equals(rePassword))) {
                    Toast.makeText(SignUpActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                }
                else if (password.length() < 6){
                    Toast.makeText(SignUpActivity.this, "Password needs to be longer than 6 Characters", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startRegister() {

        final String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            mProgress.setMessage("Signing up..");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

//                      get the user id
                        String user_id = mAuth.getCurrentUser().getUid();

//                      get a reference to the user database node
                        DatabaseReference current_user_db = mDatabase.child(user_id).child("User_Data");
//                        DatabaseReference current_user_db = mDatabase.child(user_id);

//                        DatabaseReference user_data = current_user_db.child("User_data");

                        current_user_db.child("Name").setValue(name);

                        mProgress.dismiss();

                        Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                    else{
                        mProgress.dismiss();
                        Toast.makeText(SignUpActivity.this, "Error Creating User: User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "E-mail Required", Toast.LENGTH_SHORT).show();
        }
    }
}
