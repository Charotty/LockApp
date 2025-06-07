package com.example.lockapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.lockapp.User;
import java.util.ArrayList;
import java.util.List;

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

    // CRUD методы для пользователей
    public void insertUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIO, user.fio);
        values.put(COLUMN_POSITION, user.position);
        values.put(COLUMN_PASSWORD, user.password);
        values.put(COLUMN_RFID, user.rfid);
        values.put(COLUMN_FINGERPRINT, user.fingerprint);
        values.put(COLUMN_IV_RFID, user.ivRfid);
        values.put(COLUMN_IV_FINGERPRINT, user.ivFingerprint);
        db.insert(TABLE_USERS, null, values);
    }

    public void updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIO, user.fio);
        values.put(COLUMN_POSITION, user.position);
        values.put(COLUMN_PASSWORD, user.password);
        values.put(COLUMN_RFID, user.rfid);
        values.put(COLUMN_FINGERPRINT, user.fingerprint);
        values.put(COLUMN_IV_RFID, user.ivRfid);
        values.put(COLUMN_IV_FINGERPRINT, user.ivFingerprint);
        db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(user.id)});
    }

    public void deleteUser(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, COLUMN_ID + " ASC");
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String fio = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIO));
            String position = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            String rfid = null, fingerprint = null, ivRfid = null, ivFingerprint = null;
            int rfidIdx = cursor.getColumnIndex(COLUMN_RFID);
            int fpIdx = cursor.getColumnIndex(COLUMN_FINGERPRINT);
            int ivRfidIdx = cursor.getColumnIndex(COLUMN_IV_RFID);
            int ivFpIdx = cursor.getColumnIndex(COLUMN_IV_FINGERPRINT);
            if (rfidIdx >= 0 && !cursor.isNull(rfidIdx)) {
                byte[] rfidBytes = cursor.getBlob(rfidIdx);
                rfid = rfidBytes != null ? new String(rfidBytes) : null;
            }
            if (fpIdx >= 0 && !cursor.isNull(fpIdx)) {
                byte[] fpBytes = cursor.getBlob(fpIdx);
                fingerprint = fpBytes != null ? new String(fpBytes) : null;
            }
            if (ivRfidIdx >= 0 && !cursor.isNull(ivRfidIdx)) {
                byte[] ivRfidBytes = cursor.getBlob(ivRfidIdx);
                ivRfid = ivRfidBytes != null ? new String(ivRfidBytes) : null;
            }
            if (ivFpIdx >= 0 && !cursor.isNull(ivFpIdx)) {
                byte[] ivFpBytes = cursor.getBlob(ivFpIdx);
                ivFingerprint = ivFpBytes != null ? new String(ivFpBytes) : null;
            }
            users.add(new User(id, fio, position, password, rfid, fingerprint, ivRfid, ivFingerprint));
        }
        cursor.close();
        return users;
    }
}
