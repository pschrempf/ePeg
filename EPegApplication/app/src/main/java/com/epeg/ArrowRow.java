package com.epeg;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Models a row of arrows.
 * Created by pschrempf on 4/28/2016.
 */
public class ArrowRow extends LinearLayout {

    private ImageView[] arrows;
    private boolean up;

    /**
     * Constructor for top row.
     * @param context
     */
    public ArrowRow(Context context) {
        super(context);
        up = true;
        init(context, false);
    }

    /**
     * Constructor using set attributes.
     * @param context
     * @param attrs
     */
    public ArrowRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        up = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "up", true);
        boolean showAll = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "showAll", false);
        init(context, showAll);
    }

    public ArrowRow(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    /**
     * Initialise images.
     * @param context
     * @param showAll
     */
    public void init(Context context, boolean showAll) {
        arrows = new ImageView[getResources().getInteger(R.integer.num_pegs)];

        for (int i = 0; i < arrows.length; i++) {
            arrows[i] = new ImageView(context);

            if (!showAll) arrows[i].setVisibility(View.INVISIBLE);

            arrows[i].setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));

            int backgr_id = up ? R.drawable.arrow_up : R.drawable.arrow_down;
            arrows[i].setBackground(getResources().getDrawable(backgr_id));

            addView(arrows[i]);
        }
    }

    /**
     * Gets the image with the given index.
     * @param index
     * @return
     */
    public ImageView get(int index) {
        try {
            return arrows[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
