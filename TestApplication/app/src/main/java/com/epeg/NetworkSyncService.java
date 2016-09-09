package com.epeg;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.Process;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;

/**
 * Checks internet connectivity, and if found, synchronise the internal SQLite DB with the remote Server
 */
public class NetworkSyncService extends Service {

    private static final String REMOTE_ADDR = "https://gf38.host.cs.st-andrews.ac.uk/epeg";

    private static final String JSON_PAYLOAD_DATA_TAG = "data";
    private static final String JSON_PAYLOAD_IV_TAG = "iv";
    private static final String JSON_PAYLOAD_SECRETKEY_TAG = "secretkey";
    private static final String JSON_PAYLOAD_TIMESTAMP_TAG = "timestamp";
    private static final String JSON_PAYLOAD_DEVIDE_ID_TAG = "device_id";
    private static final String JSON_PAYLOAD_RESEARCHER_ID_TAG = "researcher";

    //This one is there temporarily to pass the id of the entry so it can be updated locally to synced status
    private static final String JSON_PAYLOAD_ID_TEMP_TAG = "temp_id";

    private static final String JSON_SERVER_RESPONSE_CODE_TAG = "code";
    // Tag for logging
    public static final String TAG = NetworkSyncService.class.getName();
    private static final int NETWORK_CONN_RETRY_SECONDS = 30;
    private static final int NETWORK_REQ_FAIL_RETRY_SECONDS = 15;

    private static final int NETWORK_REQ_SUCCESS_SLEEP_MILLIS = 200;



    HttpURLConnection connection;

    // Handler for the thread function
    private NetworkSyncServiceHandler serviceHandler;
    private Looper serviceLooper;

    /**
     * Run basic setup, such as starting the sync thread
     */
    @Override
    public void onCreate() {

        /*
        * Start separate thread so that all the network comms do not block the main thread
        * of the application.
        * Also set priority to the process background.
        */
        HandlerThread serviceThread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND );

        // Start the server
        serviceThread.start();

        // Initialise the service Handler
        serviceLooper = serviceThread.getLooper();
        serviceHandler = new NetworkSyncServiceHandler(serviceLooper);
    }

    /*
     * In charge of handling all intent based starts.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // This bit there so that if we receive more than one start request, we only shut down on the last one.
        Message startMsg = serviceHandler.obtainMessage();
        startMsg.arg1 = startId;
        serviceHandler.sendMessage(startMsg);

        // If we get shut down due to resource issues, no need to restart this service.
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        //There is no binding allowed.
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
    }

    /*
     * Inner Handler for the actual business
     */
    private final class NetworkSyncServiceHandler extends Handler{
        public final String TAG = NetworkSyncServiceHandler.class.getName();

        private ArrayDeque<JSONObject> unsyncedEntries;

        public NetworkSyncServiceHandler(Looper looper){
            super(looper);

            unsyncedEntries = new ArrayDeque<>();
        }

        /*
         * Handler Thread Function
         *
         * Checks connectivity -> Tries to sync -> after syncing shut down
         */
        @Override
        public void handleMessage(Message msg) {
            try {

                /*
                 * Check if there are any unsynced entries in the database.
                 * If there are they will be placed in the unsyncedEntries queue.
                 * If there are none, the service will destroy itself.
                 */
                while ( checkNeedsSync() ){


                    // Variables required to check internet connectivity
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                    DataOutputStream outputStream = null;

                    // Check if we have network connection
                    if (networkInfo != null && networkInfo.isConnected()) {
                        Log.i(TAG, "Connected to network, attempting to sync!");

                        try {
                            // Try syncing all entries one by one
                            while ( !unsyncedEntries.isEmpty() ) {
                                try {
                                    // Get next entry to sync
                                    JSONObject nextEntry = unsyncedEntries.pop();

                                    //Get local ID of the entry
                                    long entryID = nextEntry.getLong(JSON_PAYLOAD_ID_TEMP_TAG);

                                    //Remove the temp ID field from the JSON onject
                                    nextEntry.remove(JSON_PAYLOAD_ID_TEMP_TAG);

                                    // Sets up our connection so that we are ready to call connect()
                                    setUpConnection(getResources().getString(R.string.server_address));

                                    //Connect to the remote server
                                    connection.connect();

                                    // Retrieve the output stream. We write our message here, and when we flush, it gets sent.
                                    // This needs to be reopened every time we flush
                                    outputStream = new DataOutputStream(connection.getOutputStream());

                                    // Write to stream and flush it, so that it is sent.
                                    outputStream.writeBytes(nextEntry.toString());
                                    outputStream.flush();

                                    //Get the server response
                                    int response = connection.getResponseCode();
                                    int len = connection.getContentLength();
                                    Log.i(TAG, "Server response: " + response + " Message: " + connection.getResponseMessage() + " Content length: " + len);

                                    // If we got a 200 response, update DB,
                                    // Otherwise place back in queue and retry
                                    if ( response == 200 && getResponseBodyStatusCode(connection.getInputStream(), connection.getContentLength()) == 200 )
                                    {
                                        setEntrySynced(entryID);

                                        Thread.sleep(NETWORK_REQ_SUCCESS_SLEEP_MILLIS);
                                    }
                                    else {
                                        // Put temp id field back, and push back on top of the queue
                                        nextEntry.put( JSON_PAYLOAD_ID_TEMP_TAG, entryID );
                                        unsyncedEntries.push(nextEntry);

                                        Log.i(TAG, "Request failed, retrying in " + NETWORK_REQ_FAIL_RETRY_SECONDS + " seconds.");
                                        Thread.sleep(NETWORK_REQ_FAIL_RETRY_SECONDS * 1000);
                                    }
                                }
                                catch (JSONException | InterruptedException e) {

                                    // Should never occur
                                    e.printStackTrace();
                                }
                                catch (IOException e) {
                                    //This is if we receive a connection error
                                    e.printStackTrace();
                                } finally {
                                    if(outputStream != null)
                                        outputStream.close();
                                    connection.disconnect();
                                }
                            }

                        }catch (MalformedURLException e) {
                            throw e;
                        }

                    } else {
                        Log.i(TAG, "Network unavailable at this time, cannot sync. Waiting " + NETWORK_CONN_RETRY_SECONDS + " seconds." );

                        try {
                            Thread.sleep(NETWORK_CONN_RETRY_SECONDS * 1000);
                        } catch (InterruptedException e) {
                            //If we get an interrup, restore interrupt status
                            Thread.currentThread().interrupt();
                        }
                    }
                }

            }
            catch (IOException e) {
                //This should never occur
                e.printStackTrace();
            }
            finally {

                Log.i(TAG, "All database entries synchronised, killing service.");

                stopSelf(msg.arg1);

            }
        }

        private int getResponseBodyStatusCode(InputStream inputStream, int contentLength) throws IOException, JSONException {

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            char[] buffer = new char[contentLength];

            inputStreamReader.read(buffer);

            String responseBody = new String(buffer);

            JSONObject responseJson = new JSONObject(responseBody);

            return responseJson.getInt(JSON_SERVER_RESPONSE_CODE_TAG);
        }

        private void setEntrySynced(long entryID) {
            // Get DB connection through our SQL helper
            EPegSQLiteHelper dbConn = new EPegSQLiteHelper(NetworkSyncService.this);

            // Select DB
            SQLiteDatabase db = dbConn.getReadableDatabase();

            ContentValues values = new ContentValues();

            values.put( EPegSQLiteHelper.FIELD_SYNCHRONISED, 1);

            db.update(EPegSQLiteHelper.TABLE_NAME, values, EPegSQLiteHelper.FIELD_ID + " = " + entryID, null);

            Log.i(TAG, "Entry #" + entryID + " synchronised.");

            dbConn.close();
        }

        private void setUpConnection(String remoteAddress) throws IOException {

            connection = null;

            // Create URL to the remote server
            URL remoteURL = new URL(remoteAddress);

            connection = (HttpURLConnection) remoteURL.openConnection();


            connection.setReadTimeout(10000 /*millis*/);
            connection.setConnectTimeout(15000 /*millis*/);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);

            //Set Content-Type for the Node server
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            //We can receive data
            connection.setDoInput(true);

            //We can post data
            connection.setDoOutput(true);

            //Set default chunking for the data
            connection.setChunkedStreamingMode(0);
        }

        private boolean checkNeedsSync() {

            //If we already have some stuff in our queue, just return
            if( unsyncedEntries.size() > 0 )
                return true;

            // Get DB connection through our SQL helper
            EPegSQLiteHelper dbConn = new EPegSQLiteHelper(NetworkSyncService.this);

            // Select DB
            SQLiteDatabase db = dbConn.getReadableDatabase();

            //Retrieve all non-synchronised entries
            Cursor dbCursor = db.query(EPegSQLiteHelper.TABLE_NAME, EPegSQLiteHelper.DB_COLUMNS, EPegSQLiteHelper.FIELD_SYNCHRONISED + " = 0", null, null, null, null);

            //Move cursor to the top of our result blob
            dbCursor.moveToFirst();

            Log.i(TAG, "DB Queried, rows need to be synced: " + dbCursor.getCount());

            // If we have no more entries in our queue, and no new entries in our DB, we are finished syncing.
            if(dbCursor.getCount() == 0)
                return false;

            // Place all new unsynced entries in our queue
            try {
                while (!dbCursor.isAfterLast()) {
                    JSONObject jsonPayload = new JSONObject()
                            .put(JSON_PAYLOAD_ID_TEMP_TAG, dbCursor.getLong(0) )
                            .put(JSON_PAYLOAD_DATA_TAG, dbCursor.getString(1))
                            .put(JSON_PAYLOAD_SECRETKEY_TAG, dbCursor.getString(2))
                            .put(JSON_PAYLOAD_IV_TAG, dbCursor.getString(3))
                            .put(JSON_PAYLOAD_TIMESTAMP_TAG, dbCursor.getString(4))
                            .put(JSON_PAYLOAD_DEVIDE_ID_TAG, dbCursor.getString(5))
                            .put(JSON_PAYLOAD_RESEARCHER_ID_TAG, dbCursor.getString(6));

                    Log.i(TAG, "Unsynced: " + jsonPayload.toString());

                    dbCursor.moveToNext();

                    unsyncedEntries.add(jsonPayload);
                }

            } catch (JSONException e) {
                // This cannot occur
            }finally {
                dbConn.close();
            }

            return true;

        }
    }
}
