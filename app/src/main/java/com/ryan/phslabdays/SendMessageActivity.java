package com.ryan.phslabdays;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.github.sendgrid.SendGrid;

public class SendMessageActivity extends Activity {

    private SharedPreferences thePrefs;
    private SharedPreferences.Editor editor;
    private Context theC;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        theC = this;
        thePrefs = this.getSharedPreferences("com.ryan.phslabdays", Context.MODE_PRIVATE);
        editor = thePrefs.edit();
        email = thePrefs.getString("email", "");

        final EditText greeting = (EditText) findViewById(R.id.greetingET);
        final Spinner letterDay = (Spinner) findViewById(R.id.letterDaySpinner);
        final NumberPicker daysOver = (NumberPicker) findViewById(R.id.daysOverPicker);
        final EditText noSchool = (EditText) findViewById(R.id.noSchoolET);
        final Button sendButton = (Button) findViewById(R.id.sendButton);

        greeting.setText(thePrefs.getString("greeting", ""));
        daysOver.setMaxValue(180);
        daysOver.setMinValue(0);
        daysOver.setValue(thePrefs.getInt("daysOver", 0));
        noSchool.setText(thePrefs.getString("noSchool", ""));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Person.message = greeting.getText().toString();
                Person.letterDay = letterDay.getSelectedItem().toString().charAt(0);
                Person.numSchoolDaysOver = daysOver.getValue();
                Person.noSchool = noSchool.getText().toString();

                editor.putString("greeting", greeting.getText().toString());
                editor.putString("letter", String.valueOf(letterDay.getSelectedItem().toString().charAt(0)));
                editor.putInt("daysOver", daysOver.getValue());
                editor.putString("noSchool", noSchool.getText().toString());
                editor.commit();

                final SendGrid theSend = new SendGrid("dsouzarc", "Ry1996DSCS");
                theSend.addTo("6099154930@vtext.com");
                theSend.setFrom("dsouzarc@gmail.com");
                theSend.setSubject("Test");
                makeToast("Sending...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        theSend.setText("Hello");
                        log(theSend.send());
                    }
                }).start();
            }
        });
    }

    private void makeToast(final String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void log(final String message) {
        Log.e("com.ryan.phslabdays", message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sendCustomMessage:
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}
