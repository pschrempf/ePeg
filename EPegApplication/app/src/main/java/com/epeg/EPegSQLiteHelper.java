package com.epeg;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by gregory on 15/03/16.
 *
 * Some ideas were borrowed from http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class EPegSQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = EPegSQLiteHelper.class.getName();

    private static final String DB_NAME = "ePeg.db";
    public static final int DB_VERSION = 2;

    // All data concerning the TRIALS table
    public static final String TABLE_NAME = "trials";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_DATA = "trial_results";
    public static final String FIELD_KEY = "symmetric_key";
    public static final String FIELD_IV = "initialisation_vector";
    public static final String FIELD_TIMESTAMP = "time_recorded";
    public static final String FIELD_DEVICE_ID = "device_identifier";
    public static final String FIELD_EXP_CONDUCTOR = "study_conductor";
    public static final String FIELD_SYNCHRONISED = "synchronised";

    //Enumerate all column names
    public static final String[] DB_COLUMNS = {
            EPegSQLiteHelper.FIELD_ID, EPegSQLiteHelper.FIELD_DATA,
            EPegSQLiteHelper.FIELD_KEY, EPegSQLiteHelper.FIELD_IV,
            EPegSQLiteHelper.FIELD_TIMESTAMP, EPegSQLiteHelper.FIELD_DEVICE_ID,
            EPegSQLiteHelper.FIELD_EXP_CONDUCTOR, EPegSQLiteHelper.FIELD_SYNCHRONISED };

    // All data concerning the CLINICS table
    public static final String C_TABLE_NAME = "clinics";

    public static final String C_FIELD_ID = "_id";
    public static final String C_FIELD_CLINIC_ID = "clinic_id";
    public static final String C_FIELD_ACTIVE = "active";

    public static final String[] C_FIELDS = {
            C_FIELD_ID, C_FIELD_CLINIC_ID, C_FIELD_ACTIVE };

    // All data concerning the RESEARCHERS table
    public static final String R_TABLE_NAME = "researchers";

    public static final String R_FIELD_ID = "_id";
    public static final String R_FIELD_RESEARCHER_NAME = "researcher_name";
    public static final String R_FIELD_ACTIVE = "active";

    public static final String[] R_FIELDS = {
            R_FIELD_ID, R_FIELD_RESEARCHER_NAME, R_FIELD_ACTIVE };

    //Create TRIALS table
    public static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            " ( " + FIELD_ID + " integer primary key autoincrement, "
            + FIELD_DATA + " text not null, " +
            FIELD_KEY + " text not null," +
            FIELD_IV + " text not null," +
            FIELD_TIMESTAMP + " default current_timestamp," +
            FIELD_DEVICE_ID + " text not null," +
            FIELD_EXP_CONDUCTOR + " text not null,"+
            FIELD_SYNCHRONISED + " tinyint default 0 )";

    //Create CLINICS table
    public static final String C_CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + C_TABLE_NAME +
            " ( " + C_FIELD_ID + " integer primary key autoincrement, "
            + C_FIELD_CLINIC_ID + " text not null, " +
            C_FIELD_ACTIVE + " tinyint default 0)";

    //Create Researchers table
    public static final String R_CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + R_TABLE_NAME +
            " ( " + R_FIELD_ID + " integer primary key autoincrement, "
            + R_FIELD_RESEARCHER_NAME + " text not null, " +
            R_FIELD_ACTIVE + " tinyint default 0 )";

    public EPegSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_QUERY);
        db.execSQL(C_CREATE_TABLE_QUERY);
        db.execSQL(R_CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database " + TABLE_NAME + " from old version " + oldVersion + " to version " + newVersion +
                ". All data will be erased.");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
