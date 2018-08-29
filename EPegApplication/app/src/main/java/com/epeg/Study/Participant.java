package com.epeg.Study;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Encompasses information about a participant of a study:
 * <ul>
 *     <li>id</li>
 *     <li>label (for later use with the barcode)</li>
 *     <li>dominant hand</li>
 *     <li>age</li>
 *     <li>gender</li>
 * </ul>
 *
 * Created by gregory on 02/04/16.
 */
public class Participant {

    //Tags for JSON
    public static final String JSON_PARTICIPANT_ID_TAG = "id";
    public static final String JSON_LABEL_TAG = "label";
    public static final String JSON_DOM_HAND_TAG = "dom_hand";
    public static final String JSON_HAND_RIGHT_TAG = "right";
    public static final String JSON_HAND_LEFT_TAG = "left";
    public static final String JSON_AGE_TAG = "age";
    public static final String JSON_GENDER_TAG = "gender";
    public static final String JSON_MALE_TAG = "male";
    public static final String JSON_FEMALE_TAG = "female";
    public static final String JSON_NO_GENDER_TAG = "prefer not to say";

    enum Gender{
        MALE, FEMALE, NO_INFO
    }

    private long id;

    private String label;

    // Age of the participant
    private int age;

    private String gender;

    private boolean isRightHanded;

    public Participant(String label){
        setLabel(label);
        id = 0;
        age = 0;
        gender = "not recorded yet!";
        isRightHanded = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRightHanded() {
        return isRightHanded;
    }

    public void setIsRightHanded(boolean isRightHanded) {
        this.isRightHanded = isRightHanded;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(Gender gender) {

        switch (gender){
            case MALE: this.gender = JSON_MALE_TAG; break;
            case FEMALE: this.gender = JSON_FEMALE_TAG; break;
            case NO_INFO: this.gender = JSON_NO_GENDER_TAG; break;
        }
    }

    public JSONObject jsonify() throws JSONException {

        return new JSONObject()
                .put( JSON_PARTICIPANT_ID_TAG, getId() )
                .put( JSON_LABEL_TAG, getLabel() )
                .put( JSON_AGE_TAG, age )
                .put( JSON_GENDER_TAG, gender )
                .put( JSON_DOM_HAND_TAG, isRightHanded() ?
                                    JSON_HAND_RIGHT_TAG : JSON_HAND_LEFT_TAG );
    }
}
