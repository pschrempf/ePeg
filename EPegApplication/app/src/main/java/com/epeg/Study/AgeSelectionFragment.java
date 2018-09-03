package com.epeg.Study;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.epeg.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AgeSelectionFragment extends Fragment {


    public AgeSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_age_selection, container, false);

        // Add the participant id label
        TextView participantCodeTextView = (TextView) view.findViewById(R.id.age_selection_participant_id);
        participantCodeTextView.setText(getResources().getString(R.string.participant_code, getArguments().getString("label")));

        NumberPicker np = (NumberPicker) view.findViewById(R.id.age_picker);

        np.setMinValue(6);
        np.setMaxValue(100);

        np.setValue(14);

        Button ageSelectedBtn = (Button) view.findViewById(R.id.age_selected_button);

        ageSelectedBtn.setOnClickListener(l -> {
            StudyActivity parent = (StudyActivity) getActivity();

            if (Study.getParticipant() != null) Study.getParticipant().setAge(np.getValue());

            parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.CHOOSE_GENDER);
        });

        return view;
    }

}
