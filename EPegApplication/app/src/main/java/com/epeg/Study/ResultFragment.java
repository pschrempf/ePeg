package com.epeg.Study;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.epeg.MainActivity;
import com.epeg.R;
import com.epeg.SocketIOHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment that is called at the end of a study.
 *
 * Created by pschrempf on 27/03/16.
 */
public class ResultFragment extends Fragment {

    private static final String TAG = ResultFragment.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        // Add the participant id label
        TextView participantCodeTextView = (TextView) view.findViewById(R.id.result_participant_id);
        participantCodeTextView.setText(getResources().getString(R.string.participant_code, getArguments().getString("label")));

        Button resultsViewedButton = (Button) view.findViewById(R.id.end_study_button);

        resultsViewedButton.setOnClickListener(v -> {

            // This is needed so that the server doesn't think that we just randomly disconnected.
            JSONObject extra = new JSONObject();
            try {
                extra.put("reason", "finished");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            SocketIOHandler.sendMessage(StudyActivity.STUDY_REQ.GAME_RESET, extra);

            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        return view;
    }

}
