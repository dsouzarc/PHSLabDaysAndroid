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
import android.widget.Toast;

public class LoginScreen extends Activity {

    private EditText passwordET;

    private void saveEmail(final String email) {
        final SharedPreferences thePrefs = getSharedPreferences("com.ryan.phslabdays", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = thePrefs.edit();
        editor.putString("email", email);
        editor.commit();
    }

    private void log(final String message) {
        Log.e("com.ryan.phslabdays", message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        this.passwordET = (EditText) findViewById(R.id.loginEditText);

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
