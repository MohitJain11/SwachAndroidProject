package com.android.swach.Handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.swach.extra.db_class;
import com.android.swach.models.CenterListModel;

public class CenterListHandler extends SQLiteOpenHelper {
    //Field Name
    public static final String CentreID = "CentreID";
    public static final String PSID = "PSID";
    public static final String GPID = "GPID";
    public static final String VillageID = "VillageID";
    public static final String CentreName = "CentreName";
    public static final String DocNo = "DocNo";
    public static final String FirstTeacher = "FirstTeacher";
    public static final String FTQualification = "FTQualification";
    public static final String SecondTeacher = "SecondTeacher";
    public static final String STQualification = "STQualification";
    public static final String TypeID = "TypeID";
    public static final String MobileNo = "MobileNo";
    public static final String Latitude = "Latitude";
    public static final String Longitude = "Longitude";
    public static final String CordID = "CordID";
    //Table Name
    public static final String TABLE_NAME = "Center_List_Handler";

    //create query
    public static final String CREATE_QUERY = "CREATE TABLE if not exists " + TABLE_NAME + "(id   INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CentreID + " TEXT, " +
            PSID + " TEXT, " +
            VillageID + " TEXT, " +
            CentreName + " TEXT, " +
            DocNo + " TEXT, " +
            FirstTeacher + " TEXT, " +
            FTQualification + " TEXT, " +
            SecondTeacher + " TEXT, " +
            STQualification + " TEXT, " +
            TypeID + " TEXT, " +
            MobileNo + " TEXT, " +
            Latitude + " TEXT, " +
            Longitude + " TEXT, " +
            CordID + " TEXT, " +
            GPID + " TEXT );";
    private static final int DATABASE_VERSION = 8;

    //Delete query
    private static final String Delete_QUERY = "DELETE FROM " + TABLE_NAME + "";

    //Constructor
    public CenterListHandler(Context context) {
        super(context, db_class.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
    }

    //To insert values
    public int addinnformation(CenterListModel tecm, SQLiteDatabase db) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(CentreID, tecm.CentreID);
        contentValue.put(PSID, tecm.PSID);
        contentValue.put(GPID, tecm.GPID);
        contentValue.put(VillageID, tecm.VillageID);
        contentValue.put(CentreName, tecm.CentreName);
        contentValue.put(DocNo, tecm.DocNo);
        contentValue.put(FirstTeacher, tecm.FirstTeacher);
        contentValue.put(FTQualification, tecm.FTQualification);
        contentValue.put(SecondTeacher, tecm.SecondTeacher);
        contentValue.put(STQualification, tecm.STQualification);
        contentValue.put(TypeID, tecm.TypeID);
        contentValue.put(MobileNo, tecm.MobileNo);
        contentValue.put(Latitude, tecm.Latitude);
        contentValue.put(Longitude, tecm.Longitude);
        contentValue.put(CordID, tecm.CordID);
        int status = Integer.parseInt(db.insert(TABLE_NAME, null, contentValue) + "");
        return status;
    }

    public Cursor getinformation(SQLiteDatabase db) {
        Cursor cursor;
        String[] projections = {
                CentreID,
                PSID,
                GPID,
                VillageID,
                CentreName,
                DocNo,
                FirstTeacher,
                FTQualification,
                SecondTeacher,
                STQualification,
                TypeID,
                MobileNo,
                Latitude,
                Longitude,
                CordID,
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
