package com.example.lockapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "users.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_USERS = "users";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIO = "fio";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_RFID = "rfid";
    public static final String COLUMN_FINGERPRINT = "fingerprint";
    public static final String COLUMN_IV_RFID = "iv_rfid";
    public static final String COLUMN_IV_FINGERPRINT = "iv_fingerprint";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FIO + " VARCHAR(100), " +
                    COLUMN_POSITION + " VARCHAR(50), " +
                    COLUMN_PASSWORD + " VARCHAR(255), " +
                    COLUMN_RFID + " BLOB, " +
                    COLUMN_FINGERPRINT + " BLOB, " +
                    COLUMN_IV_RFID + " BLOB, " +
                    COLUMN_IV_FINGERPRINT + " BLOB" +
            ");";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
