package com.epeg;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epeg.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaitingForOtherPlayerFragment extends Fragment {


    public WaitingForOtherPlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_waiting_for_other_player, container, false);
    }

}
