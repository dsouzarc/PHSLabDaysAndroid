package com.ryan.phslabdays;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.content.DialogInterface;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class SendMessageActivity extends Activity {

    private final HashMap<Integer, Person> oldPeople = new HashMap<Integer, Person>();
    private final LinkedList<Person> newPeople = new LinkedList<Person>();

    private SharedPreferences thePrefs;
    private SharedPreferences.Editor editor;
    private Context theC;
    private String email;

    private SendGrid theSendGrid;
    private boolean isFinishedUpdating = false;

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

        this.theSendGrid = new SendGrid(sendgridUsername, sendgridPassword);

        final EditText greeting = (EditText) findViewById(R.id.greetingET);
        final Spinner letterDay = (Spinner) findViewById(R.id.letterDaySpinner);
        final NumberPicker daysOver = (NumberPicker) findViewById(R.id.daysOverPicker);
        final EditText noSchool = (EditText) findViewById(R.id.noSchoolET);
        final Button sendButton = (Button) findViewById(R.id.sendButton);

        new GetPeopleOnLine().execute();

        greeting.setText(thePrefs.getString("greeting", ""));
        daysOver.setMaxValue(180);
        daysOver.setMinValue(0);
        daysOver.setValue(thePrefs.getInt("daysOver", 0));
        noSchool.setText(thePrefs.getString("noSchool", ""));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFinishedUpdating) {
                    makeToast("Please wait, still updating");
                    return;
                }

                savePeople();
                makeToast("Saved people in map to textfile: " + oldPeople.size());

                Person.message = greeting.getText().toString();
                Person.letterDay = letterDay.getSelectedItem().toString().charAt(0);
                Person.numSchoolDaysOver = daysOver.getValue();
                Person.noSchool = noSchool.getText().toString();

                editor.putString("greeting", greeting.getText().toString());
                editor.putString("letter", String.valueOf(letterDay.getSelectedItem().toString().charAt(0)));
                editor.putInt("daysOver", daysOver.getValue());
                editor.putString("noSchool", noSchool.getText().toString());
                editor.commit();

                final AlertDialog.Builder sendConfirm = new AlertDialog.Builder(SendMessageActivity.this);
                sendConfirm.setTitle("Please confirm");
                sendConfirm.setMessage("Are you sure you want to send this notification?");

                sendConfirm.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SendWelcomeMessage().execute();
                    }
                });

                sendConfirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                sendConfirm.show();

            }
        });
    }

    

    private class GetPeopleOnLine extends AsyncTask<Void, Integer, LinkedList<Person>> {
        @Override
        public LinkedList<Person> doInBackground(Void... params) {
            updateOldPeople();
            if(oldPeople.size() < 5) {
                publishProgress(1);
            }
            else {
                publishProgress(2);
            }

            final SpreadsheetService service =
                    new SpreadsheetService("MySpreadsheetIntegration-v1");
            final LinkedList<Person> onlinePeople = new LinkedList<Person>();

            try {
                final String gmailUsername = getValue(Variables.GM_USERNAME);
                final String gmailPassword = getValue(Variables.GM_PASSWORD);
                service.setUserCredentials(gmailUsername, gmailPassword);

                final URL SHEET_URL = new URL(Variables.SPREADSHEET_URL);
                final SpreadsheetEntry spreadsheet = service.getEntry(SHEET_URL, SpreadsheetEntry.class);
                final URL listFeedUrl = (spreadsheet.getWorksheets().get(0)).getListFeedUrl();

                final ListFeed feed1 = service.getFeed(listFeedUrl, ListFeed.class);
                publishProgress(0);

                for(ListEntry entry : feed1.getEntries()) {
                    final CustomElementCollection allValues = entry.getCustomElements();

                    try {
                        final String name = allValues.getValue("yourname") == null
                                ? "" : allValues.getValue("yourname");

                        final String phoneNumber =
                                formatNumber(allValues.getValue("yourphonenumberjustdigits"));

                        final String carrier = assignCarrier(allValues.getValue("yourcarrier"));
                        final boolean everyday = allValues
                                .getValue("whenwouldyouliketogettextnotifications").contains("Every");

                        final String science = allValues.getValue("science");
                        final char[] sciencelabdays =
                                getLabDays(allValues.getValue("sciencelabdays"));

                        final char[] misclabdays = getLabDays(allValues.getValue("misc.textdays"));
                        final String miscDay = allValues.getValue("misc.notificationmessage")
                                == null ? "" : allValues.getValue("misc.notificationmessage");

                        final Person person = new Person(name, phoneNumber, carrier,
                                new Science(science, sciencelabdays), new Science(miscDay,
                                misclabdays), everyday
                        );
                        onlinePeople.add(person);
                    }
                    catch (Exception e) {
                        log("HERE: " + e.toString());
                        makeToast("Problem getting someone: " + allValues.toString());
                    }
                }
            }
            catch (Exception e) {
                log(e.toString());
                makeToast("Problem getting people online");
            }
            return onlinePeople;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if(progress[0] == 0) {
                log("Got form results from online");
                makeToast("Got Form Results from online");
            }
            if(progress[0] == 1) {
                makeToast("Error reading saved people");
            }
            if(progress[0] == 2) {
                makeToast("Read from text file: " + oldPeople.size() + " people");
                log("Read from txt file");
            }
        }

        @Override
        public void onPostExecute(final LinkedList<Person> results) {
            makeToast("Got all results from online");

            for(Person result : results) {
                if(!oldPeople.containsKey(result.hashCode())) {
                    newPeople.add(result);
                    oldPeople.put(result.hashCode(), result);
                }
            }
            makeToast(newPeople.size() + " New People");
            isFinishedUpdating = true;
        }
    }

    /** Saves the people stored in hashmap to a textfile*/
    private void savePeople() {
        final JSONObject allPeople = new JSONObject();
        try {
            final JSONArray info = new JSONArray();
            final Set<Integer> peopleKey = oldPeople.keySet();
            for (Integer key : peopleKey) {
                info.put(oldPeople.get(key).getJSON());
            }
            allPeople.put("people", info);

            final OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(theC.openFileOutput(Variables.OLD_PEOPLE_TEXT_FILE,
                            theC.MODE_PRIVATE));
            outputStreamWriter.write(allPeople.toString());
            outputStreamWriter.close();
            log("Saved ");
        }
        catch (Exception e) {
            e.printStackTrace();
            log("error saving ");
        }
    }

    /** Updates global hashmap with previously stored people */
    private void updateOldPeople() {
        try {
            final InputStream inputStream = theC.openFileInput(Variables.OLD_PEOPLE_TEXT_FILE);

            if(inputStream != null) {
                final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                final StringBuilder stringBuilder = new StringBuilder();

                String receiveString = "";
                while ((receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();

                final JSONObject theObj = new JSONObject(stringBuilder.toString());
                final JSONArray peopleArray = theObj.getJSONArray("people");
                for (int i = 0; i < peopleArray.length(); i++) {
                    try {
                        final Person tP = Person.getPerson(peopleArray.getJSONObject(i));
                        oldPeople.put(tP.hashCode(), tP);
                    }
                    catch (Exception e) {
                        log("Problem from txt: " + peopleArray.getJSONObject(i).toString());
                        makeToast("Problem: " + peopleArray.getJSONObject(i).toString());
                    }
                }
            }
        }
        catch (Exception e) {
            log("Error updating from textfile");
            makeToast("Problem updating textfile");
        }
    }

    private static String formatNumber(String text) {
        text = text.replace("(", "").replace(")", "");
        text = text.replace("-", "").replace(" ", "");

        // Just in case some idiot puts a police number
        text = text.replace("911", "");
        return text;
    }

    private static char[] getLabDays(String text) {
        if(text == null) {
            return new char[]{};
        }

        final LinkedList<Character> theChars = new LinkedList<Character>();
        for (Character theChar : text.toCharArray()) {
            final int charVal = (int) theChar;
            // Between 'A' and 'G'
            if (charVal >= 65 && charVal <= 71) {
                theChars.add(theChar);
            }
        }
        final char[] answer = new char[theChars.size()];
        int counter = 0;
        for (Character c : theChars) {
            answer[counter] = c;
            counter++;
        }
        return answer;
    }

    private static String assignCarrier(final String name) {
        if (name.contains("verizon")) {
            return Variables.VERIZON;
        }
        if (name.contains("at")) {
            return Variables.ATT;
        }
        if (name.contains("t-mobile")) {
            return Variables.TMOBILE;
        }
        if (name.contains("virgin mobile")) {
            return Variables.VIRGINMOBILE;
        }
        if (name.contains("cingular")) {
            return Variables.CINGULAR;
        }
        if (name.contains("sprint")) {
            return Variables.SPRINT;
        }
        if (name.contains("Nextel")) {
            return Variables.NEXTEL;
        }
        System.out.println("ERROR: " + name);
        return Variables.VERIZON;
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
