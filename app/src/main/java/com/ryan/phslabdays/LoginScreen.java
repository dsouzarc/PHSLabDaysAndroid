package com.ryan.phslabdays;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
public class LoginScreen extends Activity {

    private EditText passwordET, sendgridUsername, sendgridPassword, gmailUsername, gmailPassword;
    private TextView myName, save;

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

        this.myName = (TextView) findViewById(R.id.writtenTV);
        this.save = (TextView) findViewById(R.id.saveView);
        this.passwordET = (EditText) findViewById(R.id.loginEditText);
        this.sendgridUsername = (EditText) findViewById(R.id.sendgridUserName);
        this.sendgridPassword = (EditText) findViewById(R.id.sendgridPassword);
        this.gmailUsername = (EditText) findViewById(R.id.gmailUsername);
        this.gmailPassword = (EditText) findViewById(R.id.gmailPassword);

        this.myName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFields = !showFields;

                if(showFields) {
                    save.setVisibility(View.VISIBLE);
                    sendgridUsername.setVisibility(View.VISIBLE);
                    sendgridPassword.setVisibility(View.VISIBLE);
                    gmailUsername.setVisibility(View.VISIBLE);
                    gmailPassword.setVisibility(View.VISIBLE);
                }

                else {
                    save.setVisibility(View.INVISIBLE);
                    sendgridUsername.setVisibility(View.INVISIBLE);
                    sendgridPassword.setVisibility(View.INVISIBLE);
                    gmailUsername.setVisibility(View.INVISIBLE);
                    gmailPassword.setVisibility(View.INVISIBLE);
                }
            }
        });

        this.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int counter = 0;

                if(hasValue(sendgridUsername)) {
                    counter++;
                    save(Variables.SG_USERNAME, getValue(sendgridUsername));
                }
                if(hasValue(sendgridPassword)) {
                    counter++;
                    save(Variables.SG_PASSWORD, getValue(sendgridPassword));
                }
                if(hasValue(gmailUsername)) {
                    counter++;
                    save(Variables.GM_USERNAME, getValue(gmailUsername));
                }
                if(hasValue(gmailPassword)) {
                    counter++;
                    save(Variables.GM_PASSWORD, getValue(gmailPassword));
                }

                makeToast("Saved " + counter + " fields");
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

        boolean endDate = false;

        final Calendar theCalendar = Calendar.getInstance();
        int counter = 0;
        while(!endDate) {
            //Current day of week
            final int dayOfWeek = theCalendar.get(Calendar.DAY_OF_WEEK);

            //If it's not Saturday or Sunday
            if(dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {

                //6:20 AM
                theCalendar.set(Calendar.HOUR_OF_DAY, 6);
                theCalendar.set(Calendar.MINUTE, 20);

                final Intent sendMessageIntent = new Intent(LoginScreen.this, SendMessageReceiver.class);

                //PendingIntent with ID of day of year
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(LoginScreen.this,
                        theCalendar.get(Calendar.DAY_OF_YEAR), sendMessageIntent, 0);

                //Set an alarm
                final AlarmManager sendAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                sendAlarm.set(AlarmManager.RTC, theCalendar.getTimeInMillis(), pendingIntent);
            }

            if(theCalendar.get(Calendar.MONTH) == Calendar.JUNE &&
                    theCalendar.get(Calendar.DAY_OF_MONTH) == 30) {
                endDate = true;
                makeToast("Finished updating alarms: " + counter);
                log(theCalendar.toString());
                break;
            }

            //Add a day
            theCalendar.add(Calendar.DATE, 1);
            counter++;
        }
    }

    private static boolean hasValue(final EditText field) {
        return field.getText().toString().length() > 0;
    }

    private static String getValue(final EditText field) {
        return field.getText().toString();
    }

    private void save(final String field, final String value) {
        this.theEditor.putString(field, value);
        this.theEditor.commit();
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
