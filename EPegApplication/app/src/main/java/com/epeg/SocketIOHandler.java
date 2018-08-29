package com.epeg;

import android.util.Log;

import com.epeg.Study.StudyActivity;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import android.os.Handler;

public class SocketIOHandler {

    private static final String DEFAULT_UUID = "epegExhibTestTablet";
    private static final String EXHIBITION_URL = "http://192.168.0.4:18216";
    private static final String TAG = SocketIOHandler.class.getName();

    private static Socket socket;

    private static String UUID = null;

    // responseFunction can be set by the app so that whenever we want to react to a server action
    // we perform some UI action.
    private static Runnable responseFunction = () -> {};

    private static Handler uiHandler;

    public static synchronized Socket getSocket(){
        return getSocket(DEFAULT_UUID);
    }

    /**
     * This is the singleton constructor for a Socket in the socket handler. Given a UUID, we create
     * the socket
     *
     * @return
     */
    public static synchronized Socket getSocket(String uuid){
        if(socket != null) return socket;

        if(uuid == null) throw new NullPointerException("The UUID of the ePeg WebSocket cannot be null!");

        if (UUID == null) UUID = uuid;

        // Attempt to establish the socket connection to the server.
        try{
            IO.Options extras = new IO.Options();

            extras.query = "client_type=tablet&tablet_id=" + UUID;

            socket = IO.socket(EXHIBITION_URL, extras);

            socket.on("server_action", (args) -> {
                Log.d(TAG, "Received server action!");


                if (uiHandler != null)
                    uiHandler.postDelayed(responseFunction, 500);
                else
                    Log.e(TAG, "Could not handle server action!");
            });

        } catch (URISyntaxException e) {
            Log.e(TAG, "Couldn't establish socket connection: " + e.getMessage());
        }

        return socket;
    }

    public static void setResponseFunction(Runnable r){
        responseFunction = r;
    }

    public static void setUiHandler(Handler handler){
        uiHandler = handler;
    }

    public static void sendMessage(final StudyActivity.STUDY_REQ actionType, final JSONObject actionData){
        if(socket == null){
            Log.e(TAG, "NO SOCKET SET TO SEND WITH!");
        }
        try {
            JSONObject message = new JSONObject();
            message.put("sender_id", UUID);
            message.put("action_type", actionType.index());
            message.put("action_data", actionData);

            Log.i(TAG, "SENT:" + message.toString());

            socket.emit("player_action", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        if (socket == null && !socket.connected()) socket.connect();
    }
}
