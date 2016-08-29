package com.epeg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
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

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SettingsManager sm;

    private int accelerometerSetting;
    private int userSetting;
    private int screenBrightnessModeSetting;
    private int screenBrightnessSetting;
    private int systemUiVisibilitySetting;

    String researcher = "Dr. Silvia Paracchini";

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
     * Shows researcher settings fragment.
     * @param context
     */
    private void researcherSettings(Context context) {
        setContentView(R.layout.fragment_researcher);

        // initialising researcher spinner

        final List<String> researchers = sm.getAllResearchers();
        researchers.add(getResources().getString(R.string.add_new));

        Spinner researcherSpinner = (Spinner) findViewById(R.id.researcher_spinner);
        final EditText researcherNew = (EditText) findViewById(R.id.researcher_new);

        ArrayAdapter<String> researcherSpinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, researchers);
        researcherSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        researcherSpinner.setAdapter(researcherSpinnerAdapter);

        researcherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String researcher = (String) parent.getItemAtPosition(position);
                if (researcher.equals(getResources().getString(R.string.add_new))) {
                    researcherNew.setVisibility(View.VISIBLE);
                } else {
                    // hide textbox
                    researcherNew.setVisibility(View.GONE);
                    researcherNew.getEditableText().clear();
                    // set active researcher
                    Log.d(TAG, "setting active researcher: " + researcher);
                    sm.setActiveResearcher(researcher);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // initialising clinic code spinner

        final List<String> codes = sm.getAllClinicIDs();
        codes.add(getResources().getString(R.string.add_new));

        Spinner codeSpinner = (Spinner) findViewById(R.id.clinic_code_spinner);
        final EditText codeNew = (EditText) findViewById(R.id.clinic_code_new);

        ArrayAdapter<String> codeSpinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, codes);
        codeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        codeSpinner.setAdapter(codeSpinnerAdapter);

        codeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String clinicCode = (String) parent.getItemAtPosition(position);
                if (clinicCode.equals(getResources().getString(R.string.add_new))) {
                    codeNew.setVisibility(View.VISIBLE);
                } else {
                    codeNew.setVisibility(View.GONE);
                    codeNew.getEditableText().clear();
                    // set active clinic
                    Log.d(TAG, "setting active clinic: " + clinicCode);
                    sm.setActiveClinic(clinicCode);
                }
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
           // check researcher textbox entry
           EditText newResearcher = (EditText) findViewById(R.id.researcher_new);
           if (!newResearcher.getText().toString().equals("")) {
               try {
                   String researcher = newResearcher.getText().toString();
                   Log.d(TAG, "adding new researcher: " + researcher);
                   sm.addResearcher(researcher);
                   sm.setActiveResearcher(researcher);
                   this.researcher = researcher;
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }

           // check code textbox entry
           EditText newCode = (EditText) findViewById(R.id.clinic_code_new);
           if (!newCode.getText().toString().equals("")) {
               try {
                   String code = newCode.getText().toString();
                   code = code.toUpperCase();                    // make sure code is upper case
                   Log.d(TAG, "adding new clinic code: " + code);
                   sm.addClinicID(code);
                   sm.setActiveClinic(code);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }

           // check that we have active clinic and researcher
           if (sm.getActiveClinic() != null && sm.getActiveResearcher() != null) {
               // commence with main activity
               setContentView(R.layout.activity_main);
           } else {
               // show popup window if researcher of clinic needs to be set
               LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               View layout = inflater.inflate(R.layout.popup, (ViewGroup) findViewById(R.id.popup));
               TextView popupText = (TextView) layout.findViewById(R.id.popup_text);
               popupText.setText(getResources().getString(R.string.researcher_settings_popup_prompt));
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
       }
   }

    /**
     * Starts study and syncs with network.
     * @param view - caller
     */
    public void startStudy(View view) {
        Intent studyIntent = new Intent(MainActivity.this, StudyActivity.class);
        // studyIntent.putExtra("STUDY_RESEARCHER", sm.getActiveResearcher());
        sm.close();
        MainActivity.this.startActivity(studyIntent);

        Intent syncServiceIntent = new Intent(MainActivity.this, NetworkSyncService.class);
        MainActivity.this.startService(syncServiceIntent);
    }

    /**
     * Shows settings fragment.
     * @param view - caller
     */
    public void showSettings(View view) {
        if (view.getId() == R.id.show_settings) {
            setContentView(R.layout.fragment_settings);
            NumberPicker picker = (NumberPicker) findViewById(R.id.trial_num_picker);
            picker.setValue(Study.numTrials);
            picker.setMaxValue(getResources().getInteger(R.integer.max_trials));
            picker.setMinValue(getResources().getInteger(R.integer.min_trials));
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
                    Intent settingsIntent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
                }
            }
            accelerometerSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            userSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.USER_ROTATION);
            screenBrightnessModeSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            screenBrightnessSetting = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            systemUiVisibilitySetting = getWindow().getDecorView().getSystemUiVisibility();
        } catch (Settings.SettingNotFoundException e) {
            //ignore
        }

        // set fixed rotation of tablet
        Settings.System.putInt(this.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);

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
