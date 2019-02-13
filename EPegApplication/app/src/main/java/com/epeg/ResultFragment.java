package com.epeg;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        final Activity activity = getActivity();
        activity.setContentView(R.layout.fragment_result);

        // Conclude study if possible
        try {
            Study.conclude();
            Log.d(TAG, "Study concluded!");

//            Intent syncServiceIntent = new Intent(activity, NetworkSyncService.class);
//            activity.startService(syncServiceIntent);
        } catch (StudyException e) {
            Log.e(TAG, "Could not conclude study! Error: " + e.getMessage());
            Study.cancel();
            Log.d(TAG, "Study cancelled!");
        }

        // sync with network
//        Intent syncServiceIntent = new Intent(this.getActivity(), NetworkSyncService.class);
//        this.getActivity().startService(syncServiceIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }
}
