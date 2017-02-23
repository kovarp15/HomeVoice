/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 18.02.2017.
 * Copyright (c) 2017 Petr Kovář
 *
 * All rights reserved
 * kovarp15@fel.cvut.cz
 * HomeVoice for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HomeVoice for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HomeVoice for Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.kovar.petr.homevoice.frontend.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

public class LocationButton extends Button {

    private static final String LOG_TAG = "RoomButton";

    private int m_width = 200;
    private int m_height = 100;

    private ScaleGestureDetector m_scaleDetector;

    private boolean m_modify = false;

    public LocationButton(Context context) {
        super(context);

        init();
    }

    public LocationButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public LocationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        setBackgroundResource(outValue.resourceId);

        setVisibility(VISIBLE);

        setShadowLayer(10, 0, 0, Color.BLACK);

        setOnTouchListener(new MoveListener());

        m_scaleDetector = new ScaleGestureDetector(getContext(), m_scaleListener);

    }

    public void setDimensions(int aWidth, int aHeight) {
        m_width = aWidth;
        m_height = aHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(m_width, m_height);
    }

    public void setEditMode() {
        m_modify = true;
        setPressed(true);
    }

    public void clearEditMode() {
        m_modify = false;
        setPressed(false);
    }

    public boolean isEdited() {
        return m_modify;
    }

    /**
     * The scale listener, used for handling multi-finger scale gestures.
     */
    private final ScaleGestureDetector.OnScaleGestureListener m_scaleListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private float lastSpanX;
        private float lastSpanY;

        // Detects that new pointers are going down.
        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            lastSpanX = scaleGestureDetector.getCurrentSpanX();
            lastSpanY = scaleGestureDetector.getCurrentSpanY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            float spanX = scaleGestureDetector.getCurrentSpanX();
            float spanY = scaleGestureDetector.getCurrentSpanY();

            float m_scaleX = spanX / lastSpanX;
            float m_scaleY = spanY / lastSpanY;

            m_width = (int) (m_scaleX * m_width);
            m_height = (int) (m_scaleY * m_height);

            lastSpanX = spanX;
            lastSpanY = spanY;

            requestLayout();
            invalidate();

            return true;
        }
    };

    private final class MoveListener implements OnTouchListener {

        private float deltaX;
        private float deltaY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            final int X = (int) motionEvent.getRawX();
            final int Y = (int) motionEvent.getRawY();

            if(m_modify) {

                m_scaleDetector.onTouchEvent(motionEvent);

                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case ACTION_DOWN:
                        Log.e(LOG_TAG, "ACTION DOWN");
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        deltaX = X - params.leftMargin;
                        deltaY = Y - params.topMargin;
                        break;
                    case ACTION_UP:
                        break;
                    case ACTION_POINTER_DOWN:
                        break;
                    case ACTION_POINTER_UP:
                        break;
                    case ACTION_MOVE:
                        params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        if(motionEvent.getPointerCount() == 1) {
                            params.leftMargin = (int) (X - deltaX);
                            params.topMargin = (int) (Y - deltaY);
                            view.setLayoutParams(params);
                        } else {
                            deltaX = X - params.leftMargin;
                            deltaY = Y - params.topMargin;
                        }
                        break;
                }
                LocationButton.this.invalidate();
                return true;
            } else {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case ACTION_DOWN:
                        Log.e(LOG_TAG, "ACTION DOWN");
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        deltaX = X - params.leftMargin;
                        deltaY = Y - params.topMargin;
                        break;
                }
                return false;
            }
        }
    }

    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement = 0;

        switch(MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                // This means the width of this view has been given.
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                // Take the minimum of the preferred size and what
                // we were told to be.
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }

        return measurement;
    }

}
