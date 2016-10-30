package com.epeg;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Encompasses the information required for a single study
 *
 * Created by gregory on 17/03/16.
 */
public class Study {

    public static final String TAG = Study.class.getName();

    // Tags for JSON
    public static final String JSON_PARTICIPANT_TAG = "participant";
    public static final String JSON_TRIALS_ARRAY_TAG = "trials";

    // Number of trials to be conducted for each hand (total number is equals to this number times 2)
    public static int numTrials = 5;

    private Participant participant;

    // Array for all trials that will be conducted within this study
    private Trial[] trials;

    // Keeps track of which trial we are on right now
    private int currentTrialIndex;
    private Context context;

    private static Study currentStudy = null;

    private static SecureRandom secureRandom = null;

    /**
     * Private constructor, used in the singleton initialiser.
     */
    private Study( Context context ) {
        participant = null;
        currentTrialIndex = 0;
        trials = new Trial[numTrials * 2];

        setContext(context);
    }

    /**
     * Start a new study.
     *
     * @param context Android context from where the study is started
     * @throws StudyException thrown if there already is an ongoing study
     */
    public static void startNew(Context context) throws StudyException{
        if (secureRandom == null) {
            secureRandom = new SecureRandom();
        }

        if (currentStudy == null) {
            currentStudy = new Study(context);
        }
        else
            throw new StudyException("A study is already under way!");
    }

    /**
     * Concludes an ongoing study and persists it using {@link EPegCryptoDataManager}.
     *
     * @throws StudyException thrown if there is no study to conclude, or if the current study may not be concluded yet.
     */
    public static void conclude() throws StudyException {
        if (null != currentStudy) {

            if (getCurrentTrialIndex() < numTrials * 2)
                throw new StudyException("Not enough trials have been carried out yet to conclude the study!");

            JSONObject studyData = currentStudy.jsonify();

            Log.i(TAG, "Concluding study, saving JSON: " + studyData);

            EPegCryptoDataManager dataManager = new EPegCryptoDataManager( currentStudy.getContext() );

            dataManager.open();

            dataManager.writeStudy(studyData, EPegCryptoDataManager.TMP_DEVICE_ID, EPegCryptoDataManager.TMP_EXP_CONDUCTOR);

            dataManager.close();

            Log.i(TAG, "Study concluded, reset.");
            currentStudy = null;
        }
        else
            throw new StudyException("No study is under way yet!");
    }

    /**
     * Adds the next trial to the current study.
     *
     * @param trial
     * @throws StudyException
     */
    public static void addNextTrial(Trial trial) throws StudyException {
        if (trial != null) {
            if (getCurrentTrialIndex() == numTrials * 2)
                throw new StudyException("Cannot add trial beyond limit of " + (numTrials *2) );
            if (!trial.isFinished())
                throw new StudyException("An unfinished trial may not be added to a study!");

            currentStudy.addTrial(trial);
        }
        else
            throw new StudyException("The provided trial was null");
    }

    /**
     * Cancels the current study and deletes the data gathered up to that point
     */
    public static void cancel() {
        currentStudy = null;
    }

    /**
     * Gets the result string of the current study.
     *
     * @return String containing the results of the current study
     */
    public static String getResultString() {
        return currentStudy.getStudyResultString();
    }

    public static void setNumTrials(int numTrials) {
        Study.numTrials = numTrials;
    }

    public static String generateNewParticipantLabel() throws StudyException {
        SettingsManager settingsManager = new SettingsManager(currentStudy.getContext());
        String clinicID = settingsManager.getActiveClinic();

        if (clinicID == null){
            throw new StudyException("There is no active clinic set, cannot generate participant ID!");
        }


        String generated = new BigInteger(30, secureRandom).toString(32).toUpperCase();

        settingsManager.close();

        String participantID = clinicID + "-" + generated;

        Log.i(TAG, "New Participant ID generated: " + participantID);
        return participantID;
    }

    /**
     * Returns a String representation of the results of a study.
     *
     * @return String containing results of study
     */
    private String getStudyResultString() {
        String results = "";
        for (int i = 0; i < trials.length; i++) {
            results += "Trial " + (i+1) + " ";
            if (trials[i].isFinished()) {
                results += "time: " + trials[i].getActualTime() + " ms\n";
            } else {
                results += "not completed.\n";
            }
        }
        return results;
    }

    /**
     * Creates the defined JSON layout that is specified in DataJSONlayout.txt
     *
     * @return {@link JSONArray} of the whole study
     */
    private JSONObject jsonify() throws StudyException {

        if ( participant == null )
            throw new StudyException("No participant has been set for the study!");

        JSONObject studyObject = new JSONObject();

        try {
            JSONArray trials = new JSONArray();

            for (Trial trial: this.trials) {
                trials.put(trial.jsonify());
            }

            studyObject.put(JSON_PARTICIPANT_TAG, getParticipant().jsonify())
                    .put( JSON_TRIALS_ARRAY_TAG, trials);

        } catch (JSONException | TrialFailureException e) {
            e.printStackTrace();
        }

        return  studyObject;
    }


    //====================================================================================================
    // Getters & Setters
    //====================================================================================================

    public static boolean isFinished() {
        return getCurrentTrialIndex() >= numTrials*2;
    }

    private void addTrial(Trial trial){
        trials[ currentTrialIndex++ ] = trial;
    }

    public static int getCurrentTrialIndex() {
        return currentStudy.currentTrialIndex;
    }

    private Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    public static Participant getParticipant() {
        return currentStudy.participant;
    }

    public static void setParticipant(Participant participant) {
        currentStudy.participant = participant;
    }

}
