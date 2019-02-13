package com.epeg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Timestamp;

/**
 * Created by gregory on 15/03/16.
 *
 * Represents a single trial, that is taking all the pegs from the top row to the bottom row.
 */
public class Trial {
    //Tag for logs
    public static final String TAG = "TRIAL";

    //Tags for the JSONifier method
    public static final String JSON_TRIAL_ID_TAG = "id";
    public static final String JSON_TRIAL_SUCCESS_TAG = "success";
    public static final String JSON_TRIAL_TRUE_TAG = "success";
    public static final String JSON_TRIAL_FALSE_TAG = "failed";
    public static final String JSON_HAND_USED_TAG = "handUsed";
    public static final String JSON_RIGHT_HAND_USED = "right";
    public static final String JSON_LEFT_HAND_USED = "left";
    public static final String JSON_EXTRAS_TAG = "extras";
    public static final String JSON_MEASUREMENTS_TAG = "measurements";
    public static final String JSON_START_TIME_TAG = "startTime";
    public static final String JSON_END_TIME_TAG = "endTime";
    public static final String JSON_TOTAL_TIME_TAG = "totalTime";
    public static final String JSON_SUM_TIME_TAG = "sumTime";
    public static final String JSON_ACTUAL_TIME_TAG = "actualTime";
    public static final String JSON_PEGS_LIFTED_TAG = "pegsLifted";
    public static final String JSON_PEGS_RELEASED_TAG = "pegsReleased";
    public static final String JSON_PEG_DELTAS_TAG = "pegDeltas";

    //ID of the trial set at the initialisation of the object
    private final long TRIAL_ID;

    //Number of pegs in the top and bottom rows of the frame
    private final int NUM_PEGS;

    //All the data that we want to record in the trial

    //Start of the trial - this is when start() is called
    private Timestamp startTime;

    //End of the trial - this is when stop() is called
    private Timestamp endTime;

    //The individual timestamps of each peg being gripped, released and the deltas for them
    private Timestamp[] pegGripped;
    private Timestamp[] pegReleased;
    private long[] gripDeltas;

    //The total time the pegs spent in the air (sum of the deltas)
    private long sumTime;

    //The time taken between starting and finishing the trial
    private long totalTime;

    //The actual time of the trial, this is counted from when the first peg is lifted and until the last peg is released
    private long actualTime;

    //Used to calculate the delta between the departure and arrival of the current peg between its holes. For internal use only
    private long currentDelta;

    //Index of the current pair of pegs, from left to right. For internal use only
    private int currentPegIndex;

    //Keeps track of whether the current peg has been lifted yet from its hole. Used internally for error checking
    private boolean isPegInAir;

    //Keeps track of whether the trial we are conducting was successful or was failed.
    private boolean isSuccessful;

    //Flag for denoting whether an trial has finished yet or not
    private boolean isFinished;

    //Flag to denote the direction in which the pegs will be transferred from the top row to the bottom row.
    private boolean isLeftToRight;

    //Flag to denote whether a trial is a demo or not
    private boolean isDemo;

    /**
     * Initialises a new {@link Trial} to be recorded.
     *
     * @param isLeftToRight Used to denote the direction in which the pegs will be transferred from the top row to the bottom row.
     */
    public Trial(int num_pegs, long id, boolean isLeftToRight) {

        this.isLeftToRight = isLeftToRight;
        this.TRIAL_ID = id;
        this.NUM_PEGS = num_pegs;

        pegGripped = new Timestamp[NUM_PEGS];
        pegReleased = new Timestamp[NUM_PEGS];

        gripDeltas = new long[NUM_PEGS];

        currentPegIndex = 0;

        sumTime = 0;
        actualTime = 0;
        totalTime = 0;

        setIsSuccessful(false);
        isPegInAir = false;

        setIsFinished(false);
        setIsDemo(false);
    }

    //====================================================================================================
    // Data recording methods
    //====================================================================================================

    /**
     * Start recording the trial
     */
    public void start(){
        long currTime = System.currentTimeMillis();

        setStartTime( currTime );

        totalTime = -currTime;
    }

    /**
     * Finish recording the trial
     * @throws TrialFailureException
     */
    public void stop(){

        setIsSuccessful( currentPegIndex == NUM_PEGS );

        long currTime = System.currentTimeMillis();

        setEndTime(currTime);

        totalTime = getTotalTime() + currTime;

        setIsFinished(true);
    }

    /**
     * Should be called when then next peg in the top row gets lifted from its hole
     * to start recording the data for that pair of holes.
     *
     * @throws TrialFailureException
     */
    public void nextPegLifted() throws TrialFailureException {

        //If we call the method too many times, throw exception
        if (currentPegIndex >= NUM_PEGS)
            throw new TrialFailureException("There are no more pegs left to be lifted!");

        //Make sure that everything is recorded in order
        if(isPegInAir)
            throw new TrialFailureException("The arrival of the previous peg has not been recorded yet!");

        //Set the current timestamp;
        long currTime = System.currentTimeMillis();

        //If we are on the first pair of pegs, start recording for the actual trial time spent
        if(currentPegIndex == 0)
            actualTime = -currTime;

        //Record time of departure
        pegGripped[ isLeftToRight ?
                currentPegIndex :
                NUM_PEGS - currentPegIndex - 1]

                = new Timestamp(currTime);

        //Set time to be subtracted
        currentDelta = -currTime;

        //Acknowledge that the peg has been lifted
        isPegInAir = true;
    }

    /**
     * Should be called when the next peg in the bottom row has been placed to finish recording
     * data for that pair of holes and to move onto the next pair.
     * 
     * @throws TrialFailureException
     */
    public void nextPegReleased() throws TrialFailureException {

        //If we call the method too many times, throw exception
        if (currentPegIndex >= NUM_PEGS)
            throw new TrialFailureException("There are no more pegs left to be lifted!");

        //Make sure that everything is recorded in order
        if(!isPegInAir)
            throw new TrialFailureException("The next peg has not been taken yet!");

        //Set the current timestamp;
        long currTime = System.currentTimeMillis();

        //If we are on the last peg, calculate the total actual time of the trial
        if(currentPegIndex == NUM_PEGS - 1)
            actualTime = getActualTime() + currTime;

        //Record time of arrival
//        pegReleased[isLeftToRight ?
//                currentPegIndex :
//                NUM_PEGS - currentPegIndex - 1]
        pegReleased[currentPegIndex]
                = new Timestamp(currTime);

        //Find the actual delta by adding the current timestamp to the negative of the start (= end - start)
        currentDelta += currTime;

        //Add it to total time of peg transfers
        sumTime = getSumTime() + currentDelta;

        //Record it as an individual delta for that specific pair of pegs
        gripDeltas[isLeftToRight ?
                currentPegIndex :
                NUM_PEGS - currentPegIndex - 1]

                = currentDelta;

        //Move onto the next pair of pegs
        currentPegIndex++;

        //Acknowledge that the peg has arrived
        isPegInAir = false;
    }


    //====================================================================================================
    // Getters & Setters
    //====================================================================================================

    /**
     *
     * @return {@link Timestamp} of the start of the trial
     */
    public Timestamp getStartTime() {
        return startTime;
    }

    /**
     * This method should only be used internally.
     *
     * @param startTime {@link Timestamp} of the start of the trial.
     */
    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    /**
     *
     * @param startTime long UNIX timestamp of the start of the trial.
     */
    public void setStartTime(Long startTime) {
        this.startTime = new Timestamp(startTime);
    }

    /**
     *
     * @return {@link Timestamp} of the end of the trial
     */
    public Timestamp getEndTime() {
        return endTime;
    }

    /**
     * This method should only be used internally.
     *
     * @param endTime {@link Timestamp} of the end of the trial.
     */
    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    /**
     *
     * @param endTime long UNIX timestamp of the end of the trial.
     */
    public void setEndTime(Long endTime) {
        this.endTime = new Timestamp(endTime);
    }

    /**
     * The total time the pegs spent in the air (sum of the deltas)
     * @return Sum of the deltas
     */
    public long getSumTime() {
        return sumTime;
    }

    /**
     * The time taken between starting and finishing the trial
     * @return Time between the calls to start() and stop()
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * The actual time of the trial,
     * this is counted from when the first peg is lifted and until the last peg is released.
     * @return Delta between first peg lifted and last peg released
     */
    public long getActualTime() {
        return actualTime;
    }

    /**
     *
     * @return The trial has finished (stop() has been called)
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     *
     * @param isFinished The trial has concluded
     */
    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public long getID() {
        return TRIAL_ID;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setIsDemo(boolean isDemo) {
        this.isDemo = isDemo;
    }

    //====================================================================================================
    // JSON Builder and toString methods
    //====================================================================================================

    /**
     * Converts the {@link Trial} to a {@link JSONObject}.
     * Can only be called if the trial has finished.
     * For the layout see JSONlayout.txt
     * @return {@link JSONObject} representing of the class.
     * @throws JSONException
     * @throws TrialFailureException if the method was called before the trial had finished.
     */
    public JSONObject jsonify() throws JSONException, TrialFailureException {

        if(!isFinished)
            throw new TrialFailureException("The trial has not finised yet!");

        //Initialise the required JSON objects
        JSONObject trialObj = new JSONObject();

        JSONArray pegsLifted = new JSONArray();
        JSONArray pegsReleased = new JSONArray();
        JSONArray pegDeltas = new JSONArray();

        //Only go until we actually got with the current trial
        for (int i = 0; i < currentPegIndex; i++) {
            pegsLifted.put(this.pegGripped[i]);
            pegsReleased.put(this.pegReleased[i]);
            pegDeltas.put(this.gripDeltas[i]);
        }

        //Create the data structure
        trialObj.put(JSON_TRIAL_ID_TAG, getID())
                .put( JSON_HAND_USED_TAG, isLeftToRight ?
                        JSON_LEFT_HAND_USED : JSON_RIGHT_HAND_USED )
                .put( JSON_TRIAL_SUCCESS_TAG, isSuccessful() ?
                        JSON_TRIAL_TRUE_TAG : JSON_TRIAL_FALSE_TAG )
                .put( JSON_EXTRAS_TAG, "" )
                .put(JSON_MEASUREMENTS_TAG, new JSONObject()
                                .put(JSON_START_TIME_TAG, getStartTime().getTime())
                                .put(JSON_END_TIME_TAG, getEndTime().getTime())
                                .put(JSON_TOTAL_TIME_TAG, getTotalTime())
                                .put(JSON_SUM_TIME_TAG, getSumTime())
                                .put(JSON_ACTUAL_TIME_TAG, getActualTime())
                                .put(JSON_PEGS_LIFTED_TAG, pegsLifted)
                                .put(JSON_PEGS_RELEASED_TAG, pegsReleased)
                                .put(JSON_PEG_DELTAS_TAG, pegDeltas)
                );

        return trialObj;
    }

    /**
     * Formulates a JSON String from the class's jsonify() method
     * @return JSON string of the trial
     * @throws JSONException
     * @throws TrialFailureException
     */
    public String toJsonString() throws JSONException, TrialFailureException {
        return jsonify().toString();
    }


}
