package com.epeg.Study;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.epeg.MainActivity;
import com.epeg.R;
import com.epeg.SocketIOHandler;
import com.epeg.StudyFragmentPagerAdapter;
import com.epeg.WaitingForOtherPlayerFragment;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that controls the main flow of a study.
 *
 * @author Gergely Flamich, Patrick Schrempf
 */
public class StudyActivity extends AppCompatActivity {


    // App should reset after 2 mins = 120000 ms
    private static final long WATCHDOG_TIMEOUT = 120000;
    private static final String TAG = StudyActivity.class.getSimpleName();

    public enum STUDY_FRAG_TAG {
        CHOOSE_AGE(0),
        CHOOSE_GENDER(1),
        CHOOSE_HAND(2),
        LANDING_SCREEN(3),
        SETUP(4),
        TRIAL(5),
        PRE_RESULTS(6),
        RESULTS(7),
        WAITING_FOR_OTHER_PLAYER(8);

        int fragmentIndex;

        STUDY_FRAG_TAG(int fragmentIndex) {
            this.fragmentIndex = fragmentIndex;
        }

        public int index() {
            return fragmentIndex;
        }
    }

    public enum STUDY_REQ {
        NEW_SINGLE_GAME(0),
        NEW_MULTI_GAME(1),
        START_TRIAL(2),
        TRIAL_FINISHED(3),
        DISPLAY_READ(4),
        EXPERIMENT_DONE(5),
        GAME_RESET(6);

        int requestIndex;

        STUDY_REQ(int fragmentIndex) {
            this.requestIndex = fragmentIndex;
        }

        public int index() {
            return requestIndex;
        }
    }

    private ViewPager studyFragmentContainer;

    private boolean leftToRight = false;
    private boolean isSinglePlayer;
    private boolean shouldTurnScreen;

    Participant participant;

    private int systemUiVisibilitySetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        // Lock the orientation of the device and disable various physical controls
        initSettings();

        SocketIOHandler.setUiHandler(new Handler(Looper.getMainLooper()));

        try {

            // Either create a new study if one is not running already, or resume the previous one.
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

        studyFragmentContainer = (ViewPager) findViewById(R.id.study_fragment_pager);

        setupStudyFragments(studyFragmentContainer);

        setStudyFragment(STUDY_FRAG_TAG.CHOOSE_AGE);

        // Once we successfully established the connection to the ePeg server, send what type of study we want to start
        isSinglePlayer = getIntent().getExtras().getBoolean("isSinglePlayer");
        shouldTurnScreen = getIntent().getExtras().getBoolean("shouldTurnScreen");

        Log.i(TAG, "IS SINGLE? " + isSinglePlayer);
        Log.i(TAG, "SHOULD ROTATE? "+ shouldTurnScreen);

        if (isSinglePlayer) {
            SocketIOHandler.sendMessage(STUDY_REQ.NEW_SINGLE_GAME, null);
        } else {
            SocketIOHandler.sendMessage(STUDY_REQ.NEW_MULTI_GAME, null);
            waitForOtherPlayer(STUDY_FRAG_TAG.CHOOSE_AGE);
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
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        });

        // Start watchdog
        setupLongTimeout();

    }

    private void setupStudyFragments(ViewPager viewPager) {
        StudyFragmentPagerAdapter adapter = new StudyFragmentPagerAdapter(getSupportFragmentManager());

        Bundle labelArg = new Bundle();
        labelArg.putString("label", participant.getLabel());

        ArrayList<Fragment> studyFragments = new ArrayList<>();

        studyFragments.add(new AgeSelectionFragment());
        studyFragments.add(new GenderSelectionFragment());
        studyFragments.add(new ChooseHandFragment());
        studyFragments.add(new StudyLandingScreenFragment());
        studyFragments.add(new SetupFragment());
        studyFragments.add(new TrialFragment());
        studyFragments.add(new PreResultFragment());
        studyFragments.add(new ResultFragment());
        studyFragments.add(new WaitingForOtherPlayerFragment());

        for(Fragment studyFragment : studyFragments){
            studyFragment.setArguments(labelArg);
            adapter.addFragment(studyFragment, studyFragment.getClass().getName());
        }

        viewPager.setAdapter(adapter);
    }

    public void setStudyFragment(STUDY_FRAG_TAG tag, boolean smoothScroll) {
        studyFragmentContainer.setCurrentItem(tag.index(), smoothScroll);
    }

    public void setStudyFragment(STUDY_FRAG_TAG tag) {
        setStudyFragment(tag, true);
    }

    public void waitForOtherPlayer(STUDY_FRAG_TAG moveToAfterResponse) {
        // Move to the waiting screen
        setStudyFragment(STUDY_FRAG_TAG.WAITING_FOR_OTHER_PLAYER, false);

        // When both players are ready, move back
        SocketIOHandler.setResponseFunction(() -> setStudyFragment(moveToAfterResponse, false));
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
        SocketIOHandler.getSocket().connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // When the app is moved to the background, we disconnect from the server.
        SocketIOHandler.getSocket().disconnect();
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

    // =============================================================================================
    // Watchdog for the app
    // =============================================================================================
    Timer longTimer;

    synchronized void setupLongTimeout() {
        if(longTimer != null) {
            longTimer.cancel();
            longTimer = null;
        }

        longTimer = new Timer();
        longTimer.schedule(new TimerTask() {
            public void run() {
                longTimer.cancel();
                longTimer = null;

                // If we timeout, we cancel the study.
                cancelStudy();
            }
        }, WATCHDOG_TIMEOUT);
    }

    // We reset the watchdog if the user does something
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

        setupLongTimeout(); // 2
    }

    /**
     * Callback function that enables Fragments to cancel the current study cleanly.
     */
    public void cancelStudy() {
        // start intent to go back to MainActivity

        Study.cancel();
        startActivity(new Intent(this, MainActivity.class));

        SocketIOHandler.sendMessage(STUDY_REQ.GAME_RESET, null);
    }

    /**
     * Cancels the current trial - flips orientation and shows study activity.
     *
     * @param view - caller
     */
//    public void cancelTrial(View view) {
//        switch (view.getId()) {
//            case R.id.cancel_trial:
//                flipOrientation();
//                setStudyFragment(STUDY_FRAG_TAG.LANDING_SCREEN);
//                break;
//        }
//    }

    /**
     * Callback that ends the current trial, whether it succeeded or failed.
     *
     * @param trial - trial that has ended
     */
    public void endTrial(Trial trial) {
        Log.d(TAG, "End of trial.");

        try {
            SocketIOHandler.sendMessage(STUDY_REQ.TRIAL_FINISHED, trial.jsonify());
        } catch (JSONException | TrialFailureException e) {
            e.printStackTrace();
        }

        flipOrientation();

        // swap trial direction
        leftToRight = !leftToRight;

        try {
            // add trial to study
            Study.addNextTrial(trial);
            Log.d(TAG, "Trial successful! (changing leftToRight)");

            if (Study.isFinished()) {
                Log.d(TAG, "Study finished!");



                // show result fragment
                if (isSinglePlayer)
                    setStudyFragment(STUDY_FRAG_TAG.PRE_RESULTS);
                else
                    waitForOtherPlayer(STUDY_FRAG_TAG.PRE_RESULTS);

                // Conclude study if possible
                try {
                    Study.conclude();
                    Log.d(TAG, "Study concluded!");
                } catch (StudyException e) {
                    Log.e(TAG, "Could not conclude study! Error: " + e.getMessage());
                    Study.cancel();
                    Log.d(TAG, "Study cancelled!");
                }

                return;
            }

        } catch (StudyException e) {
            Log.d(TAG, "Trial failed!");
            Log.d(TAG, e.getMessage());
        }

        if (isSinglePlayer)
            setStudyFragment(STUDY_FRAG_TAG.LANDING_SCREEN);
        else
            waitForOtherPlayer(STUDY_FRAG_TAG.LANDING_SCREEN);
    }

    /**
     * Changes the orientation of the screen by 180 degrees.
     */
    private void flipOrientation() {
        if (!shouldTurnScreen) return;

        Log.i(TAG, "orientation: " + getRequestedOrientation() + ", landscape:" + ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Log.i(TAG, "orientation: " + getRequestedOrientation() + ",reverse landscape:" + ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    /**
     * Returns if the current trial is going from left to right.
     *
     * @return if trial is going from left to right
     */
    public boolean isLeftToRight() {
        return leftToRight;
    }

    /**
     * Override volume controls.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;

            default:
                result = super.dispatchKeyEvent(event);
                break;
        }

        return result;
    }

    /**
     * Initialises settings.
     */
    private void initSettings() {
        systemUiVisibilitySetting = getWindow().getDecorView().getSystemUiVisibility();


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
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibilitySetting);

    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }


}
