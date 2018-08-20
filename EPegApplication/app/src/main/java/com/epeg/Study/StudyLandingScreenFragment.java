package com.epeg.Study;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epeg.R;

public class StudyLandingScreenFragment extends Fragment {

    Button startTrialButton;
    Button startDemoButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_study_landing_screen, container, false);

        // Add the click listeners
        startTrialButton = (Button) view.findViewById(R.id.start_trial);
        startDemoButton = (Button) view.findViewById(R.id.start_demo);

        startTrialButton.setOnClickListener((v) -> ((StudyActivity)getActivity()).setStudyFragment(StudyActivity.STUDY_FRAG_TAG.TRIAL));
        startDemoButton.setOnClickListener((v) -> ((StudyActivity)getActivity()).setStudyFragment(StudyActivity.STUDY_FRAG_TAG.TRIAL));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        TextView info = (TextView) getActivity().findViewById(R.id.trial_status);
        info.setText(getResources().getString(R.string.trial_status, Study.getCurrentTrialIndex(), Study.numTrials * 2));
    }
}
