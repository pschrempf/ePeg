package com.epeg.Study;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;

import com.epeg.R;

/**
 * Created by pschrempf on 15/03/16.
 *
 * Class extending Button that represents where a peg is placed on the screen (also contains basic linked list implementation).
 */
public class Peg extends android.support.v7.widget.AppCompatButton {

    // index
    int index;

    // next peg in list
    Peg next;

    // arrow pointing to that peg
    ImageView arrow;

    /**
     * Constructs a peg in the given application context.
     *
     * @param context - application context of peg
     */
    public Peg(Context context) {
        super(context);
        this.setHeight(getResources().getInteger(R.integer.peg_height));
        this.setWidth(getResources().getInteger(R.integer.peg_width));


        final TableLayout.LayoutParams pegParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1);

        // This will separate the buttons from each other
        pegParams.setMarginStart(2);
        pegParams.setMarginEnd(2);

        this.setLayoutParams(pegParams);
    }

    /**
     * Constructs a Peg with a layout and onClickListener.
     *
     * @param context - application context of peg
     * @param layoutParams - layout parameters for peg
     * @param onClickListener - OnClickListener for peg
     */
    public Peg(Context context, TableLayout.LayoutParams layoutParams, OnClickListener onClickListener) {
        this(context);
        this.setLayoutParams(layoutParams);
        this.setOnClickListener(onClickListener);
        next = null;
    }

    /**
     * Set the index of the peg.
     *
     * @param index - index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the peg's index.
     *
     * @return index of the peg
     */
    public int getIndex() {
        return this.index;
    }
    /**
     * Sets the arrow corresponding to the peg.
     * @param arrow - ImageView of arrow
     */
    public void setArrow(ImageView arrow) {
        this.arrow = arrow;
    }

    /**
     * Shows/hides the arrow that is connected to the peg.
     * @param show - whether to show or hide the arrow
     */
    public void showArrow(boolean show) {
        if (arrow != null) {
            if (show) arrow.setVisibility(View.VISIBLE);
            else arrow.setVisibility(View.INVISIBLE);
        }
    }

}
