package com.example.lockapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventLogDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "eventlog.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_EVENT_LOG = "event_log";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIO = "fio";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_METHOD = "method";
    public static final String COLUMN_RESULT = "result";
    public static final String COLUMN_ATTEMPT = "attempt";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public EventLogDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_EVENT_LOG + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIO + " TEXT, "
                + COLUMN_POSITION + " TEXT, "
                + COLUMN_METHOD + " TEXT, "
                + COLUMN_RESULT + " TEXT, "
                + COLUMN_ATTEMPT + " INTEGER, "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_LOG);
        onCreate(db);
    }
}
