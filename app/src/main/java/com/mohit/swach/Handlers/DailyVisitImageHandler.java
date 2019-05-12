package com.mohit.swach.Handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mohit.swach.extra.db_class;
import com.mohit.swach.models.DailyVisitImageModel;
import com.mohit.swach.models.DailyVisitModel;

public class DailyVisitImageHandler extends SQLiteOpenHelper {
    //Field Name
    public static final String DailyVisitTableId = "DailyVisitTableId";
    public static final String TypeID = "TypeID";
    public static final String URL_Path = "URL_Path";
    //Table Name
    public static final String TABLE_NAME = "Daily_Visit_Image_Handler";

    //create query
    public static final String CREATE_QUERY = "CREATE TABLE if not exists " + TABLE_NAME + "(id   INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TypeID + " TEXT, " +
            DailyVisitTableId + " TEXT, " +
            URL_Path + " TEXT );";
    private static final int DATABASE_VERSION = 8;

    //Delete query
    private static final String Delete_QUERY = "DELETE FROM " + TABLE_NAME + "";

    //Constructor
    public DailyVisitImageHandler(Context context) {
        super(context, db_class.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
    }

    //To insert values
    public int addinnformation(DailyVisitImageModel tecm, SQLiteDatabase db) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DailyVisitTableId, tecm.DailyVisitTableId);
        contentValue.put(TypeID, tecm.TypeID);
        contentValue.put(URL_Path, tecm.URL_Path);
        int status = Integer.parseInt(db.insert(TABLE_NAME, null, contentValue) + "");
        return status;
    }

    public Cursor getinformation(SQLiteDatabase db) {
        Cursor cursor;
        String[] projections = {
                DailyVisitTableId,
                TypeID,
                URL_Path,
        };
        cursor = db.query(TABLE_NAME, projections, null, null, null, null, null);
        return cursor;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //delete
    public void deletequery(SQLiteDatabase db) {
        db.execSQL(Delete_QUERY);
    }

    public int countExistCustomer(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("Select * FROM " + TABLE_NAME, null);
        return cursor.getCount();
    }
}
