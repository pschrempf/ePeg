package com.epeg;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gf38 on 19/06/18.
 */

public class ExhibitionAsyncTask extends AsyncTask<JSONObject, Void, Void> {

    private static final String EXHIBITION_SERVER_URL = "https://gf38.host.cs.st-andrews.ac.uk/epegExhibitionData";
    public static final String TAG = ExhibitionAsyncTask.class.getName();

    private NetworkInfo networkInfo;

    private HttpURLConnection connection;


    /**
     * Constructor for our asynchronous task that will be sending the live info from the trials
     * to the server so that it can be displayed.
     *
     * @param networkInfo the network status of the context from which the AsyncTask
     *                    is being called. This is needed, because we must check if
     *                    we are connected to the Internet before we can send anything.
     */
    public ExhibitionAsyncTask(NetworkInfo networkInfo){
        this.networkInfo = networkInfo;
    }

    @Override
    protected Void doInBackground(JSONObject... jsonObjects) {

        DataOutputStream outputStream;

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i(TAG, "Device is connected to a network.");

            try {
                // setup a basic Http connection to the given address
                setUpConnection(EXHIBITION_SERVER_URL);

                connection.connect();

                // This is where we will be writing the json objects
                outputStream = new DataOutputStream(connection.getOutputStream());

                // Send the JSON message to the server as a POST request
                for (JSONObject epegInfo :
                        jsonObjects) {

                    outputStream.writeBytes(epegInfo.toString());
                    outputStream.flush();

                    Log.i(TAG, epegInfo.toString());

                    int response = connection.getResponseCode();
                    int len = connection.getContentLength();

                    Log.i(TAG, "Server response: " + response + " Message: " + connection.getResponseMessage() + " Content length: " + len);

                }


            } catch (IOException e) {
                Log.e(TAG, "Connecting to remote address: " +
                        EXHIBITION_SERVER_URL +
                        " failed!\n Error: " +
                        e.getMessage());
            }
        }
        return null;
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

}
