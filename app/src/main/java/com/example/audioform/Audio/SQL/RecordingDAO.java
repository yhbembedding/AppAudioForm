package com.example.audioform.Audio.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.audioform.Audio.RecordingDTO;

public class RecordingDAO {
    DBHelper dbHelper;
    Context context;
    public RecordingDAO(Context context) {
        dbHelper = new DBHelper(context);
        this.context = context;
    }

    public void addRecording(String name, String path, long length, String date){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.COLUMN_NAME, name);
        contentValues.put(DBHelper.COLUMN_PATH, path);
        contentValues.put(DBHelper.COLUMN_LENGTH, length);
        contentValues.put(DBHelper.COLUMN_DATE, date);
        long  rowId = database.insert(DBHelper.TABLE_NAME, null, contentValues);
    }

    public int getCount(){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String column[] = {DBHelper._ID};
        Cursor cursor = database.query(DBHelper.TABLE_NAME, column, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public RecordingDTO getItemAt(int position){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String column[] = {DBHelper._ID, DBHelper.COLUMN_NAME, DBHelper.COLUMN_PATH, DBHelper.COLUMN_LENGTH, DBHelper.COLUMN_DATE};
        Cursor cursor = database.query(DBHelper.TABLE_NAME, column, null, null, null, null, null);
        if(cursor.moveToPosition(position)){
            RecordingDTO item = new RecordingDTO();
            item.set_id(cursor.getInt(cursor.getColumnIndex(DBHelper._ID)));
            item.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            item.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PATH)));
            item.setLength(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_LENGTH)));
            item.setDate(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE)));
            cursor.close();
            return item;
        }
        return null;
    }

    public void deleteItem(int id){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String whereClause = DBHelper._ID + "=?";
        String whereArgs[] = {String.valueOf(id)};
        int deletedRows = database.delete(DBHelper.TABLE_NAME, whereClause, whereArgs);
        database.close();
    }

    public void deleteAllItem(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_NAME, null, null);
    }

    public void renameItem(int id, String name, String path){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.COLUMN_NAME, name);
        contentValues.put(DBHelper.COLUMN_PATH, path);

        db.update(DBHelper.TABLE_NAME, contentValues, DBHelper._ID + "=?", new String[]{String.valueOf(id)});
    }
}
