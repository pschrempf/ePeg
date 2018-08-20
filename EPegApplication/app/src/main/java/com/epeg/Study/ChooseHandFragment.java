package com.epeg.Study;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.epeg.R;

/**
 * Fragment that lets a user choose left or right handed.
 *
 * Created by pschrempf on 17/03/16.
 */
public class ChooseHandFragment extends Fragment {

    Button leftHandButton;
    Button rightHandButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_hand, container, false);

        leftHandButton = (Button) view.findViewById(R.id.left_hand_button);
        rightHandButton = (Button) view.findViewById(R.id.right_hand_button);

        // Add the appropriate hand choosing method
        leftHandButton.setOnClickListener(v -> {
            StudyActivity parent = (StudyActivity) getActivity();

            Study.getParticipant().setIsRightHanded(true);

            parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.LANDING_SCREEN);
            parent.sendMessage(R.integer.REQ_DISPLAY_READ, null);
        });

        return view;
    }
}
