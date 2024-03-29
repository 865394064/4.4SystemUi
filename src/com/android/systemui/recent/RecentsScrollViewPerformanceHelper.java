/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recent;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import com.android.systemui.R;

public class RecentsScrollViewPerformanceHelper {
    public static final boolean OPTIMIZE_SW_RENDERED_RECENTS = true;
    public static final boolean USE_DARK_FADE_IN_HW_ACCELERATED_MODE = true;
    private View mScrollView;

    private int mFadingEdgeLength;
    private boolean mIsVertical;
    private boolean mSoftwareRendered = false;

    public static RecentsScrollViewPerformanceHelper create(Context context,
            AttributeSet attrs, View scrollView, boolean isVertical) {
        boolean isTablet = context.getResources().
                getBoolean(R.bool.config_recents_interface_for_tablets);
        if (!isTablet && (OPTIMIZE_SW_RENDERED_RECENTS || USE_DARK_FADE_IN_HW_ACCELERATED_MODE)) {
            return new RecentsScrollViewPerformanceHelper(context, attrs, scrollView, isVertical);
        } else {
            return null;
        }
    }

    public RecentsScrollViewPerformanceHelper(Context context,
            AttributeSet attrs, View scrollView, boolean isVertical) {
        mScrollView = scrollView;
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View);
        mFadingEdgeLength = a.getDimensionPixelSize(android.R.styleable.View_fadingEdgeLength,
                ViewConfiguration.get(context).getScaledFadingEdgeLength());
        mIsVertical = isVertical;
    }

    public void onAttachedToWindowCallback(
            RecentsCallback callback, LinearLayout layout, boolean hardwareAccelerated) {
        mSoftwareRendered = !hardwareAccelerated;
        if ((mSoftwareRendered && OPTIMIZE_SW_RENDERED_RECENTS)
                || USE_DARK_FADE_IN_HW_ACCELERATED_MODE) {
            mScrollView.setVerticalFadingEdgeEnabled(false);
            mScrollView.setHorizontalFadingEdgeEnabled(false);
        }
    }

    public void addViewCallback(View newLinearLayoutChild) {
        if (mSoftwareRendered && OPTIMIZE_SW_RENDERED_RECENTS) {
            final RecentsPanelView.ViewHolder holder =
                    (RecentsPanelView.ViewHolder) newLinearLayoutChild.getTag();
            holder.labelView.setDrawingCacheEnabled(true);
            holder.labelView.buildDrawingCache();
        }
    }

    public void drawCallback(Canvas canvas,
            int left, int right, int top, int bottom, int scrollX, int scrollY,
            float topFadingEdgeStrength, float bottomFadingEdgeStrength,
            float leftFadingEdgeStrength, float rightFadingEdgeStrength) {

        if ((mSoftwareRendered && OPTIMIZE_SW_RENDERED_RECENTS)
                || USE_DARK_FADE_IN_HW_ACCELERATED_MODE) {
            Paint p = new Paint();
            Matrix matrix = new Matrix();
            // use use a height of 1, and then wack the matrix each time we
            // actually use it.
           // Shader fade = new LinearGradient(0, 0, 0, 1, 0xCC000000, 0, Shader.TileMode.CLAMP);
            // PULL OUT THIS CONSTANT

            //p.setShader(fade);

            // draw the fade effect
            boolean drawTop = false;
            boolean drawBottom = false;
            boolean drawLeft = false;
            boolean drawRight = false;

            float topFadeStrength = 0.0f;
            float bottomFadeStrength = 0.0f;
            float leftFadeStrength = 0.0f;
            float rightFadeStrength = 0.0f;

            final float fadeHeight = mFadingEdgeLength;
            int length = (int) fadeHeight;

            // clip the fade length if top and bottom fades overlap
            // overlapping fades produce odd-looking artifacts
            if (mIsVertical && (top + length > bottom - length)) {
                length = (bottom - top) / 2;
            }

            // also clip horizontal fades if necessary
            if (!mIsVertical && (left + length > right - length)) {
                length = (right - left) / 2;
            }

            if (mIsVertical) {
                topFadeStrength = Math.max(0.0f, Math.min(1.0f, topFadingEdgeStrength));
                drawTop = topFadeStrength * fadeHeight > 1.0f;
                bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, bottomFadingEdgeStrength));
                drawBottom = bottomFadeStrength * fadeHeight > 1.0f;
            }

            if (!mIsVertical) {
                leftFadeStrength = Math.max(0.0f, Math.min(1.0f, leftFadingEdgeStrength));
                drawLeft = leftFadeStrength * fadeHeight > 1.0f;
                rightFadeStrength = Math.max(0.0f, Math.min(1.0f, rightFadingEdgeStrength));
                drawRight = rightFadeStrength * fadeHeight > 1.0f;
            }

            if (drawTop) {
                matrix.setScale(1, fadeHeight * topFadeStrength);
                matrix.postTranslate(left, top);
                //fade.setLocalMatrix(matrix);
                canvas.drawRect(left, top, right, top + length, p);
            }

            if (drawBottom) {
                matrix.setScale(1, fadeHeight * bottomFadeStrength);
                matrix.postRotate(180);
                matrix.postTranslate(left, bottom);
               // fade.setLocalMatrix(matrix);
                canvas.drawRect(left, bottom - length, right, bottom, p);
            }

            if (drawLeft) {
                matrix.setScale(1, fadeHeight * leftFadeStrength);
                matrix.postRotate(-90);
                matrix.postTranslate(left, top);
               // fade.setLocalMatrix(matrix);
                canvas.drawRect(left, top, left + length, bottom, p);
            }

            if (drawRight) {
                matrix.setScale(1, fadeHeight * rightFadeStrength);
                matrix.postRotate(90);
                matrix.postTranslate(right, top);
                //fade.setLocalMatrix(matrix);
                canvas.drawRect(right - length, top, right, bottom, p);
            }
        }
    }

    public int getVerticalFadingEdgeLengthCallback() {
        return mFadingEdgeLength;
    }

    public int getHorizontalFadingEdgeLengthCallback() {
        return mFadingEdgeLength;
    }

}
