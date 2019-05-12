package com.mohit.swach.extra;

import android.database.sqlite.SQLiteDatabase;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;
public class db_class {
    static SQLiteDatabase sqLiteDatabase;
    public static String DATABASE_NAME = "USERINFO.DB";


    public static void SQLiteDataBaseBuild() {

        sqLiteDatabase = openOrCreateDatabase(DATABASE_NAME, null);
    }
}
