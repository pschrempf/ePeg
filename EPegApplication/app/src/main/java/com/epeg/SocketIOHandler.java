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
    private static Runnable responseFunction = () -> {
    };

    private static Runnable joinFunction = () -> {
    };
    private static Runnable lockFunction = () -> {
    };
    private static Runnable unlockFunction = () -> {
    };

    private static Handler mainUiHandler;
    private static Handler studyUiHandler;

    private static int responseTrigger = -1;

    // Constants for the server messages
    public static final int MULTIPLAYER_PROGRESS = 1;
    public static final int GAME_STARTED = 3;
    public static final int GAME_LOCKED = 4;
    public static final int GAME_UNLOCKED = 5;
    public static final int GAME_JOINED = 6;

    public static synchronized Socket getSocket() {
        return getSocket(DEFAULT_UUID);
    }

    /**
     * This is the singleton constructor for a Socket in the socket handler. Given a UUID, we create
     * the socket
     *
     * @return
     */
    public static synchronized Socket getSocket(String uuid) {
        if (socket != null) return socket;

        if (uuid == null)
            throw new NullPointerException("The UUID of the ePeg WebSocket cannot be null!");

        if (UUID == null) UUID = uuid;

        // Attempt to establish the socket connection to the server.
        try {
            IO.Options extras = new IO.Options();

            extras.query = "client_type=tablet&tablet_id=" + UUID;

            socket = IO.socket(EXHIBITION_URL, extras);

            socket.on("server_action", (args) -> {
                Log.d(TAG, "Received server action!");

                JSONObject data = (JSONObject) args[0];
                Log.d(TAG, data.toString());

                try {
                    if (mainUiHandler == null)
                        Log.e(TAG, "Could not handle server action in MainActivity!");

                    switch (data.getInt("action_type")) {
                        case GAME_STARTED:
                            mainUiHandler.post(joinFunction);
                            return;
                        case GAME_LOCKED:
                            mainUiHandler.post(lockFunction);
                            return;
                        case GAME_UNLOCKED:
                            mainUiHandler.post(unlockFunction);
                            return;
                        default:
                            Log.i(TAG, "Unrecognised server action in MainActivity!");
                    }


                    // Check if we have an Activity where we can run this
                    if (studyUiHandler != null)
                        if(data.getInt("action_type") == responseTrigger)
                            studyUiHandler.postDelayed(responseFunction, 500);
                    else
                        Log.e(TAG, "Could not handle server action in StudyActivity!");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        } catch (URISyntaxException e) {
            Log.e(TAG, "Couldn't establish socket connection: " + e.getMessage());
        }

        return socket;
    }

    public static void setResponseFunction(Runnable r, int responseT) {
        responseFunction = r;

        responseTrigger = responseT;
    }

    public static void setUpMain(Handler handler, Runnable joinF, Runnable lockF, Runnable unlockF) {
        mainUiHandler = handler;
        joinFunction = joinF;
        lockFunction = lockF;
        unlockFunction = unlockF;
    }

    public static void setStudyUiHandler(Handler handler) {
        studyUiHandler = handler;
    }

    public static void sendMessage(final StudyActivity.STUDY_REQ actionType, final JSONObject actionData) {
        if (socket == null) {
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
