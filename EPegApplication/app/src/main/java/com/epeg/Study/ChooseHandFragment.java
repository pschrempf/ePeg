package com.epeg.Study;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epeg.R;
import com.epeg.SocketIOHandler;

import org.json.JSONException;

/**
 * Fragment that lets a user choose left or right handed.
 *
 * Created by pschrempf on 17/03/16.
 */
public class ChooseHandFragment extends Fragment {

    private static final String TAG = ChooseHandFragment.class.getName();
    Button leftHandButton;
    Button rightHandButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_hand, container, false);

        // Add the participant id label
        TextView participantCodeTextView = (TextView) view.findViewById(R.id.choose_hand_participant_id);
        participantCodeTextView.setText(getResources().getString(R.string.participant_code, getArguments().getString("label")));

        leftHandButton = (Button) view.findViewById(R.id.left_hand_button);
        rightHandButton = (Button) view.findViewById(R.id.right_hand_button);

        // Add the appropriate hand choosing method
        leftHandButton.setOnClickListener(v -> {
            StudyActivity parent = (StudyActivity) getActivity();

            Study.getParticipant().setIsRightHanded(false);

            if (parent.isSinglePlayer())
                parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.LANDING_SCREEN);
            else {
                parent.waitForOtherPlayer(StudyActivity.STUDY_FRAG_TAG.LANDING_SCREEN);
            }

            try {
                SocketIOHandler.sendMessage(StudyActivity.STUDY_REQ.DISPLAY_READ, Study.getParticipant().jsonify());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        rightHandButton.setOnClickListener(v -> {
            StudyActivity parent = (StudyActivity) getActivity();

            Study.getParticipant().setIsRightHanded(true);

            if (parent.isSinglePlayer())
                parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.LANDING_SCREEN);
            else
                parent.waitForOtherPlayer(StudyActivity.STUDY_FRAG_TAG.LANDING_SCREEN);

            try {
                SocketIOHandler.sendMessage(StudyActivity.STUDY_REQ.DISPLAY_READ, Study.getParticipant().jsonify());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        return view;
    }
}
