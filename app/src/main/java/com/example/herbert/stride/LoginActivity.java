package com.example.herbert.stride;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textViewRegister = (TextView) findViewById(R.id.textViewRegister);

        textViewRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == textViewRegister){
            startActivity(new Intent(this, SignUpActivity.class));
        }
    }
}
