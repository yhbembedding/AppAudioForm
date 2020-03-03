package com.example.audioform.Audio.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Record.db";
    public static final String TABLE_NAME = "RecordTable";
    public static final String _ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PATH = "Path";
    public static final String COLUMN_LENGTH = "Length";
    public static final String COLUMN_DATE = "Date";
    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = " CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_PATH + " TEXT, " + COLUMN_LENGTH + " INTEGER, " + COLUMN_DATE + " TEXT) ";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
