package com.epeg.Study;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.epeg.MainActivity;
import com.epeg.R;
import com.epeg.StudyFragmentPagerAdapter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Class that controls the main flow of a study.
 *
 * @author Gergely Flamich, Patrick Schrempf
 */
public class StudyActivity extends AppCompatActivity {

    public enum STUDY_FRAG_TAG{
        PARTICIPANT_CODE(0),
        CHOOSE_HAND(1),
        LANDING_SCREEN(2),
        SETUP(3),
        TRIAL(4),
        RESULTS(5);

        int fragmentIndex;

        STUDY_FRAG_TAG(int fragmentIndex){
            this.fragmentIndex = fragmentIndex;
        }

        public int index(){
            return fragmentIndex;
        }
    }

    private static final String TAG = StudyActivity.class.getSimpleName();
    private static final String EXHIBITION_URL = "http://192.168.0.4:18216";

    // The view pager will control the flow of the application, as it will hold all the fragments
    // That form a part of the study
    private StudyFragmentPagerAdapter studyFragmentPagerAdapter;
    private ViewPager studyFragmentContainer;

    private boolean leftToRight;
    private boolean demo;
    private int demosAvailable;

    Participant participant;

    private int systemUiVisibilitySetting;

    private Socket epegWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        initSettings();

//        fm = getFragmentManager();
//        currentFragment = null;
//        demo = false;
//        demosAvailable = 3;

        try {

            if (null == savedInstanceState || !savedInstanceState.getBoolean("isRunning", false)) {
                Study.startNew(getApplicationContext());

                // set up new participant with generated label
                participant = new Participant(Study.generateNewParticipantLabel());
                Study.setParticipant(participant);
            }

            participant = Study.getParticipant();

        } catch (StudyException e) {
            e.printStackTrace();
            cancelStudy();
        }

        studyFragmentPagerAdapter = new StudyFragmentPagerAdapter(getSupportFragmentManager());

        studyFragmentContainer = (ViewPager) findViewById(R.id.study_fragment_pager);

        setupStudyFragments(studyFragmentContainer);


        // Attempt to establish the socket connection to the server.
        try{
            epegWebSocket = IO.socket(EXHIBITION_URL);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Couldn't establish socket connection: " + e.getMessage());
        }

        // set view to update UI flags after change
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.d(TAG, "Resetting UI visibility");
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        });
    }

    private void setupStudyFragments(ViewPager viewPager){
        StudyFragmentPagerAdapter adapter = new StudyFragmentPagerAdapter(getSupportFragmentManager());

        ParticipantCodeFragment pcf = new ParticipantCodeFragment();
        Bundle labelArg = new Bundle();
        labelArg.putString("label", participant.getLabel());
        pcf.setArguments(labelArg);

        adapter.addFragment(pcf, "participant code");
        adapter.addFragment(new ChooseHandFragment(), "choose hand");
        adapter.addFragment(new StudyLandingScreenFragment(), "landing screen");
        adapter.addFragment(new SetupFragment(), "setup");
        adapter.addFragment(new TrialFragment(), "trial");
        adapter.addFragment(new ResultFragment(), "results");

        viewPager.setAdapter(adapter);
    }

    public void setStudyFragment(STUDY_FRAG_TAG tag){

        studyFragmentContainer.setCurrentItem(tag.index(), true);

        // TODO: to be sent after the hand fragment has been chosen
        //sendMessage(R.integer.REQ_DISPLAY_READ, null);

        // TODO: to be sent after we started the trial
        //sendMessage(R.integer.REQ_START_TRIAL, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming epeg Study activity");

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // When the app is in the foreground, reconnect to the server
        epegWebSocket.connect();

        sendMessage(R.integer.REQ_NEW_SINGLE_GAME, null);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // When the app is moved to the background, we disconnect from the server.
        epegWebSocket.disconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", true);
    }

    @Override
    public void onStop() {
        super.onStop();
        restoreSettings();
    }

    /**
     * Callback function that enables Fragments to cancel the current study cleanly.
     */
    public void cancelStudy() {
        // start intent to go back to MainActivity
        Study.cancel();
        //fm.beginTransaction().remove(caller).commit();
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * End study and return to main activity.
     * @param v - must be R.id.end_study_button
     */
    public void endStudy(View v) {
        switch(v.getId()) {
            case R.id.end_study_button:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    /**
     * Callback that sets the dominant hand and starts trial.
     *
     * @param view - Button of hand chosen
     */
    public void setDominantHand(View view) throws StudyException {
        String dominantHand;
        switch(view.getId()) {
            case R.id.right_hand_button:
                dominantHand = getResources().getString(R.string.right_hand_button);
                Study.getParticipant().setIsRightHanded(true);
                break;
            case R.id.left_hand_button:
                dominantHand = getResources().getString(R.string.left_hand_button);
                Study.getParticipant().setIsRightHanded(false);
                break;
            default:
                throw new StudyException("Incorrect callback on 'setDominantHand' from id: " + view.getId());
        }
        Log.d(TAG, "Setting dominant hand: " + dominantHand + ".");

        // start all studies from right
        leftToRight = false;

        setStudyFragment(STUDY_FRAG_TAG.LANDING_SCREEN);
    }


    /**
     * Cancels the current trial - flips orientation and shows study activity.
     * @param view - caller
     */
    public void cancelTrial(View view) {
        switch(view.getId()) {
            case R.id.cancel_trial:
                flipOrientation();
                setStudyFragment(STUDY_FRAG_TAG.LANDING_SCREEN);
                break;
        }
    }

    /**
     * Callback that ends the current trial, whether it succeeded or failed.
     *
     * @param trial - trial that has ended
     */
    public void endTrial(Trial trial) {
        Log.d(TAG, "End of trial.");

        try {
            sendMessage(R.integer.REQ_TRIAL_FINISHED, trial.jsonify());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (TrialFailureException e) {
            e.printStackTrace();
        }

        flipOrientation();

        // swap trial direction
        leftToRight = !leftToRight;

        if (!demo) {
            try {
                // add trial to study
                Study.addNextTrial(trial);
                Log.d(TAG, "Trial successful! (changing leftToRight)");

                if (Study.isFinished()) {
                    Log.d(TAG, "Study finished!");

                    // show result fragment
                    setStudyFragment(STUDY_FRAG_TAG.RESULTS);
                    return;
                }

            } catch (StudyException e) {
                Log.d(TAG, "Trial failed!");
                Log.d(TAG, e.getMessage());
            }
        } else if (demosAvailable > 0) {

            // set one demo unavailable
            demosAvailable--;

            setStudyFragment(STUDY_FRAG_TAG.SETUP);
        }

        setStudyFragment(STUDY_FRAG_TAG.LANDING_SCREEN);
    }

    /**
     * Complete the setup of the study and start trial.
     */
    public void setupComplete(View view) {
        setStudyFragment(STUDY_FRAG_TAG.TRIAL); // trial
    }


    /**
     * Changes the orientation of the screen by 180 degrees.
     */
    private void flipOrientation() {
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.USER_ROTATION) == Surface.ROTATION_0) {
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
            } else if (Settings.System.getInt(getContentResolver(), Settings.System.USER_ROTATION) == Surface.ROTATION_90) {
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
            } else if (Settings.System.getInt(getContentResolver(), Settings.System.USER_ROTATION) == Surface.ROTATION_180) {
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
            } else {
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Returns if the current trial is going from left to right.

     * @return if trial is going from left to right
     */
    public boolean isLeftToRight() {
        return leftToRight;
    }

    /**
     * Return if the current trial is a demo trial.
     * @return if current trial is a demo trial
     */
    public boolean isDemo() {
        return demo;
    }

    /**
     * Override volume controls.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result;
        switch( event.getKeyCode() ) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;

            default:
                result= super.dispatchKeyEvent(event);
                break;
        }

        return result;
    }

    /**
     * Initialises settings.
     */
    private void initSettings() {
        systemUiVisibilitySetting = getWindow().getDecorView().getSystemUiVisibility();

        // set to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set full screen immersive mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Restores setting to same as before the activity.
     */
    private void restoreSettings() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibilitySetting);

    }

    private void sendMessage(final int actionType, final JSONObject actionData){
        try {
            JSONObject message = new JSONObject();
            message.put("sender_id", "BLALA");
            message.put("action_type", actionType);
            message.put("action_data", actionData);

            epegWebSocket.emit("player_action", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
