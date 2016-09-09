package com.epeg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gregory on 03/05/16.
 */
public class SettingsManager {

    private static final String TAG = SettingsManager.class.getName();
    SQLiteDatabase database;
    SQLiteOpenHelper dbHelper;

    public SettingsManager(Context context) {
        dbHelper = new EPegSQLiteHelper(context);

        database = dbHelper.getWritableDatabase();

        //dbHelper.onUpgrade(database, 1, 2);
    }

    public void close(){
        dbHelper.close();
    }

    public List<String> getAllClinicIDs(){

        ArrayList<String> clinicIDs = new ArrayList<>();

        Cursor cursor = database.query(EPegSQLiteHelper.C_TABLE_NAME, EPegSQLiteHelper.C_FIELDS, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            clinicIDs.add(cursor.getString(1));

            cursor.moveToNext();
        }

        cursor.close();

        return clinicIDs;
    }

    public void addClinicID(String clinic_id) throws  IOException{
        Cursor cursor = database.query(EPegSQLiteHelper.C_TABLE_NAME, EPegSQLiteHelper.C_FIELDS, EPegSQLiteHelper.C_FIELD_CLINIC_ID + "='" + clinic_id + "'",null,null,null,null);

        if (cursor.getCount() > 0)
            throw new IOException("Clinic ID already exists in database!");

        ContentValues contentValues = new ContentValues();
        contentValues.put(EPegSQLiteHelper.C_FIELD_CLINIC_ID, clinic_id);
        database.insert(EPegSQLiteHelper.C_TABLE_NAME, null, contentValues);
    }

    public void removeClinicID(String clinic_id){
        database.delete(EPegSQLiteHelper.C_TABLE_NAME, EPegSQLiteHelper.C_FIELD_CLINIC_ID + " = '" + clinic_id +"'", null);
    }

    public void setActiveClinic(String clinic_id){

        ContentValues nullifiyer = new ContentValues();
        nullifiyer.put(EPegSQLiteHelper.C_FIELD_ACTIVE, 0);

        ContentValues contentValues = new ContentValues();
        contentValues.put(EPegSQLiteHelper.C_FIELD_ACTIVE, 1);

        // Nullify all rows
        database.update(EPegSQLiteHelper.C_TABLE_NAME, nullifiyer, EPegSQLiteHelper.C_FIELD_ACTIVE + " = 1", null);

        // Set the one row active
        int affected = database.update(EPegSQLiteHelper.C_TABLE_NAME, contentValues, EPegSQLiteHelper.C_FIELD_CLINIC_ID + " = '" + clinic_id + "'", null);

        if (affected == 0)
            Log.w(TAG, "No new clinic was activated!");
    }

    public String getActiveClinic(){
        String active = null;
        Cursor cursor = database.query(EPegSQLiteHelper.C_TABLE_NAME, EPegSQLiteHelper.C_FIELDS, EPegSQLiteHelper.C_FIELD_ACTIVE + "=1", null,null, null,null);

        cursor.moveToFirst();
        if (cursor.getCount() != 0)
        {
            active = cursor.getString(1);
        }

        cursor.close();

        return active;
    }

    public void addResearcher(String researcher) throws  IOException{
        Cursor cursor = database.query(EPegSQLiteHelper.R_TABLE_NAME, EPegSQLiteHelper.R_FIELDS, EPegSQLiteHelper.R_FIELD_RESEARCHER_NAME + "='" + researcher+"'", null,null,null,null);

        if (cursor.getCount() > 0)
            throw new IOException("Researcher already exists in database!");

        ContentValues contentValues = new ContentValues();
        contentValues.put(EPegSQLiteHelper.R_FIELD_RESEARCHER_NAME, researcher);
        database.insert(EPegSQLiteHelper.R_TABLE_NAME, null, contentValues);
    }

    public void removeResearcher(String researcher){
        database.delete(EPegSQLiteHelper.R_FIELD_RESEARCHER_NAME, EPegSQLiteHelper.R_FIELD_RESEARCHER_NAME + " = '" + researcher + "'", null);
    }

    public List<String> getAllResearchers(){

        ArrayList<String> researchers = new ArrayList<>();

        Cursor cursor = database.query(EPegSQLiteHelper.R_TABLE_NAME, EPegSQLiteHelper.R_FIELDS, null, null, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            researchers.add(cursor.getString(1));

            cursor.moveToNext();
        }

        cursor.close();

        return researchers;
    }

    public String getActiveResearcher(){
        String active = null;
        Cursor cursor = database.query(EPegSQLiteHelper.R_TABLE_NAME, EPegSQLiteHelper.R_FIELDS, EPegSQLiteHelper.R_FIELD_ACTIVE + "=1", null,null, null,null);

        cursor.moveToFirst();
        if (cursor.getCount() != 0)
        {
            active = cursor.getString(1);
        }

        cursor.close();

        return active;
    }

    public void setActiveResearcher(String researcherName){

        ContentValues nullifiyer = new ContentValues();
        nullifiyer.put(EPegSQLiteHelper.R_FIELD_ACTIVE, 0);

        ContentValues contentValues = new ContentValues();
        contentValues.put(EPegSQLiteHelper.R_FIELD_ACTIVE, 1);

        // Nullify all rows
        database.update(EPegSQLiteHelper.R_TABLE_NAME, nullifiyer, EPegSQLiteHelper.R_FIELD_ACTIVE + " = 1", null);

        // Set the one row active
        int affected = database.update(EPegSQLiteHelper.R_TABLE_NAME, contentValues, EPegSQLiteHelper.R_FIELD_RESEARCHER_NAME+ " = '" + researcherName + "'", null);

        if (affected < 0)
            Log.w(TAG, "No new researcher was activated!");
    }

}
