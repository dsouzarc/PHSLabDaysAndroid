package com.ryan.phslabdays;

import android.util.Log;

/** To hold common variables */

public class Variables {

	// CARRIERS
	public static final String VERIZON = "@vtext.com";
	public static final String ATT = "@txt.att.net";
	public static final String TMOBILE = "@tmomail.net";
	public static final String VIRGINMOBILE = "@vmobl.com";
	public static final String CINGULAR = "@cingularme.com";
	public static final String SPRINT = "@messaging.sprintpcs.com";
	public static final String NEXTEL = "@messaging.nextel.com";

    public static final String OLD_PEOPLE_TEXT_FILE = "oldpeople.txt";

    public static final String SG_USERNAME = "sendgrid_username";
    public static final String SG_PASSWORD = "sendgrid_password";
    public static final String GM_USERNAME = "gmail_username";
    public static final String GM_PASSWORD = "gmail_password";

    public static final String SPREADSHEET_URL =
            "https://spreadsheets.google.com/feeds/spreadsheets/1OpZPyzOHbBeDHrFaxZbD-5ASiZKM7-U-JNl7PUNXYw4";

    public static void log(final String message) {
        Log.e("com.ryan.phslabdays", message);
    }
}
