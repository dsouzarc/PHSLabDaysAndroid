package com.ryan.phslabdays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class LoginScreen extends Activity {

    private EditText passwordET, sendgridUsername, sendgridPassword, gmailUsername, gmailPassword;
    private TextView myName;

    private SharedPreferences thePrefs;
    private SharedPreferences.Editor theEditor;

    private boolean showFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        this.thePrefs =
                getApplicationContext().getSharedPreferences("com.ryan.phslabdays", Context.MODE_PRIVATE);
        this.theEditor = this.thePrefs.edit();

        this.myName = (TextView) findViewById(com.ryan.phslabdays.R.id.writtenTV);
        this.passwordET = (EditText) findViewById(R.id.loginEditText);
        this.sendgridUsername = (EditText) findViewById(com.ryan.phslabdays.R.id.sendgridUserName);
        this.sendgridPassword = (EditText) findViewById(com.ryan.phslabdays.R.id.sendgridPassword);
        this.gmailUsername = (EditText) findViewById(com.ryan.phslabdays.R.id.gmailUsername);
        this.gmailPassword = (EditText) findViewById(com.ryan.phslabdays.R.id.gmailPassword);

        this.myName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFields = !showFields;

                if(showFields) {
                    sendgridUsername.setVisibility(View.VISIBLE);
                    sendgridPassword.setVisibility(View.VISIBLE);
                    gmailUsername.setVisibility(View.VISIBLE);
                    gmailPassword.setVisibility(View.VISIBLE);
                }

                else {
                    sendgridUsername.setVisibility(View.INVISIBLE);
                    sendgridPassword.setVisibility(View.INVISIBLE);
                    gmailUsername.setVisibility(View.INVISIBLE);
                    gmailPassword.setVisibility(View.INVISIBLE);
                }
            }
        });

        this.passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals("7399")) {
                    startActivity(new Intent(LoginScreen.this, SendMessageActivity.class));
                    finish();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("7399")) {
                    startActivity(new Intent(LoginScreen.this, SendMessageActivity.class));
                    finish();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void log(final String message) {
        Log.e("com.ryan.phslabdays", message);
    }

    private void makeToast(final String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
