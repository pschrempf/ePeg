package com.epeg.Study;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epeg.R;

/**
 * Fragment that lets a user choose left or right handed.
 *
 * Created by pschrempf on 17/03/16.
 */
public class ChooseHandFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_hand, container, false);

        return view;
    }
}
