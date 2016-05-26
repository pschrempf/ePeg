package com.epeg;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Models a row of pegs as a LinearLayout.
 *
 * Created by pschrempf on 4/27/2016.
 */
public class PegRow extends LinearLayout {

    private PegList pegs;

    public PegRow(Context context) {
        super(context);
        init(context);
    }

    public PegRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PegRow(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    /**
     * Initialises the pegs.

     * @param context
     */
    public void init(Context context) {
        pegs = new PegList();

        for (int i = 0; i < getResources().getInteger(R.integer.num_pegs); i++) {
            Peg peg = new Peg(context);
            pegs.addLastPeg(peg);
            addView(peg);
            Log.d("PegRow", "adding peg...");
        }
    }

    /**
     * Gets the PegList of pegs.
     *
     * @return PegList
     */
    public PegList getPegs() {
        return pegs;
    }
}
