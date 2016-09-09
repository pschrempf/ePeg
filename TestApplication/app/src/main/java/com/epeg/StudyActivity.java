package com.epeg;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Class that controls the main flow of a study.
 *
 * @author Patrick Schrempf
 */
public class StudyActivity extends Activity {

    private static final String TAG = StudyActivity.class.getSimpleName();

    private FragmentManager fm;
    private Fragment currentFragment;
    private boolean leftToRight;
    private boolean demo;
    private boolean demoAvailable;

    private int systemUiVisibilitySetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        initSettings();

        fm = getFragmentManager();
        currentFragment = null;
        demo = false;
        demoAvailable = true;

        try {

            Study.startNew(getApplicationContext());

            // set up new participant with generated label
            Participant participant = new Participant(Study.generateNewParticipantLabel());
            Study.setParticipant(participant);

            showParticipantLabel(participant.getLabel());

        } catch (StudyException e) {
            e.printStackTrace();
            cancelStudy(currentFragment);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        restoreSettings();
    }

    /**
     * Inflates fragment_participant_code layout to show participant label.
     */
    public void showParticipantLabel(String label) {
        setContentView(R.layout.fragment_participant_code);

        TextView tv1 = (TextView) findViewById(R.id.participant_code);
        tv1.setText(getResources().getString(R.string.participant_code, label));
    }

    /**
     * Show ChooseHandFragment.
     * @param view - caller
     */
    public void chooseHand(View view) {
        if (view.getId() == R.id.start_choose_hand)
            updateCurrentFragment(new ChooseHandFragment(), "choose_hand");
    }

    /**
     * Callback function that enables Fragments to cancel the current study cleanly.
     *
     * @param caller - Fragment that is performing the callback
     */
    public void cancelStudy(Fragment caller) {
        // start intent to go back to MainActivity
        Study.cancel();
        fm.beginTransaction().remove(caller).commit();
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

        // start all studies from left
        leftToRight = true;

        showActivity();
    }

    /**
     * Shows activity_study layout, setting the number of trials completed.
     */
    public void showActivity() {
        setContentView(R.layout.activity_study);

        TextView info = (TextView) findViewById(R.id.trial_status);
        info.setText(getResources().getString(R.string.trial_status, Study.getCurrentTrialIndex(), Study.numTrials * 2));

        // hide demo button
        if (!demoAvailable) {
            Button demo = (Button) findViewById(R.id.start_demo);
            demo.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Starts a new SetupFragment.
     * @param view - caller
     */
    public void startTrial(View view) {
        demo = false;
        demoAvailable = false;
        updateCurrentFragment(new SetupFragment(), "setup");
    }

    /**
     * Cancels the current trial - flips orientation and shows study activity.
     * @param view - caller
     */
    public void cancelTrial(View view) {
        switch(view.getId()) {
            case R.id.cancel_trial:
                flipOrientation();
                showActivity();
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

        flipOrientation();

        if (!demo) {
            try {

                // add trial to study
                Study.addNextTrial(trial);
                Log.d(TAG, "Trial successful! (changing leftToRight)");

                // swap trial direction
                leftToRight = !leftToRight;

                if (Study.isFinished()) {
                    Log.d(TAG, "Study finished!");

                    // show result fragment
                    updateCurrentFragment(new ResultFragment(), "results");
                    return;
                }


            } catch (StudyException e) {
                Log.d(TAG, "Trial failed!");
            }
        } else if (demoAvailable) {

            // swap trial direction
            leftToRight = !leftToRight;

            // set demo unavailable
            demoAvailable = false;

            updateCurrentFragment(new SetupFragment(), "setup");
        }

        showActivity();
    }

    /**
     * Complete the setup of the study and start trial.
     * @param view - caller
     */
    public void setupComplete(View view) {
        switch(view.getId()) {
            case R.id.setup_complete:
                updateCurrentFragment(new TrialFragment(), "trial");
                break;
        }
    }

    /**
     * Start trial demo.
     * @param view - caller
     */
    public void startDemo(View view) {
        switch(view.getId()) {
            case R.id.start_demo:
                demo = true;
                updateCurrentFragment(new SetupFragment(), "setup");
                break;
        }
    }

    /**
     * Removes and updates the current fragment, adding the new fragment with a specified tag.
     *
     * @param newFragment - new fragment to replace current fragment
     * @param tag - tag to add to new fragment in fragment manager
     */
    private void updateCurrentFragment(Fragment newFragment, String tag) {
        FragmentTransaction ft = fm.beginTransaction();

        // remove current fragment
        if (currentFragment != null) {
            ft.remove(currentFragment);
        }

        // set, add and commit new fragment
        currentFragment = newFragment;
        ft.add(currentFragment, tag).commit();
    }

    /**
     * Changes the orientation of the screen by 180 degrees.
     */
    private void flipOrientation() {
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.USER_ROTATION) == Surface.ROTATION_0) {
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
            } else {
                Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
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
}
