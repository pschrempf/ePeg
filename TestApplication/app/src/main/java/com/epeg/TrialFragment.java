package com.epeg;

import android.app.Fragment;
import android.content.res.Resources;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment that carries out one trial.
 *
 * Created by pschrempf on 15/03/16.
 */
public class TrialFragment extends Fragment {

    // Activity and resources
    private StudyActivity activity;

    private PegList pegs;
    private Peg currentPeg;
    private TextView status;
    private Trial currentTrial;
    private boolean pegLifted;

    // Constants
    private static int numPegs;
    static int numTrials;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // set activity
        activity = (StudyActivity) getActivity();
        activity.setContentView(R.layout.fragment_trial);

        // set default variables
        Resources res = getResources();
        numPegs = res.getInteger(R.integer.num_pegs);
        numTrials = res.getInteger(R.integer.num_trials);
        pegLifted = false;

        return inflater.inflate(R.layout.fragment_trial, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add status text
        status = (TextView) activity.findViewById(R.id.trial_status);
        status.setText(Study.getParticipant.getLabel() + ": " + getResources().getString(R.string.start));

        PegRow pegRowTop = (PegRow) activity.findViewById(R.id.top_row_pegs);
        PegRow pegRowBottom = (PegRow) activity.findViewById(R.id.bottom_row_pegs);
        ArrowRow arrowRowTop = (ArrowRow) activity.findViewById(R.id.top_row_arrows);
        ArrowRow arrowRowBottom = (ArrowRow) activity.findViewById(R.id.bottom_row_arrows);

        // hide hand
        ImageView hand;
        if (activity.isLeftToRight()) {
            hand = (ImageView) activity.findViewById(R.id.right_hand);
            hand.setVisibility(View.INVISIBLE);
        } else {
            hand = (ImageView) activity.findViewById(R.id.left_hand);
            hand.setVisibility(View.INVISIBLE);
        }

        // initialise pegs
        initPegs(pegRowTop.getPegs(), pegRowBottom.getPegs(), arrowRowTop, arrowRowBottom);

        // set current peg
        currentPeg = pegs.getHead();
        currentPeg.showArrow(true);
        currentTrial = new Trial(numPegs, 1, activity.isLeftToRight());

    }

    /**
     * Initialises all peg buttons.
     */
    private void initPegs(PegList pegsTop, PegList pegsBottom, ArrowRow arrowsTop, ArrowRow arrowsBottom) {
        pegs = new PegList();

        Peg top = pegsTop.getHead();
        Peg bottom = pegsBottom.getHead();

        // onclick listener for pegs is the same for each peg
        View.OnClickListener pegClicker = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Peg peg = (Peg) v;
                try {

                    // Skipped a peg
                    if (peg.equals(currentPeg.next)) {
                        currentPeg = currentPeg.next;
                        if (pegLifted)
                            currentTrial.nextPegReleased();
                        else
                            currentTrial.nextPegLifted();

                        pegLifted = !pegLifted;
                    }

                    // Incorrect peg lifted
                    if (!peg.equals(currentPeg)) {
                        //status.setText(getResources().getString(R.string.wrong_peg));
                        //activity.endTrial(currentTrial); // call this if trial should end on error

                    // First peg lifted
                    } else if (peg.equals(pegs.getHead())) {
                        currentTrial.start();
                        currentTrial.nextPegLifted();
                        pegLifted = true;
                        status.setText(Study.getParticipant.getLabel() + ": " + getResources().getString(R.string.start));

                        // Jump to next peg
                        currentPeg.showArrow(false);
                        currentPeg = currentPeg.next;

                        // Last peg released
                    } else if (peg.equals(pegs.getTail())) {
                        currentTrial.nextPegReleased();
                        pegLifted = false;
                        currentTrial.stop();
                        activity.endTrial(currentTrial);

                    // Correct peg lifted - update trial
                    } else {

                        if (pegLifted)
                            currentTrial.nextPegReleased();
                        else
                            currentTrial.nextPegLifted();
                        pegLifted = !pegLifted;

                        //status.setText(getString(R.string.trial_in_progress));

                        // Jump to next peg
                        currentPeg.showArrow(false);
                        currentPeg = currentPeg.next;

                    }

                } catch (TrialFailureException e) {
                    e.printStackTrace();
                    //status.setText(e.getMessage());
                    activity.endTrial(currentTrial);
                }
            }
        };

        // set onClicks and create peg list
        for (int i = 0; i < numPegs; i++) {
            top.setOnClickListener(pegClicker);
            bottom.setOnClickListener(pegClicker);

            Peg nextTop = top.next;
            Peg nextBottom = bottom.next;

            // add pegs to pegList in the correct order
            if (activity.isLeftToRight()) {
                pegs.addLastPeg(top);
                pegs.addLastPeg(bottom);
            } else {
                pegs.addFirstPeg(bottom);
                pegs.addFirstPeg(top);
            }

            top.setArrow(arrowsTop.get(i));
            bottom.setArrow(arrowsBottom.get(i));

            top = nextTop;
            bottom = nextBottom;
        }
    }

}
