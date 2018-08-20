package com.epeg.Study;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.epeg.R;

/**
 * Fragment that checks the correct setup of the pegs.
 *
 * Created by pschrempf on 19/03/16.
 */
public class SetupFragment extends Fragment {

    Button setupCompletedBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, container, false);

        setupCompletedBtn = (Button) view.findViewById(R.id.setup_complete);

        // Add onclick listener
        setupCompletedBtn.setOnClickListener((v) -> ((StudyActivity) getActivity()).setStudyFragment(StudyActivity.STUDY_FRAG_TAG.TRIAL));

        return view;
    }

}
