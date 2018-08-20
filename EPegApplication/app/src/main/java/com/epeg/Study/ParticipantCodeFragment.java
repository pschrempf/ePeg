package com.epeg.Study;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epeg.R;

public class ParticipantCodeFragment extends Fragment {

    Button doneButton;
    TextView participantCodeTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_participant_code, container, false);

        doneButton = (Button) view.findViewById(R.id.start_choose_hand);
        participantCodeTextView = (TextView) view.findViewById(R.id.participant_code);
        participantCodeTextView.setText(getResources().getString(R.string.participant_code, getArguments().getString("label")));

        doneButton.setOnClickListener((v) -> {
            ((StudyActivity) getActivity()).setStudyFragment(StudyActivity.STUDY_FRAG_TAG.CHOOSE_HAND);
        });

        return view;
    }
}
