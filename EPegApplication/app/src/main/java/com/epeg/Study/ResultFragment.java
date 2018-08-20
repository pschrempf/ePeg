package com.epeg.Study;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epeg.R;

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

        // Conclude study if possible
        try {
            Study.conclude();
            Log.d(TAG, "Study concluded!");

        } catch (StudyException e) {
            Log.e(TAG, "Could not conclude study! Error: " + e.getMessage());
            Study.cancel();
            Log.d(TAG, "Study cancelled!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }
}
