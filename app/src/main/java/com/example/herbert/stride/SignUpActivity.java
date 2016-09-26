package com.example.herbert.stride;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

        mName = (EditText) findViewById(R.id.editTextName);
        mEmail = (EditText) findViewById(R.id.editTextEmail);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mRePassword = (EditText) findViewById(R.id.editTextPassword2);
        mRegisterButton = (Button) findViewById(R.id.registerButton);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mPassword.getText().toString().trim().equals(mRePassword.getText().toString().trim())){
                    startRegistration();
//                  Toast.makeText(SignUpActivity.this, "Password matches", Toast.LENGTH_SHORT).show();
//                  Log.d("Password", mPassword.getText().toString().trim());
//                  Log.d("Retyped Password", mRePassword.getText().toString().trim());

                }
                else{
                    Toast.makeText(SignUpActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
//                    startRegistration();
                }
            }

            private void startRegistration() {
                final String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                Log.d("Strings", name);
                Log.d("Strings", email);
                Log.d("Strings", password);

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

//                    mProgress.setMessage("Creating Account...");
//                    mProgress.show();
//                    mProgress.dismiss();

//                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()){
////                                mProgress.stop();
////                                String user_id = mAuth.getCurrentUser().getUid();
//
////                                DatabaseReference current_user_db = mDatabase.child(user_id);
//
////                                current_user_db.child("name").setValue(name);
//
//                                mProgress.dismiss();
//                            }
//                            else{
//                                mProgress.dismiss();
//                                Toast.makeText(SignUpActivity.this, "Unable to create user", Toast.LENGTH_SHORT).show();
//                                Log.d("Creating User", "Error creating user");
//                            }
//                        }
//                    });
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Log.d("User", "create user was unsuccessful");
                                    }
                                    else{
                                        Log.d("User", "Creating user was successful");
                                    }

                                    // ...
                                }
                            });
                }

            }
        });
    }
}
