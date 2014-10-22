package com.ryan.phslabdays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

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
                PERSON_NOTIFICATIONS_EVERYDAY + " BOOLEAN," +
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

    





}
