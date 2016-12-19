package com.epeg;

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

    private long id;

    private String label;

    // Age of the participant
    private int age;

    // #feminism
    private boolean isFemale;

    private boolean isRightHanded;

    public Participant(String label){
        setLabel(label);
        id = 0;
        age = 0;
        isFemale = true;
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

    public JSONObject jsonify() throws JSONException {

        return new JSONObject()
                .put( JSON_PARTICIPANT_ID_TAG, getId() )
                .put( JSON_LABEL_TAG, getLabel() )
                .put( JSON_DOM_HAND_TAG, isRightHanded() ?
                                    JSON_HAND_RIGHT_TAG : JSON_HAND_LEFT_TAG );
    }
}
