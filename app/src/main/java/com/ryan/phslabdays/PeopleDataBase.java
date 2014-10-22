package com.ryan.phslabdays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PeopleDataBase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PHSLabDays.db";
    private static final String TABLE_NAME = "participants";

    private static final String PERSON_ID = "id";
    private static final String PERSON_NAME = "name";
    private static final String PERSON_PHONE = "phone";
    private static final String PERSON_CARRIER = "carrier";
    private static final String PERSON_NOTIFICATIONS_EVERYDAY = "notifications";
    private static final String PERSON_SCIENCE = "science";
    private static final String PERSON_SCIENCE_DAYS = "sciencedays";
    private static final String PERSON_MISC = "misc";
    private static final String PERSON_MISC_DAYS = "miscdays";

    public PeopleDataBase(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE = "CREATE TABLE " + TABLE_NAME +
                "(" + PERSON_ID + " INTEGER PRIMARY KEY," +
                PERSON_NAME + " TEXT," +
                PERSON_PHONE + " TEXT," +
                PERSON_CARRIER + " TEXT," +
                PERSON_NOTIFICATIONS_EVERYDAY + " TEXT," +
                PERSON_SCIENCE + " TEXT," +
                PERSON_SCIENCE_DAYS + " TEXT," +
                PERSON_MISC + " TEXT," +
                PERSON_MISC_DAYS + " TEXT)";
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addPerson(final Person person) {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.insert(TABLE_NAME, null, personToContentValues(person));
    }

    private ContentValues personToContentValues(final Person person) {
        final ContentValues values = new ContentValues();

        values.put(PERSON_NAME, person.getName());
        values.put(PERSON_PHONE, person.getPhoneNumber());
        values.put(PERSON_CARRIER, person.getCarrier());
        values.put(PERSON_NOTIFICATIONS_EVERYDAY, person.isEveryday());

        char[] days = person.getScience().getLabDays();
        String daysString = "";

        for(char day : days) {
            daysString += day + "<";
        }
        values.put(PERSON_SCIENCE, person.getScience().getScienceName());
        values.put(PERSON_SCIENCE_DAYS, daysString);

        days = person.getMisc().getLabDays();
        daysString = "";

        for(char day : days) {
            daysString += day + "<";
        }
        values.put(PERSON_MISC, person.getMisc().getScienceName());
        values.put(PERSON_MISC_DAYS, daysString);

        return values;
    }

    public HashMap<Integer, Person> getAllPeople() {
        final HashMap<Integer, Person> allPeople = new HashMap<Integer, Person>();
        final String query = "SELECT * FROM " + TABLE_NAME;
        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                final String name = cursor.getString(0);
                final String phoneNumber = cursor.getString(1);
                final String carrier = cursor.getString(2);
                final boolean everydayNotifications = cursor.getString(3).contains("true");
                final String science = cursor.getString(4);
                final char[] scienceDays = toCharArray(cursor.getString(5).split(","));
                final String misc = cursor.getString(6);
                final char[] miscDays = toCharArray(cursor.getString(7).split(","));

                final Person person = new Person(name, phoneNumber, carrier,
                        new Science(science, scienceDays), new Science(misc, miscDays),
                        everydayNotifications);

                allPeople.put(person.hashCode(), person);
            } while(cursor.moveToNext());
        }

        return allPeople;
    }

    public List<Person> getAllPeopleList() {
        final List<Person> allPeople = new ArrayList<Person>();

        final String query = "SELECT * FROM " + TABLE_NAME;
        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                final String name = cursor.getString(0);
                final String phoneNumber = cursor.getString(1);
                final String carrier = cursor.getString(2);
                final boolean everydayNotifications = cursor.getString(3).contains("true");
                final String science = cursor.getString(4);
                final char[] scienceDays = toCharArray(cursor.getString(5).split(","));
                final String misc = cursor.getString(6);
                final char[] miscDays = toCharArray(cursor.getString(7).split(","));

                allPeople.add(new Person(name, phoneNumber, carrier,
                        new Science(science, scienceDays), new Science(misc, miscDays),
                        everydayNotifications));
            } while(cursor.moveToNext());
        }

        return allPeople;
    }

    public void updatePerson(final String oldPersonPhoneNumber, final Person newPerson) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues values = personToContentValues(newPerson);

        db.update(TABLE_NAME, values, PERSON_PHONE + " = ?",
                new String[]{oldPersonPhoneNumber});
    }

    public void deletePerson(final String phoneNumber) {
        final SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, PERSON_PHONE + " = ?",
                new String[] {phoneNumber});
        db.close();
    }

    private static char[] toCharArray(final String[] values) {
        final char[] chars = new char[values.length];

        for(int i = 0; i < values.length; i++) {
            if(values[i].length() == 0) {
                chars[i] = 'Z';
            }
            else {
                chars[i] = values[i].charAt(0);
            }
        }
        return chars;
    }
}
