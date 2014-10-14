package com.ryan.phslabdays;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

import java.io.IOException;
import java.net.*;
import java.util.*;

import android.widget.Toast;
import android.util.Log;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.github.sendgrid.SendGrid;
import java.net.URL;
import java.util.List;
import com.google.gdata.client.spreadsheet.ListQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;


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

                    final URL SPREADSHEET_FEED_URL = new URL(
                            "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

                    // Make a request to the API and get all spreadsheets.
                    final SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
                    final List<SpreadsheetEntry> spreadsheets = feed.getEntries();

                    SpreadsheetEntry theSheet = null;

                    // Iterate through all of the spreadsheets returned
                    for (SpreadsheetEntry spreadsheet : spreadsheets) {
                        // Print the title of this spreadsheet to the screen
                        log(spreadsheet.getTitle().getPlainText());

                        if(spreadsheet.getTitle().toString().contains("PHS Lab Days")) {
                            theSheet = spreadsheet;
                            log("FOUND");
                            break;
                        }
                    }

                    if(theSheet == null) {
                        theSheet = spreadsheets.get(3);
                    }

                    WorksheetFeed worksheetFeed = service.getFeed(theSheet.getWorksheetFeedUrl(), WorksheetFeed.class);
                    List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
                    WorksheetEntry worksheet = worksheets.get(0);

                    URL cellFeedUrl = worksheet.getCellFeedUrl();
                    CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);

                    // Iterate through each cell, printing its value.
                    for (CellEntry cell : cellFeed.getEntries()) {
                        // Print the cell's address in A1 notation
                        log(cell.getTitle().getPlainText() + "\t");
                        // Print the cell's address in R1C1 notation
                        log(cell.getId().substring(cell.getId().lastIndexOf('/') + 1) + "\t");
                        // Print the cell's formula or text value
                        log(cell.getCell().getInputValue() + "\t");
                        // Print the cell's calculated value if the cell's value is numeric
                        // Prints empty string if cell's value is not numeric
                        log(cell.getCell().getNumericValue() + "\t");
                        // Print the cell's displayed value (useful if the cell has a formula)
                        log(cell.getCell().getValue() + "\t");
                    }

                    String t = "https://spreadsheets.google.com/feeds/spreadsheets/1OpZPyzOHbBeDHrFaxZbD-5ASiZKM7-U-JNl7PUNXYw4";
                    URL metafeedUrl = new URL(t);
                    SpreadsheetEntry spreadsheet = service.getEntry(metafeedUrl, SpreadsheetEntry.class);
                    URL listFeedUrl = ((WorksheetEntry) spreadsheet.getWorksheets().get(0)).getListFeedUrl();

                    // Print entries
                    ListFeed feed1 = (ListFeed) service.getFeed(listFeedUrl, ListFeed.class);
                    for(ListEntry entry : feed1.getEntries())
                    {
                        log("new row");
                        for(String tag : entry.getCustomElements().getTags())
                        {
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
