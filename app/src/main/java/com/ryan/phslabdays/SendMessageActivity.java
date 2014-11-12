package com.ryan.phslabdays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.util.GregorianCalendar;
import java.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlarmManager;
import java.util.Calendar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.github.sendgrid.SendGrid;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SendMessageActivity extends Activity {

    private final HashMap<Integer, Person> oldPeople = new HashMap<Integer, Person>();
    private final LinkedList<Person> newPeople = new LinkedList<Person>();
    private final Queue<String> messages = new LinkedList<String>();

    private SharedPreferences thePrefs;
    private SharedPreferences.Editor editor;
    private Context theC;
    private String email;

    private boolean isFinishedUpdating = false;

    private String sendGridUsername, sendGridPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        theC = this;
        thePrefs = this.getSharedPreferences("com.ryan.phslabdays", Context.MODE_PRIVATE);
        editor = thePrefs.edit();
        email = thePrefs.getString("email", "");

        this.sendGridUsername = getValue(Variables.SG_USERNAME);
        this.sendGridPassword = getValue(Variables.SG_PASSWORD);

        final EditText greeting = (EditText) findViewById(R.id.greetingET);
        final Spinner letterDay = (Spinner) findViewById(R.id.letterDaySpinner);
        final NumberPicker daysOver = (NumberPicker) findViewById(R.id.daysOverPicker);
        final EditText noSchool = (EditText) findViewById(R.id.noSchoolET);
        final Button sendButton = (Button) findViewById(R.id.sendButton);

        //Update TextView with yesterday's letter day
        ((TextView) findViewById(R.id.letterDayTV)).setText("Letter Day (" +
                thePrefs.getString("letter", "") + ")");

        //People from Google SpreadSheet
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

    /** Sends welcome messages to new people */
    private class SendWelcomeMessage extends AsyncTask<Void, Integer, Void> {

        private final AlertDialog.Builder progressAlert;
        private AlertDialog theAlert;

        private Person currentPerson;

        public SendWelcomeMessage() {
            this.progressAlert = new AlertDialog.Builder(SendMessageActivity.this);
            this.progressAlert.setTitle("Sending Welcome Message: " + newPeople.size());
            this.progressAlert.setMessage("New People: " + newPeople.size());
        }

        @Override
        public Void doInBackground(Void... params) {
            publishProgress(0);
            messages.add("Sending welcome messages to " + newPeople.size() + " new people");
            while(newPeople.size() > 0) {
                this.currentPerson = newPeople.removeFirst();
                final SendGrid theSendGrid = new SendGrid(sendGridUsername, sendGridPassword);
                theSendGrid.addTo(this.currentPerson.getPhoneNumber() +
                        this.currentPerson.getCarrier());
                theSendGrid.setFrom("dsouzarc@gmail.com");
                theSendGrid.setSubject("Welcome to PHS Lab Days");

                String welcomeText = "If you have any questions, please contact " +
                        "Ryan D'souza @ dsouzarc@gmail.com or (609) 915 4930.";

                if(!this.currentPerson.shouldGetMessage()) {
                    welcomeText += Person.getLetterDay();
                }
                publishProgress(1);
                theSendGrid.setText(welcomeText);
                try {
                    final String status = "Cancel send"; //theSendGrid.send();
                    messages.add("Sent Welcome! " + this.currentPerson.getName() + " " +
                            this.currentPerson.getPhoneNumber() +
                            " " + status);
                }
                catch (Exception e) {
                    messages.add("Error sending welcome " + e.toString() +
                            " " + this.currentPerson.getPhoneNumber() + " " + this.currentPerson.getName());
                }
            }
            return null;
        }

        @Override
        public void onProgressUpdate(final Integer... param) {
            if(param[0] == 0) {
                this.theAlert = this.progressAlert.create();
                this.theAlert.show();
            }
            else {
                if(this.currentPerson == null) {
                    return;
                }
                this.theAlert.setMessage("Sending welcome to: " +
                        this.currentPerson.getName() + " " +
                        this.currentPerson.getPhoneNumber());
            }
        }

        @Override
        public void onPostExecute(Void param) {
            this.progressAlert.setMessage("Finished!");
            this.theAlert.cancel();
            makeToast("Finished sending welcomes!");
            messages.add("Finished sending welcomes");
            new SendDailyMessage().execute();
        }
    }

    /** Send the daily message to everyone */
    private class SendDailyMessage extends AsyncTask<Void, Integer, Void> {

        private final AlertDialog.Builder theAlertB;
        private final AlertDialog theAlert;

        public SendDailyMessage() {
            this.theAlertB = new AlertDialog.Builder(SendMessageActivity.this);
            this.theAlertB.setTitle("Sending Daily to: " + oldPeople.size());
            this.theAlertB.setMessage("Sending message to: " + oldPeople.size());
            this.theAlert = theAlertB.create();
        }

        @Override
        public Void doInBackground(final Void... params) {
            publishProgress(0);
            final Set<Integer> keySet = oldPeople.keySet();
            for(Integer key : keySet) {
                final Person person = oldPeople.get(key);
                if(person.shouldGetMessage()) {
                    final SendGrid theSendGrid = new SendGrid(sendGridUsername, sendGridPassword);
                    theSendGrid.addTo(person.getPhoneNumber() + person.getCarrier());
                    theSendGrid.setFrom("dsouzarc@gmail.com");
                    theSendGrid.setSubject(person.getGreeting());
                    theSendGrid.setText(person.getMessage());

                    try {
                        final String status = theSendGrid.send();
                        publishProgress(key);
                        messages.add("Daily: " + status + person.getName() + " " + person.getMessage());
                    }
                    catch (Exception e) {
                        messages.add("Daily FAIL: " + e.toString() + " " +
                                person.getName() + " " + person.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        public void onProgressUpdate(final Integer... param) {
            if(param[0] == 0) {
                theAlert.show();
            }
            else {
                final Person person = oldPeople.get(param[0]);
                theAlert.setMessage("Sending message to: " + person.getName() + " " + person.getMessage());
            }
        }

        @Override
        public void onPostExecute(final Void param) {
            theAlert.setMessage("finished sending daily");
            theAlert.cancel();
            showLogCat();
            makeToast("Finished sending daily");
            messages.add("Finished sending daily");
        }
    }

    private void showLogCat() {
        final LayoutInflater theInflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View theView = theInflater.inflate(R.layout.show_results_layout, null);
        final LinearLayout theLayout = (LinearLayout) theView.findViewById(R.id.layoutForMessages);
        setContentView(theView);

        int counter = 0;
        while(messages.size() > 0) {
            theLayout.addView(getView(messages.poll(), counter));
            counter++;
        }
    }

    private TextView getView(final String message, final int number) {
        final TextView textView = new TextView(theC);
        textView.setText(message);
        textView.setTextColor(number % 2 == 0 ? Color.BLACK : Color.BLUE);

        if(message.toLowerCase().contains("failure") || message.toLowerCase().contains("problem")) {
            textView.setTextColor(Color.RED);
        }

        textView.setPadding(16, 16, 16, 16);
        return textView;
    }

    private class GetPeopleOnLine extends AsyncTask<Void, Integer, LinkedList<Person>> {

        private int numPeopleGoogleDoc = 0;

        @Override
        public LinkedList<Person> doInBackground(Void... params) {
            //Add all previously saved people to global HashMap
            updateOldPeople();
            if(oldPeople.size() < 5) {
                publishProgress(1);
            }
            else {
                publishProgress(2);
            }

            final LinkedList<Person> onlinePeople = new LinkedList<Person>();
            final SpreadsheetService service =
                    new SpreadsheetService("MySpreadsheetIntegration-v1");

            try {
                final String gmailUsername = getValue(Variables.GM_USERNAME);
                final String gmailPassword = getValue(Variables.GM_PASSWORD);
                service.setUserCredentials(gmailUsername, gmailPassword);

                final URL SHEET_URL = new URL(Variables.SPREADSHEET_URL);
                final SpreadsheetEntry spreadsheet = service.getEntry(SHEET_URL, SpreadsheetEntry.class);
                final URL listFeedUrl = (spreadsheet.getWorksheets().get(0)).getListFeedUrl();

                final ListFeed feed1 = service.getFeed(listFeedUrl, ListFeed.class);
                publishProgress(0);
                messages.add(feed1.getEntries().size() + " people on live Google spreadsheet");

                for(ListEntry entry : feed1.getEntries()) {
                    final CustomElementCollection allValues = entry.getCustomElements();

                    try {
                        /*for(String key : allValues.getTags()) {
                            log(key + "        " + allValues.getValue(key));
                        }*/

                        final String name = allValues.getValue("yourname") == null
                                ? "" : allValues.getValue("yourname");

                        final String phoneNumber =
                                formatNumber(allValues.getValue("yourphonenumberjustdigits"));

                        final String carrier = assignCarrier(allValues.getValue("yourcarrier"));
                        final boolean everyday = allValues
                                .getValue("whenwouldyouliketogettextnotifications")
                                .contains("Every");

                        final String science = allValues.getValue("science");
                        final char[] sciencelabdays =
                                getLabDays(allValues.getValue("sciencelabdays"));

                        final char[] misclabdays = getLabDays(allValues.getValue("misc.textdays"));
                        final String miscDay = allValues.getValue("misc.notificationmessage")
                                == null ? "" : allValues.getValue("misc.notificationmessage");

                        final Person person = new Person(name, phoneNumber, carrier,
                                new Science(science, sciencelabdays), new Science(miscDay,
                                misclabdays), everyday);

                        /*if(name.contains("Rishab")) {
                            log("Rishab: " + person.toString());
                            final SendGrid theSendGrid = new SendGrid(sendGridUsername, sendGridPassword);
                            theSendGrid.addTo("6099154930@vtext.com");
                            theSendGrid.setFrom("dsouzarc@gmail.com");
                            theSendGrid.setSubject("Taran, did you get this");
                            theSendGrid.setText("Testing " + person.getPhoneNumber() + person.getCarrier());
                            try {
                                final String status = theSendGrid.send();
                                log(status + person.toString());
                            }
                            catch (Exception e) {
                                log("Errr");
                            }
                        }*/

                        onlinePeople.add(person);
                        numPeopleGoogleDoc++;
                    }
                    catch (Exception e) {
                        log("HERE: " + e.toString());
                        makeToast("Problem getting someone: " + allValues.toString());
                        messages.add("Problem getting someone from online doc: " + allValues.toString());
                    }
                }
            }
            catch (Exception e) {
                log(e.toString());
                makeToast("Problem getting people online");
                messages.add("Problem getting people online");
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
                //makeToast("Error reading saved people");
            }
            if(progress[0] == 2) {
                makeToast("Read from text file: " + oldPeople.size() + " people");
                log("Read from txt file");
            }
        }

        @Override
        public void onPostExecute(final LinkedList<Person> results) {
            makeToast("Got all results from online. #" + numPeopleGoogleDoc + " people");

            //If person isn't in database, is new person, add it to DB and list
            /*final PeopleDataBase db = new PeopleDataBase(theC);
            log("NEW BEFORE: " + oldPeople.size());
            for(Person result : results) {
                if(!oldPeople.containsKey(result.hashCode())) {
                    newPeople.add(result);
                    log("Not there: " + result.toString());
                    db.addPerson(result);
                    oldPeople.put(result.hashCode(), result);
                }
            }
            makeToast(newPeople.size() + " New People");
            messages.add("New People: " + newPeople.size());
            log("OLD PEOPLE AFTER: " + oldPeople.size());*/

            for(Person person : results) {
                oldPeople.put(person.hashCode(), person);
            }
            makeToast("Non-duplicates = " + oldPeople.size());
            isFinishedUpdating = true;
        }
    }

    /** Updates global hashmap with previously stored people */
    private void updateOldPeople() {
        final PeopleDataBase theDB = new PeopleDataBase(theC);
        //theDB.addAllPeople(oldPeople);
        //oldPeople.putAll(theDB.getAllPeople());
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

    private String assignCarrier(String name) {
        name = name.toLowerCase();
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
        log("ERROR: " + name);
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
                if(!isFinishedUpdating) {
                    makeToast("Please wait. Still updating");
                    return super.onOptionsItemSelected(item);
                }

                final LayoutInflater theInflater = (LayoutInflater)
                        getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View theView = theInflater.inflate(R.layout.send_to_all_layout, null);
                final EditText subjectET = (EditText) theView.findViewById(R.id.messageSubjectET);
                final EditText messageET = (EditText) theView.findViewById(R.id.messageTextET);

                subjectET.setBackgroundColor(Color.WHITE);
                messageET.setBackgroundColor(Color.WHITE);

                subjectET.setTextColor(Color.BLACK);
                messageET.setTextColor(Color.BLUE);

                final AlertDialog.Builder theAlert = new AlertDialog.Builder(SendMessageActivity.this);
                theAlert.setTitle("Send Message to All");
                theAlert.setView(theView);

                theAlert.setPositiveButton("Send to all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String subject = subjectET.getText().toString();
                        final String message = messageET.getText().toString();
                        final AlertDialog.Builder progress = new AlertDialog.Builder(SendMessageActivity.this);
                        progress.setTitle("Sending message to all: " + oldPeople.size());
                        progress.setMessage("Subject: " + subject + " Message: " + message);

                        final AlertDialog result = progress.create();
                        result.show();

                        final AsyncTask<Void, Integer, Void> sendInBackground =
                                new AsyncTask<Void, Integer, Void>() {
                            @Override
                            public Void doInBackground(Void... params) {
                                final Set<Integer> keySet = oldPeople.keySet();
                                int counter = 0;

                                for(Integer key : keySet) {
                                    final Person person = oldPeople.get(key);
                                    final SendGrid theSendGrid = new SendGrid(sendGridUsername,
                                            sendGridPassword);
                                    theSendGrid.addTo(person.getPhoneNumber() + person.getCarrier());
                                    theSendGrid.setFrom("dsouzarc@gmail.com");
                                    theSendGrid.setSubject(subject);
                                    theSendGrid.setText(message);

                                    try {
                                        final String status = theSendGrid.send();
                                        messages.add("Special text: " + status + person.getName() +
                                                " " + message);
                                    }
                                    catch (Exception e) {
                                        messages.add("Special Text FAIL: " + e.toString() + " " +
                                                person.getName() + " " + message);
                                    }
                                    publishProgress(counter);
                                    counter++;
                                }
                                return null;
                            }

                            @Override
                            public void onProgressUpdate(Integer... params) {
                                result.setMessage(message + params[0] + "/" + oldPeople.size());
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                showLogCat();
                                result.cancel();
                            }
                        };
                        sendInBackground.execute();
                    }
                });
                theAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                theAlert.show();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}