package com.epeg.Study;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.epeg.R;
import com.epeg.SocketIOHandler;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreResultFragment extends Fragment {


    public PreResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pre_result, container, false);

        Button getResultsButton = (Button) view.findViewById(R.id.get_results_button);

        getResultsButton.setOnClickListener(l -> {
            SocketIOHandler.sendMessage(StudyActivity.STUDY_REQ.EXPERIMENT_DONE, null);
            SocketIOHandler.setResponseFunction(() -> {}, -1);

            StudyActivity parent = (StudyActivity) getActivity();

            parent.setStudyFragment(StudyActivity.STUDY_FRAG_TAG.RESULTS);
        });

        return view;
    }

}
