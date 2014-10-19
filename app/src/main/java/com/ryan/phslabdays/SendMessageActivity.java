package com.ryan.phslabdays;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.sendgrid.SendGrid;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

import java.net.URL;

public class SendMessageActivity extends Activity {

    private SharedPreferences thePrefs;
    private SharedPreferences.Editor editor;
    private Context theC;
    private String email;

    private SendGrid theSendGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        theC = this;
        thePrefs = this.getSharedPreferences("com.ryan.phslabdays", Context.MODE_PRIVATE);
        editor = thePrefs.edit();
        email = thePrefs.getString("email", "");

        final String sendgridUsername = getValue(Variables.SG_USERNAME);
        final String sendgridPassword = getValue(Variables.SG_PASSWORD);
        final String gmailUsername = getValue(Variables.GM_USERNAME);
        final String gmailPassword = getValue(Variables.GM_PASSWORD);

        this.theSendGrid = new SendGrid(sendgridUsername, sendgridPassword);

        final SpreadsheetService service =
                new SpreadsheetService("MySpreadsheetIntegration-v1");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log("Here");
                    service.setUserCredentials(gmailUsername, gmailPassword);

                    final URL SHEET_URL = new URL(Variables.SPREADSHEET_URL);
                    final SpreadsheetEntry spreadsheet = service.getEntry(SHEET_URL, SpreadsheetEntry.class);
                    final URL listFeedUrl = (spreadsheet.getWorksheets().get(0)).getListFeedUrl();

                    final ListFeed feed1 = service.getFeed(listFeedUrl, ListFeed.class);
                    for(ListEntry entry : feed1.getEntries()) {
                        final CustomElementCollection allValues = entry.getCustomElements();

                        final String name = allValues.getValue("yourname");



                        log("new row");
                        for(String tag : entry.getCustomElements().getTags()) {
                            log("     "+tag + ": " + entry.getCustomElements().getValue(tag));
                        }
                    }
                }
                catch (Exception e) {
                    log(e.toString());
                }
            }
        }).start();

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
            }
        });
    }

    private String getValue(final String tag) {
        return this.thePrefs.getString(tag, "");
    }
    private void makeToast(final String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void log(final String message) {
        Log.e("com.ryan.phslabdays", message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
