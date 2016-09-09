package com.epeg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

/**
 * Class for the main activity and settings page.
 *
 * @author Patrick Schrempf
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SettingsManager sm;

    private int accelerometerSetting;
    private int userSetting;
    private int screenBrightnessModeSetting;
    private int screenBrightnessSetting;
    private int systemUiVisibilitySetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSettings();

        sm = new SettingsManager(this.getApplicationContext());
        researcherSettings(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        researcherSettings(getApplicationContext());
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
        newResearcherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewResearcher(context, v);
            }
        });

        // initialise clinic code spinner
        loadClinicCodes(context);

        Button newClinicButton = (Button) findViewById(R.id.add_new_clinic_code);
        newClinicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewClinicCode(context, v);
            }
        });

    }

    private void loadResearchers(Context context) {
        final List<String> researchers = sm.getAllResearchers();
        Spinner researcherSpinner = (Spinner) findViewById(R.id.researcher_spinner);

        ArrayAdapter<String> researcherSpinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, researchers);
        researcherSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        researcherSpinner.setAdapter(researcherSpinnerAdapter);

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

        codeSpinner.post(new Runnable() {
            @Override
            public void run() {
                String activeClinic = sm.getActiveClinic();

                if (null != activeClinic) {
                    for (int i = 0; i < codes.size(); i++) {
                        if (codes.get(i).equals(activeClinic)) {
                            codeSpinner.setSelection(i);
                            Log.d(TAG, "Setting selection of spinner: " + codes.get(i));
                        }
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
        View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.popup));
        TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
        popupText.setText(textToShow);
        Button popupButton = (Button) layout.findViewById(R.id.popup_close);
        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    /**
     * Shows popup window to add researcher.
     *
     * @param view
     */
    public void addNewResearcher(final Context context, View view) {
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.editable_popup, (ViewGroup) findViewById(R.id.editable_popup));

        TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
        popupText.setText("Add new researcher:");

        final EditText popupEditText = (EditText) layout.findViewById(R.id.popup_edit_text);
        popupEditText.setHint("e.g. Dr. Abc");

        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        Button popupCloseButton = (Button) layout.findViewById(R.id.popup_close);
        popupCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        Button popupSaveButton = (Button) layout.findViewById(R.id.popup_save);
        popupSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String newResearcher = popupEditText.getText().toString();
                    Log.i(TAG, "Adding new researcher: " + newResearcher);
                    sm.addResearcher(newResearcher);
                    loadResearchers(context);
                    popup.dismiss();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
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
        View layout = inflater.inflate(R.layout.editable_popup, (ViewGroup) findViewById(R.id.editable_popup));

        TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
        popupText.setText("Add new clinic code:");

        final EditText popupEditText = (EditText) layout.findViewById(R.id.popup_edit_text);
        popupEditText.setHint("e.g. STA");

        final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        Button popupCloseButton = (Button) layout.findViewById(R.id.popup_close);
        popupCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        Button popupSaveButton = (Button) layout.findViewById(R.id.popup_save);
        popupSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String newCode = popupEditText.getText().toString();
                    Log.i(TAG, "Adding new clinic code: " + newCode);
                    sm.addClinicID(newCode);
                    loadClinicCodes(context);
                    popup.dismiss();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
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
        sm.close();
        MainActivity.this.startActivity(studyIntent);

        Intent syncServiceIntent = new Intent(MainActivity.this, NetworkSyncService.class);
        MainActivity.this.startService(syncServiceIntent);
    }

    /**
     * Shows settings fragment.
     *
     * @param view - caller
     */
    public void showSettings(View view) {
        if (view.getId() == R.id.show_settings) {
            setContentView(R.layout.fragment_settings);
            NumberPicker picker = (NumberPicker) findViewById(R.id.trial_num_picker);
            picker.setValue(Study.numTrials);
            picker.setMaxValue(getResources().getInteger(R.integer.max_trials));
            picker.setMinValue(getResources().getInteger(R.integer.min_trials));
            picker.setValue(getResources().getInteger(R.integer.default_trials));
            picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    Study.setNumTrials(newVal);
                }
            });
        }
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
     * Initialises settings.
     */
    private void initSettings() {
        try {
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
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        // set fixed rotation of tablet
        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);

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
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Restores settings to same as before activity.
     */
    private void restoreSettings() {
        // set fixed rotation of tablet
        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, accelerometerSetting);
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, userSetting);

        // set fixed brightness of screen
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screenBrightnessModeSetting);
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessSetting);

        // set to keep screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set full screen immersive mode
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibilitySetting);

    }

}
