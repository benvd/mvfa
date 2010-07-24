/* Modifications to allow more than one move in either direction and also
 * to allow the swipe motion to be used for switching tabs when used as TabContentView
 * 
 * Modifications Copyright (C) 2009 Justin Shapcott, nEx.Software
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

/* Original Copyright Information
 * 
 * Copyright (C) 2008 The Android Open Source Project
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

package my.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

class Switcher extends FrameLayout {
	private static final int MAJOR_MOVE = 60;
	private static final int ANIM_DURATION = 1000;

	private GestureDetector mGestureDetector;
	private int mCurrentView;
	private View mChild, mHistoryView;
	private View children[];

	private int mWidth;
	private TranslateAnimation inLeft;
	private TranslateAnimation outLeft;

	private TranslateAnimation inRight;
	private TranslateAnimation outRight;

	private static final int NONE = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private int mPreviousMove;

	public Switcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCurrentView = 0;
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				int dx = (int) (e2.getX() - e1.getX());
				if (Math.abs(dx) > MAJOR_MOVE && Math.abs(velocityX) > Math.abs(velocityY)) {
					if (velocityX > 0) {
						moveRight();
					} else {
						moveLeft();
					}
					return true;
				} else {
					return false;
				}
			}
		});
	}

	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		mWidth = w;
		inLeft = new TranslateAnimation(mWidth, 0, 0, 0);
		outLeft = new TranslateAnimation(0, -mWidth, 0, 0);
		inRight = new TranslateAnimation(-mWidth, 0, 0, 0);
		outRight = new TranslateAnimation(0, mWidth, 0, 0);

		inLeft.setDuration(ANIM_DURATION);
		outLeft.setDuration(ANIM_DURATION);
		inRight.setDuration(ANIM_DURATION);
		outRight.setDuration(ANIM_DURATION);

	}

	protected void onFinishInflate() {
		int count = getChildCount();
		children = new View[count];
		for (int i = 0; i < count; ++i) {
			children[i] = getChildAt(i);
			if (i != mCurrentView) {
				children[i].setVisibility(View.GONE);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	void moveLeft() {
		try {
			if (this.getParent().getParent() instanceof my.android.widget.TabHost) {
				my.android.widget.TabHost MyTabHost = ((my.android.widget.TabHost) Switcher.this.getParent()
						.getParent());
				if (mCurrentView < MyTabHost.getTabWidget().getTabCount() - 1) {
					MyTabHost.setCurrentTab(mCurrentView + 1);
					// mCurrentView++;
					mPreviousMove = LEFT;
				}
			} else {
				if (mCurrentView < children.length - 1 /* && mPreviousMove != LEFT */) {
					children[mCurrentView + 1].setVisibility(View.VISIBLE);
					children[mCurrentView + 1].startAnimation(inLeft);
					children[mCurrentView].startAnimation(outLeft);
					children[mCurrentView].setVisibility(View.GONE);

					mCurrentView++;
					mPreviousMove = LEFT;
				}
			}
		} catch (Exception e) {
		}
	}

	void moveRight() {
		try {
			// Switch the tab on the Parent TabHost
			if (this.getParent().getParent() instanceof my.android.widget.TabHost) {
				my.android.widget.TabHost MyTabHost = ((my.android.widget.TabHost) Switcher.this.getParent()
						.getParent());
				if (mCurrentView > 0) {
					MyTabHost.setCurrentTab(mCurrentView - 1);
					// mCurrentView--;
					mPreviousMove = RIGHT;
				}
			} else {
				if (mCurrentView > 0 /* && mPreviousMove != RIGHT */) {
					children[mCurrentView - 1].setVisibility(View.VISIBLE);
					children[mCurrentView - 1].startAnimation(inRight);
					children[mCurrentView].startAnimation(outRight);
					children[mCurrentView].setVisibility(View.GONE);

					mCurrentView--;
					mPreviousMove = RIGHT;
				}
			}
		} catch (Exception e) {
		}
	}

	public int getCurrentIndex() {
		return mCurrentView;
	}

	// Added so the TabHost tell us what the current tab is...
	public void setCurrentIndex(int index) {
		mCurrentView = index;
	}
}
