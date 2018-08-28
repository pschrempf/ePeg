package com.epeg.Study;

import android.support.v4.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.epeg.R;

/**
 * Fragment that carries out one trial.
 *
 * Created by pschrempf on 15/03/16.
 */
public class TrialFragment extends Fragment {

    private StudyActivity parent;
    private View trialLayout;

    private PegList pegs;
    private Peg currentPeg;
    private TextView status;
    private Trial currentTrial;
    private boolean pegLifted;
    private boolean running;
    private boolean completed;

    // Constants
    private static int numPegs;
    static int numTrials;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        parent = (StudyActivity) getActivity();

        trialLayout = inflater.inflate(R.layout.fragment_trial, container, false);

        // set default variables
        Resources res = getResources();
        numPegs = res.getInteger(R.integer.num_pegs);
        numTrials = res.getInteger(R.integer.num_trials);
        pegLifted = false;
        running = false;
        completed = false;

        return trialLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // This check is needed, because at the end it might be the case that the Study is concluded,
        // so it is null, but the ViewPager still wants to load this fragment
        if (!Study.isStudyUnderway()) return;

        // Add status text
        status = (TextView) trialLayout.findViewById(R.id.trial_status);
        status.setText(Study.getParticipant().getLabel() + ": " + getResources().getString(R.string.start));

        PegRow pegRowTop = (PegRow) trialLayout.findViewById(R.id.top_row_pegs);
        PegRow pegRowBottom = (PegRow) trialLayout.findViewById(R.id.bottom_row_pegs);
        ArrowRow arrowRowTop = (ArrowRow) trialLayout.findViewById(R.id.top_row_arrows);
        ArrowRow arrowRowBottom = (ArrowRow) trialLayout.findViewById(R.id.bottom_row_arrows);



        // hide hand
        ImageView hand;
        if (parent.isLeftToRight()) {
            hand = (ImageView) trialLayout.findViewById(R.id.right_hand);
            hand.setVisibility(View.INVISIBLE);
        } else {
            hand = (ImageView) trialLayout.findViewById(R.id.left_hand);
            hand.setVisibility(View.INVISIBLE);
        }

        // initialise pegs
        initPegs(pegRowTop.getPegs(), pegRowBottom.getPegs(), arrowRowTop, arrowRowBottom);

        // set current peg
        currentPeg = pegs.getHead();
        currentPeg.showArrow(true);
        currentTrial = new Trial(numPegs, 1, parent.isLeftToRight());

    }

    /**
     * Initialises all peg buttons.
     */
    private void initPegs(PegList pegsTop, PegList pegsBottom, ArrowRow arrowsTop, ArrowRow arrowsBottom) {
        pegs = new PegList();

        Peg top = pegsTop.getHead();
        Peg bottom = pegsBottom.getHead();

        // ontouch listener for pegs is the same for each peg
        View.OnTouchListener pegTouch = (v, event) -> {
            Peg peg = (Peg) v;
            try {
                touchPeg(peg);
                return true;
            } catch (TrialFailureException e) {
                status.setText("Please restart the trial!");
                return false;
            }
        };

        // onclick listener for pegs is the same for each peg
        View.OnClickListener pegClick = v -> {
            Peg peg = (Peg) v;
            try {
                touchPeg(peg);
            } catch (TrialFailureException e) {
                status.setText("Please restart the trial!");
            }
        };

        // set onClicks, onTouches and create peg list
        for (int i = 0; i < numPegs; i++) {
            top.setOnClickListener(pegClick);
            bottom.setOnClickListener(pegClick);

            top.setOnTouchListener(pegTouch);
            bottom.setOnTouchListener(pegTouch);

            Peg nextTop = top.next;
            Peg nextBottom = bottom.next;

            // add pegs to pegList in the correct order
            if (parent.isLeftToRight()) {
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

        // set peg indices
        Peg curr = pegs.getHead();
        int index = 0;
        while (!curr.equals(pegs.getTail())) {
            curr.setIndex(index++);
            curr = curr.next;
        }
        curr.setIndex(index);

    }

    /**
     * Processes a peg being touched or clicked.
     *
     * @param peg - peg to be processed
     * @throws TrialFailureException
     */
    private void touchPeg(Peg peg) throws TrialFailureException {
        if (running) {
            // Last peg released
            if (peg.equals(pegs.getTail())) {
                if (!completed)
                    throw new TrialFailureException("Necessary pegs not registered!");
                currentTrial.nextPegReleased();
                pegLifted = false;
                currentTrial.stop();
                parent.endTrial(currentTrial);

            // Process other pegs
            } else {
                if (peg.getIndex() == 12) {
                    completed = true;
                }
                int dest = peg.getIndex();

                // Jump to next peg
                while (currentPeg != null && currentPeg.getIndex() <= dest) {
                    if (pegLifted) {
                        currentTrial.nextPegReleased();
                    } else {
                        currentTrial.nextPegLifted();
                    }
                    pegLifted = !pegLifted;
                    currentPeg = currentPeg.next;
                }

            }
        }

        // First peg lifted
        else if (peg.equals(pegs.getHead())) {
            currentTrial.start();
            currentTrial.nextPegLifted();
            pegLifted = true;
            running = true;
            status.setText(Study.getParticipant().getLabel() + ": " + getResources().getString(R.string.start));

            // Jump to next peg
            currentPeg.showArrow(false);
            currentPeg = currentPeg.next;

        }

    }

}
