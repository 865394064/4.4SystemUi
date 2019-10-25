package com.android.systemui;

import android.app.Application;

public class SystemUiApp extends Application {

	/*
	 * if true, the font and drawable's color should be white; if false, the
	 * font and drawable's color should be black
	 */
	private boolean mWhite;
	// to decide the font and drawable's color
	private int mColor;

	public boolean isWhite() {
		return mWhite;
	}

	public void setWhite(boolean white) {
		mWhite = white;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}
}
