package com.mohit.swach.Handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mohit.swach.extra.db_class;
import com.mohit.swach.models.DailyVisitModel;

public class DailyVisitHandler extends SQLiteOpenHelper {

    //Field Name
    public static final String DailyVisitTableId = "DailyVisitTableId";
    public static final String DailyVisitID = "DailyVisitID";
    public static final String CentreID = "CentreID";
    public static final String Remark1 = "Remark1";
    public static final String Remark2 = "Remark2";
    public static final String Remark3 = "Remark3";
    public static final String Remark5 = "Remark5";
    public static final String Remark4 = "Remark4";
    public static final String VisitDate = "VisitDate";
    public static final String UserID = "UserID";
    public static final String Latitude = "Latitude";
    public static final String Longitude = "Longitude";
    //Table Name
    public static final String TABLE_NAME = "Daily_Visit_Handler";

    //create query
    public static final String CREATE_QUERY = "CREATE TABLE if not exists " + TABLE_NAME + "(" + DailyVisitTableId  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DailyVisitID + " TEXT, " +
            CentreID + " TEXT, " +
            Remark1 + " TEXT, " +
            Remark2 + " TEXT, " +
            Remark3 + " TEXT, " +
            Remark5 + " TEXT, " +
            Remark4 + " TEXT, " +
            VisitDate + " TEXT, " +
            UserID + " TEXT, " +
            Latitude + " TEXT, " +
            Longitude + " TEXT );";
    private static final int DATABASE_VERSION = 8;

    //Delete query
    private static final String Delete_QUERY = "DELETE FROM " + TABLE_NAME + "";

    //Constructor
    public DailyVisitHandler(Context context) {
        super(context, db_class.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_QUERY);
//        Log.e("DATABASE OPERATION", "Table create..." + CREATE_QUERY);
    }

    //To insert values
    public int addinnformation(DailyVisitModel tecm, SQLiteDatabase db) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DailyVisitID, tecm.DailyVisitID);
        contentValue.put(CentreID, tecm.CentreID);
        contentValue.put(Remark1, tecm.Remark1);
        contentValue.put(Remark2, tecm.Remark2);
        contentValue.put(Remark3, tecm.Remark3);
        contentValue.put(Remark4, tecm.Remark4);
        contentValue.put(Remark5, tecm.Remark5);
        contentValue.put(UserID, tecm.UserID);
        contentValue.put(VisitDate, tecm.VisitDate);
        contentValue.put(Latitude, tecm.Latitude);
        contentValue.put(Longitude, tecm.Longitude);
        int dailyVisitTableId = Integer.parseInt(db.insert(TABLE_NAME, null, contentValue) + "");
        return dailyVisitTableId;
    }

    public Cursor getinformation(SQLiteDatabase db) {
        Cursor cursor;
        String[] projections = {
                DailyVisitTableId,
                DailyVisitID,
                CentreID,
                Remark1,
                Remark2,
                Remark3,
                Remark4,
                Remark5,
                UserID,
                VisitDate,
                Latitude,
                Longitude
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

//    public Cursor Getorder(SQLiteDatabase db) {
//        Cursor cursor = db.rawQuery("Select * FROM " + TABLE_NAME + " WHERE " + Is_Sync + " = '" + "N" + "'", null);
//        return cursor;
//    }


    public int countExistCustomer(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("Select * FROM " + TABLE_NAME, null);
        return cursor.getCount();
    }


}
