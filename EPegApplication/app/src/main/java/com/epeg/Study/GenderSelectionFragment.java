package com.epeg.Study;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epeg.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GenderSelectionFragment extends Fragment {


    public GenderSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gender_selection, container, false);

        // Add the participant id label
        TextView participantCodeTextView = (TextView) view.findViewById(R.id.gender_selection_participant_id);
        participantCodeTextView.setText(getResources().getString(R.string.participant_code, getArguments().getString("label")));

        Button maleBtn = (Button) view.findViewById(R.id.male_selected_btn);

        maleBtn.setOnClickListener(l -> {
            StudyActivity parent = (StudyActivity) getActivity();

            assert Study.getParticipant() != null;
            Study.getParticipant().setGender(Participant.Gender.MALE);

            parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.CHOOSE_HAND);
        });

        Button femaleBtn = (Button) view.findViewById(R.id.female_selected_btn);

        femaleBtn.setOnClickListener(l -> {
            StudyActivity parent = (StudyActivity) getActivity();

            assert Study.getParticipant() != null;
            Study.getParticipant().setGender(Participant.Gender.FEMALE);

            parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.CHOOSE_HAND);
        });

        Button noGenderBtn = (Button) view.findViewById(R.id.no_gender_selected_btn);

        noGenderBtn.setOnClickListener(l -> {
            StudyActivity parent = (StudyActivity) getActivity();

            assert Study.getParticipant() != null;
            Study.getParticipant().setGender(Participant.Gender.NO_INFO);

            parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.CHOOSE_HAND);
        });

        return view;
    }

}
