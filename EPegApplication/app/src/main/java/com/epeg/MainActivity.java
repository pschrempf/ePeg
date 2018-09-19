package com.epeg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.epeg.Study.Study;
import com.epeg.Study.StudyActivity;
import com.github.nkzawa.socketio.client.Socket;

import java.io.IOException;
import java.util.List;

/**
 * Class for the main activity and settings page.
 *
 * @author Gergely Flamich, Patrick Schrempf
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NUMBER_OF_TRIALS = 3;

    // This variable is used to track whether the settings page should be shown at all or not.
    private static boolean isTabletConfigured = false;
    private static boolean shouldTurnScreen = true;

    private SettingsManager sm;

    private int accelerometerSetting;
    private int userSetting;
    private int screenBrightnessModeSetting;
    private int screenBrightnessSetting;
    private int systemUiVisibilitySetting;

    private static boolean defaultOrientationIsLandscape = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Study.setNumTrials(NUMBER_OF_TRIALS);

        initSettings();

        sm = new SettingsManager(this.getApplicationContext());

        // only display the settings window if it is the first time us running the app.
        if(!isTabletConfigured) researcherSettings(getApplicationContext());
        else {
            Log.d(TAG, "Already configured");
            setContentView(R.layout.activity_main);
        }

        // set view to update UI flags after change
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.d(TAG, "Resetting UI visibility");
            setDefaultOperationFlags();
        });

        String uuid = sm.getActiveResearcher() + "-" + sm.getActiveClinic();

        // Create socket and connect it to the server with the above UUID
        Socket s = SocketIOHandler.getSocket(uuid);
        if (!s.connected()) s.connect();

        SocketIOHandler.setUpMain(new Handler(Looper.getMainLooper()),
                () -> {
                    Button startGameBtn =(Button) findViewById(R.id.start_game);
                    startGameBtn.setText("Join Game");
                    startGameBtn.setOnClickListener((v) -> {
                                Intent studyIntent = new Intent(MainActivity.this, StudyActivity.class);
                                studyIntent.putExtra("isSinglePlayer", false);
                                studyIntent.putExtra("shouldTurnScreen", shouldTurnScreen);
                                sm.close();
                                MainActivity.this.startActivity(studyIntent);
                    });
                },
                () -> {
                    Button startGameBtn = (Button) findViewById(R.id.start_game);
                    startGameBtn.setText("Wait for other player to finish");
                    startGameBtn.setEnabled(false);
                    startGameBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    startGameBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                },
                () -> {
                    Button startGameBtn = (Button) findViewById(R.id.start_game);
                    startGameBtn.setText(getResources().getText(R.string.start_single_player_button));
                    startGameBtn.setEnabled(true);
                    startGameBtn.setTextColor(Color.WHITE);
                    startGameBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    startGameBtn.setOnClickListener(this::startStudy);
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming epeg Main activity");
        setDefaultOperationFlags();

    }


    /**
     * Show researcher settings fragment.
     *
     * @param context
     */
    private void researcherSettings(final Context context) {
        setContentView(R.layout.fragment_researcher);

        // initialise researcher spinner
        loadResearchers(context);

        Button newResearcherButton = (Button) findViewById(R.id.add_new_researcher);
        newResearcherButton.setOnClickListener(v -> addNewResearcher(context, v));

        // initialise clinic code spinner
        loadClinicCodes(context);

        Button newClinicButton = (Button) findViewById(R.id.add_new_clinic_code);
        newClinicButton.setOnClickListener(v -> addNewClinicCode(context, v));

        Switch sw = (Switch) findViewById(R.id.tablet_rotation_toggle);

        sw.setOnCheckedChangeListener((v, checked) -> {
            Log.i(TAG, "Toggle: " + checked);

            shouldTurnScreen = checked;
        });

        sw = (Switch) findViewById(R.id.tablet_landscape_toggle);

        sw.setOnCheckedChangeListener((v, checked) -> {
            Log.i(TAG, "Is in landscape: " + checked);

            defaultOrientationIsLandscape = checked;

        });

        isTabletConfigured = true;
    }

    private void loadResearchers(Context context) {
        final List<String> researchers = sm.getAllResearchers();
        final Spinner researcherSpinner = (Spinner) findViewById(R.id.researcher_spinner);

        ArrayAdapter<String> researcherSpinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, researchers);
        researcherSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        researcherSpinner.setAdapter(researcherSpinnerAdapter);

        researcherSpinner.post(() -> {
            String activeResearcher = sm.getActiveResearcher();

            if (null != activeResearcher) {
                for (int i = 0; i < researchers.size(); i++) {
                    if (researchers.get(i).equals(activeResearcher)) {
                        researcherSpinner.setSelection(i);
                        Log.d(TAG, "Setting selection of spinner: " + researchers.get(i));
                    }
                }
            }
        });

        researcherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // set researcher
                String researcher = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "setting active researcher: " + researcher);
                sm.setActiveResearcher(researcher);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadClinicCodes(Context context) {
        final List<String> codes = sm.getAllClinicIDs();
        final Spinner codeSpinner = (Spinner) findViewById(R.id.clinic_code_spinner);

        ArrayAdapter<String> codeSpinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, codes);
        codeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        codeSpinner.setAdapter(codeSpinnerAdapter);

        codeSpinner.post(() -> {
            String activeClinic = sm.getActiveClinic();

            if (null != activeClinic) {
                for (int i = 0; i < codes.size(); i++) {
                    if (codes.get(i).equals(activeClinic)) {
                        codeSpinner.setSelection(i);
                        Log.d(TAG, "Setting selection of spinner: " + codes.get(i));
                    }
                }
            }
        });

        codeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String clinicCode = (String) parent.getItemAtPosition(position);
                // set active clinic
                Log.d(TAG, "setting active clinic: " + clinicCode);
                sm.setActiveClinic(clinicCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Complete research settings and go back to main activity.
     * @param view - caller
     */
    public void researchSettingsComplete(View view) {
        if (view.getId() == R.id.research_settings_complete) {

            // check that we have active clinic and researcher, if not show popup
            if (sm.getActiveClinic() == null) {
                showPopup(view, getResources().getString(R.string.researcher_settings_popup_prompt));
            } else if (sm.getActiveResearcher() == null) {
                showPopup(view, getResources().getString(R.string.researcher_settings_popup_prompt));
            } else {
                // commence with main activity
                setContentView(R.layout.activity_main);
                restoreSettings();
            }
        }
    }

    /**
     * Shows a simple popup window in the centre of the screen, displaying text.
     *
     * @param view
     * @param textToShow
     */
    private void showPopup(View view, String textToShow) {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.popup));
        TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
        popupText.setText(textToShow);
        Button popupButton = (Button) layout.findViewById(R.id.popup_close);
        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupButton.setOnClickListener(v -> popup.dismiss());
        popup.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    /**
     * Shows popup window to add researcher.
     *
     * @param view
     */
    public void addNewResearcher(final Context context, View view) {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        View layout = inflater.inflate(R.layout.editable_popup, (ViewGroup) findViewById(R.id.editable_popup));

        TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
        popupText.setText(R.string.new_researcher_message);

        final EditText popupEditText = (EditText) layout.findViewById(R.id.popup_edit_text);
        popupEditText.setHint("e.g. Dr. Abc");

        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        Button popupCloseButton = (Button) layout.findViewById(R.id.popup_close);
        popupCloseButton.setOnClickListener(v -> popup.dismiss());

        Button popupSaveButton = (Button) layout.findViewById(R.id.popup_save);
        popupSaveButton.setOnClickListener(v -> {
            try {
                String newResearcher = popupEditText.getText().toString();
                Log.i(TAG, "Adding new researcher: " + newResearcher);
                sm.addResearcher(newResearcher);
                sm.setActiveResearcher(newResearcher);
                loadResearchers(context);
                popup.dismiss();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        });

        popup.showAtLocation(view, Gravity.TOP, 0, 0);
    }

    /**
     * Shows popup window to add clinic code.
     *
     * @param view
     */
    public void addNewClinicCode(final Context context, View view) {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        View layout = inflater.inflate(R.layout.editable_popup, (ViewGroup) findViewById(R.id.editable_popup));

        TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
        popupText.setText(R.string.new_clinic_code_message);

        final EditText popupEditText = (EditText) layout.findViewById(R.id.popup_edit_text);
        popupEditText.setHint("e.g. STA");

        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        Button popupCloseButton = (Button) layout.findViewById(R.id.popup_close);
        popupCloseButton.setOnClickListener(v -> popup.dismiss());

        Button popupSaveButton = (Button) layout.findViewById(R.id.popup_save);
        popupSaveButton.setOnClickListener(v -> {
            try {
                String newCode = popupEditText.getText().toString();
                Log.i(TAG, "Adding new clinic code: " + newCode);
                sm.addClinicID(newCode);
                sm.setActiveClinic(newCode);
                loadClinicCodes(context);
                popup.dismiss();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        });

        popup.showAtLocation(view, Gravity.TOP, 0, 0);
    }

    /**
     * Starts study and syncs with network.
     *
     * @param view - caller
     */
    public void startStudy(View view) {
        Intent studyIntent = new Intent(MainActivity.this, StudyActivity.class);
        studyIntent.putExtra("isSinglePlayer", true);
        studyIntent.putExtra("shouldTurnScreen", shouldTurnScreen);
        sm.close();
        MainActivity.this.startActivity(studyIntent);
    }

    /**
     * Hides settings fragment.
     *
     * @param view - caller
     */
    public void hideSettings(View view) {
        if (view.getId() == R.id.hide_settings) {
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        restoreSettings();
    }

    /**
     * Initialise settings.
     */
    private void initSettings() {
        try {
            // Ask for write settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {

                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            }


            accelerometerSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            userSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.USER_ROTATION);
            screenBrightnessModeSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            screenBrightnessSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            systemUiVisibilitySetting = getWindow().getDecorView().getSystemUiVisibility();

            Log.d(TAG, "Rotation: " + userSetting);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        setDefaultOperationFlags();
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
     * Set flags for rotation, brightness and fullscreen mode.
     */
    public void setDefaultOperationFlags() {
        // set fixed rotation of tablet
        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        if (defaultOrientationIsLandscape) {
            Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
        } else {
            Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
        }

        // set fixed brightness of screen
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);

        // set to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set full screen immersive mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Restores settings to same as before activity.
     */
    private void restoreSettings() {
        // set fixed rotation of tablet
        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, accelerometerSetting);
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, defaultOrientationIsLandscape ? Surface.ROTATION_0 : Surface.ROTATION_90);

        // set fixed brightness of screen
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screenBrightnessModeSetting);
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessSetting);

        // set full screen immersive mode
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibilitySetting);

    }

}
