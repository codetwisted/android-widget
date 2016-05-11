package org.codetwisted.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

public class DrawerLayout extends ViewGroup {

	public DrawerLayout(Context context) {
		super(context);

		initWidget(context, null, 0, 0);
	}

	public DrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		initWidget(context, attrs, 0, 0);
	}

	public DrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initWidget(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public DrawerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		initWidget(context, attrs, defStyleAttr, defStyleRes);
	}


	public interface Listener {

		void onDrawerStartOpening();

		void onDrawerOpened();

		void onDrawerStartClosing();

		void onDrawerClosed();

		void onDrawerSliding(int current, int from, int to);

	}

	public static class ListenerAdapter implements Listener {
		@Override
		public void onDrawerStartOpening() {
			/* stub */
		}

		@Override
		public void onDrawerOpened() {
			/* stub */
		}

		@Override
		public void onDrawerStartClosing() {
			/* stub */
		}

		@Override
		public void onDrawerClosed() {
			/* stub */
		}

		@Override
		public void onDrawerSliding(int current, int from, int to) {
			/* stub */
		}
	}


	private Listener listener;

	public void setListener(Listener listener) {
		this.listener = listener;
	}


	private int gravity;

	public void setGravity(int gravity) {
		if (this.gravity != gravity) {
			drawer = getDrawerImpl(this.gravity = gravity);
			requestLayout();
		}
	}

	public int getGravity() {
		return gravity;
	}


	private boolean touchEnabled = true;

	public void setTouchEnabled(boolean canTouch) {
		this.touchEnabled = touchEnabled;
	}

	public boolean isTouchEnabled() {
		return touchEnabled;
	}


	private static final int STATE_IDLE    	= 0;
	private static final int STATE_PULLED  	= 1;
	private static final int STATE_OPENING 	= 2;
	private static final int STATE_CLOSING 	= 3;
	private static final int STATE_ROLLING 	= 4;

	private int state = STATE_IDLE;


	private boolean  drawerOpen;
	private Runnable drawerOpenTask;

	public boolean isDrawerOpen() {
		return drawerOpen;
	}

	public void setDrawerOpen(final boolean drawerOpen, final boolean animated) {
		if (this.drawerOpen != drawerOpen) {
			this.drawerOpen = drawerOpen;

			state = STATE_ROLLING;
			drawerOpenTask = new Runnable() {

				@Override
				public void run() {
					drawer.setOpen(drawerOpen, animated);

					if (!animated) {
						state = STATE_IDLE;
					}
					drawerOpenTask = null;
				}
			};
			requestLayout();
		}
	}


	private float drawerOffset;

	public float getDrawerOffset() {
		return drawerOffset;
	}

	public void setDrawerOffset(float drawerOffset) {
		if (this.drawerOffset != drawerOffset) {
			this.drawerOffset = filterDrawerOffset(drawerOffset);
			requestLayout();
		}
	}


	private boolean seizeContent = false;

	public boolean isSeizeContent() {
		return seizeContent;
	}

	public void setSeizeContent(boolean seizeContent) {
		this.seizeContent = seizeContent;
	}


	private int touchSlop;
	private int flingVelocityMinimum;

	private long animationDuration;

	public long getAnimationDuration() {
		return animationDuration;
	}

	public void setAnimationDuration(long animationDuration) {
		this.animationDuration = Math.max(animationDuration,
										  getResources().getInteger(android.R.integer.config_shortAnimTime));
	}

	private final ValueAnimator animator = new ValueAnimator();

	private void initWidget(Context context, AttributeSet attrs, int defStyleAttr,
		int defStyleRes) {

		this.drawerOffset = 0;
		this.gravity = GravityCompat.START;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DrawerLayout,
				defStyleAttr, defStyleRes);
			{
				this.drawerOffset = filterDrawerOffset(
					a.getDimension(R.styleable.DrawerLayout_drawerOffset, drawerOffset));
				this.seizeContent = a.getBoolean(R.styleable.DrawerLayout_seizeContent, false);
				this.gravity = a.getInteger(R.styleable.DrawerLayout_android_gravity, gravity);
				this.animationDuration = a.getInt(R.styleable.DrawerLayout_animationTime,
												  getResources().getInteger(android.R.integer.config_shortAnimTime));
				this.touchEnabled = a.getBoolean(R.styleable.DrawerLayout_touchEnabled, true);
			}
			a.recycle();
		}
		drawer = getDrawerImpl(gravity);

		ViewConfiguration vc = ViewConfiguration.get(context);

		touchSlop = vc.getScaledTouchSlop();
		flingVelocityMinimum = vc.getScaledMinimumFlingVelocity();

		//noinspection Convert2Lambda
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				drawer.onAnimationUpdate(animation);
			}
		});
		animator.addListener(new ValueAnimator.AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				/* Nothing to do */
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				drawer.onAnimationEnd(animation);
				state = STATE_IDLE;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				/* Nothing to do */
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				/* Nothing to do */
			}
		});
	}

	private float filterDrawerOffset(float drawerOffset) {
		return Math.max(drawerOffset, 0);
	}


	private interface DrawerImpl {

		void measure(SparseArray<View> drawerChildren, int widthMeasureSpec, int heightMeasureSpec);

		void layout(SparseArray<View> drawerChildren, int parentLeft, int parentTop,
			int parentRight, int parentBottom);


		void handleGrip(int pointerIndex, int pointerId, MotionEvent ev);

		void handlePull(int pointerIndex, int pointerId, MotionEvent ev);

		void handleFree(int pointerIndex, int pointerId, MotionEvent ev);


		void onAnimationUpdate(ValueAnimator animation);

		void onAnimationEnd(Animator animator);


		void setOpen(boolean open, boolean animated);

	}

	private DrawerImpl drawer;

	@SuppressLint("RtlHardcoded")
	private DrawerImpl getDrawerImpl(int gravity) {
		int layoutDirection;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			layoutDirection = getLayoutDirection();
		}
		else {
			layoutDirection = 0;
		}
		switch (GravityCompat.getAbsoluteGravity(gravity, layoutDirection)) {
			case Gravity.LEFT:
				return new LeftDrawerImpl();
			case Gravity.TOP:
				return new TopDrawerImpl();
			case Gravity.RIGHT:
				return new RightDrawerImpl();
			case Gravity.BOTTOM:
				return new BottomDrawerImpl();
		}
		return new LeftDrawerImpl();
	}


	private void dispatchDrawerOpening() {
		if (state != STATE_OPENING) {
			if (listener != null) {
				listener.onDrawerStartOpening();
			}
			state = STATE_OPENING;
		}
	}

	private void dispatchDrawerOpen() {
		if (listener != null) {
			listener.onDrawerOpened();
		}
		drawerOpen = true;
	}

	private void dispatchDrawerClosing() {
		if (state != STATE_CLOSING) {
			if (listener != null) {
				listener.onDrawerStartClosing();
			}
			state = STATE_CLOSING;
		}
	}

	private void dispatchDrawerClosed() {
		if (listener != null) {
			listener.onDrawerClosed();
		}
		drawerOpen = false;
	}

	private void dispatchDrawerSliding(int current, int from, int to) {
		if (listener != null) {
			listener.onDrawerSliding(current, from, to);
		}
	}


	private final Rect handleRect = new Rect();
	private final Rect contentRect = new Rect();

	private void performHandleClick(View handle, MotionEvent ev, int pointerIndex) {
		if (handle != null && handleRect.contains( // preserve new line
			(int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex))) {

			handle.performClick();
		}
	}

	private final class LeftDrawerImpl implements DrawerImpl {

		private int contentRightCurrent;
		private int contentRightMax;
		private int contentRightMin;
		private int contentRightMiddle;

		private int contentRightThreshold;

		private View handle;

		@Override
		public void measure(SparseArray<View> drawerChildren, int widthMeasureSpec,
			int heightMeasureSpec) {
			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				int contentWidthMax = getMeasuredWidth();

				handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					LayoutParams lp = (LayoutParams) handle.getLayoutParams();

					contentWidthMax -= (handle.getMeasuredWidth() + lp.rightMargin);
				}
				contentRightMin = (int) drawerOffset;
				contentRightMax = Math.min(content.getMeasuredWidth(), contentWidthMax);
				contentRightMiddle = (contentRightMin + contentRightMin) >> 1;

				if (state == STATE_IDLE) {
					contentRightCurrent = isDrawerOpen() ? contentRightMax : contentRightMin;
				}
			}
		}

		@Override
		public void layout(SparseArray<View> drawerChildren, int parentLeft, int parentTop,
			int parentRight, int parentBottom) {
			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				content.layout(contentRightCurrent - content.getMeasuredWidth(), parentTop,
					contentRightCurrent, parentBottom);

				content.getHitRect(contentRect);

				View handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					int handleTop = parentTop
						+ (parentBottom - parentTop - handle.getMeasuredHeight()) / 2;

					handle.layout(content.getRight(), handleTop,
						content.getRight() + handle.getMeasuredWidth(),
						handleTop + handle.getMeasuredHeight());

					handle.getHitRect(handleRect);

					int padding = touchSlop >> 1;

					handleRect.top -= padding;
					handleRect.right += padding;
					handleRect.bottom += padding;
				}
				contentRightThreshold = Math.round(
					.68f * contentRightMiddle + .5f * handleRect.width());

				return;
			}
			handleRect.setEmpty();
		}

		private float x;
		private int   contentLeftFixed;

		private VelocityTracker velocityTracker;

		@Override
		public void handleGrip(int pointerIndex, int pointerId, MotionEvent ev) {
			x = ev.getX(pointerIndex);
			contentLeftFixed = contentRightCurrent;

			velocityTracker = VelocityTracker.obtain();
			velocityTracker.addMovement(ev);

			animator.cancel();
		}


		private boolean moved;

		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getX(pointerIndex) - x;

			if (Math.abs(diff) > touchSlop) {
				int contentRightCurrentNew = Math.max(
					Math.min(contentLeftFixed + Math.round(diff), contentRightMax),
					contentRightMin);

				if (contentRightCurrent != contentRightCurrentNew) {
					if (contentRightCurrent < contentRightCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}
					state = STATE_PULLED;

					contentRightCurrent = contentRightCurrentNew;

					dispatchDrawerSliding(contentRightCurrent, contentRightMin, contentRightMax);
					requestLayout();

					velocityTracker.addMovement(ev);
					moved = true;
				}
				else if (moved) {
					if (!(contentRightMin < contentRightCurrent // preserve new line
						&& contentRightCurrent < contentRightMax)) {

						onAnimationEnd(null);
						moved = false;
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			if (moved) {
				float velocity;
				{
					velocityTracker.computeCurrentVelocity(1000);

					velocity = velocityTracker.getXVelocity(pointerId);
				}
				velocityTracker.recycle();

				float velocityDirection = Math.signum(velocity);
				float velocityPower = velocity / flingVelocityMinimum;

				int contentRightTarget;

				if (velocityPower > 1.32f) {
					contentRightTarget = contentRightMax;
				}
				else if (velocityPower < -.96f) {
					contentRightTarget = contentRightMin;
				}
				else {
					contentRightTarget =
						velocityDirection > 0 ^ contentRightCurrent > contentRightThreshold
							? contentRightMax
							: contentRightMin;
				}
				animator.setIntValues(contentRightCurrent, contentRightTarget);
				animator.setDuration(animationDuration);
				animator.start();

				if (contentRightTarget == contentRightMax) {
					dispatchDrawerOpening();
				}
				else {
					dispatchDrawerClosing();
				}

				moved = false;
			}
			else {
				state = STATE_IDLE;
				performHandleClick(handle, ev, pointerIndex);
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentRightCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentRightCurrent, contentRightMin, contentRightMax);
			requestLayout();
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			if (contentRightCurrent == contentRightMin) {
				dispatchDrawerClosed();
			}
			else if (contentRightCurrent == contentRightMax) {
				dispatchDrawerOpen();
			}
		}

		@Override
		public void setOpen(boolean open, boolean animated) {
			if (animated) {
				if (open) {
					dispatchDrawerOpening();
				}
				else {
					dispatchDrawerClosing();
				}

				animator.setIntValues(contentRightCurrent,
					open ? contentRightMax : contentRightMin);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentRightCurrent = open ? contentRightMax : contentRightMin;
			}
		}
	}

	private final class TopDrawerImpl implements DrawerImpl {

		private int contentBottomCurrent;
		private int contentBottomMin;
		private int contentBottomMax;
		private int contentBottomMiddle;

		private int contentBottomThreshold;

		private View handle;

		@Override
		public void measure(SparseArray<View> drawerChildren, int widthMeasureSpec,
			int heightMeasureSpec) {

			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				int contentHeightMax = getMeasuredHeight();

				handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					LayoutParams lp = (LayoutParams) handle.getLayoutParams();

					contentHeightMax -= (handle.getMeasuredHeight() + lp.bottomMargin);
				}
				contentBottomMin = (int) drawerOffset;
				contentBottomMax = Math.min(content.getMeasuredHeight(), contentHeightMax);
				contentBottomMiddle = (contentBottomMin + contentBottomMax) >> 1;

				if (state == STATE_IDLE) {
					contentBottomCurrent = isDrawerOpen() ? contentBottomMax : contentBottomMin;
				}
			}
		}

		@Override
		public void layout(SparseArray<View> drawerChildren, int parentLeft, int parentTop,
			int parentRight, int parentBottom) {

			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				content.layout(parentLeft, contentBottomCurrent - content.getMeasuredHeight(),
					parentRight, contentBottomCurrent);

				content.getHitRect(contentRect);

				View handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					int handleLeft = parentLeft
						+ (parentRight - parentLeft - handle.getMeasuredWidth()) / 2;

					handle.layout(handleLeft, content.getBottom(),
						handleLeft + handle.getMeasuredWidth(),
						content.getBottom() + handle.getMeasuredHeight());

					handle.getHitRect(handleRect);
					{
						int padding = touchSlop >> 1;

						handleRect.left -= padding;
						handleRect.right += padding;
						handleRect.bottom += padding;
					}
				}
				contentBottomThreshold = Math.round(
					0.68f * contentBottomMiddle - .5f * handleRect.height());

				return;
			}
			handleRect.setEmpty();
		}


		private float y;
		private int   contentBottomFixed;

		private VelocityTracker velocityTracker;

		@Override
		public void handleGrip(int pointerIndex, int pointerId, MotionEvent ev) {
			y = ev.getY(pointerIndex);
			contentBottomFixed = contentBottomCurrent;

			velocityTracker = VelocityTracker.obtain();
			velocityTracker.addMovement(ev);

			animator.cancel();
		}


		private boolean moved;

		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getY(pointerIndex) - y;

			if (Math.abs(diff) > touchSlop) {
				int contentBottomCurrentNew = Math.max(
					Math.min(contentBottomFixed + Math.round(diff), contentBottomMax),
					contentBottomMin);

				if (contentBottomCurrent != contentBottomCurrentNew) {
					if (contentBottomCurrent < contentBottomCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}

					contentBottomCurrent = contentBottomCurrentNew;

					dispatchDrawerSliding(contentBottomCurrent, contentBottomMin, contentBottomMax);
					requestLayout();

					velocityTracker.addMovement(ev);
					moved = true;
				}
				else if (moved) {
					if (!(contentBottomMin < contentBottomCurrent // preserve new line
						&& contentBottomCurrent < contentBottomMax)) {

						onAnimationEnd(null);
						moved = false;
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			if (moved) {
				float velocity;
				{
					velocityTracker.computeCurrentVelocity(1000);

					velocity = velocityTracker.getYVelocity(pointerId);
				}
				velocityTracker.recycle();

				float velocityDirection = Math.signum(velocity);
				float velocityPower = velocity / flingVelocityMinimum;

				int contentBottomTarget;

				if (velocityPower > 1.32f) {
					contentBottomTarget = contentBottomMax;
				}
				else if (velocityPower < -.96f) {
					contentBottomTarget = contentBottomMin;
				}
				else {
					contentBottomTarget =
						velocityDirection > 0 ^ contentBottomCurrent > contentBottomThreshold
							? contentBottomMax
							: contentBottomMin;
				}
				animator.setIntValues(contentBottomCurrent, contentBottomTarget);
				animator.setDuration(animationDuration);
				animator.start();

				if (contentBottomTarget == contentBottomMax) {
					dispatchDrawerOpening();
				}
				else {
					dispatchDrawerClosing();
				}

				moved = false;
			}
			else {
				state = STATE_IDLE;
				performHandleClick(handle, ev, pointerIndex);
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentBottomCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentBottomCurrent, contentBottomMin, contentBottomMax);
			requestLayout();
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			if (contentBottomCurrent == contentBottomMin) {
				dispatchDrawerClosed();
			}
			else if (contentBottomCurrent == contentBottomMax) {
				dispatchDrawerOpen();
			}
		}

		@Override
		public void setOpen(boolean open, boolean animated) {
			if (animated) {
				if (open) {
					dispatchDrawerOpening();
				}
				else {
					dispatchDrawerClosing();
				}

				animator.setIntValues(contentBottomCurrent,
					open ? contentBottomMax : contentBottomMin);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentBottomCurrent = open ? contentBottomMax : contentBottomMin;
			}
		}
	}

	private final class RightDrawerImpl implements DrawerImpl {

		private int contentLeftCurrent;
		private int contentLeftMax;
		private int contentLeftMin;
		private int contentLeftMiddle;

		private int contentLeftThreshold;

		private View handle;

		@Override
		public void measure(SparseArray<View> drawerChildren, int widthMeasureSpec,
			int heightMeasureSpec) {
			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				int contentWidthMax = getMeasuredWidth();

				handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					LayoutParams lp = (LayoutParams) handle.getLayoutParams();

					contentWidthMax -= (handle.getMeasuredWidth() + lp.leftMargin);
				}
				contentLeftMax = getMeasuredWidth() - (int) drawerOffset;
				contentLeftMin = Math.round(
					getMeasuredWidth() - Math.min(content.getMeasuredWidth(), contentWidthMax));
				contentLeftMiddle = (contentLeftMin + contentLeftMax) / 2;

				if (state == STATE_IDLE) {
					contentLeftCurrent = isDrawerOpen() ? contentLeftMin : contentLeftMax;
				}
			}
		}

		@Override
		public void layout(SparseArray<View> drawerChildren, int parentLeft, int parentTop,
			int parentRight, int parentBottom) {
			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				content.layout(contentLeftCurrent, parentTop,
					contentLeftCurrent + content.getMeasuredWidth(), parentBottom);

				content.getHitRect(contentRect);

				View handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					int handleTop = parentTop
						+ (parentBottom - parentTop - handle.getMeasuredHeight()) / 2;

					handle.layout(content.getLeft() - handle.getMeasuredWidth(), handleTop,
						content.getLeft(), handleTop + handle.getMeasuredHeight());

					handle.getHitRect(handleRect);

					int padding = touchSlop >> 1;

					handleRect.left -= padding;
					handleRect.top -= padding;
					handleRect.bottom += padding;
				}
				contentLeftThreshold = Math.round(
					.68f * contentLeftMiddle + .5f * handleRect.width());

				return;
			}
			handleRect.setEmpty();
		}

		private float x;
		private int   contentLeftFixed;

		private VelocityTracker velocityTracker;

		@Override
		public void handleGrip(int pointerIndex, int pointerId, MotionEvent ev) {
			x = ev.getX(pointerIndex);
			contentLeftFixed = contentLeftCurrent;

			velocityTracker = VelocityTracker.obtain();
			velocityTracker.addMovement(ev);

			animator.cancel();
		}


		private boolean moved;

		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getX(pointerIndex) - x;

			if (Math.abs(diff) > touchSlop) {
				int contentLeftCurrentNew = Math.max(
					Math.min(contentLeftFixed + Math.round(diff), contentLeftMax), contentLeftMin);

				if (contentLeftCurrent != contentLeftCurrentNew) {
					if (contentLeftCurrent > contentLeftCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}
					state = STATE_PULLED;

					contentLeftCurrent = contentLeftCurrentNew;

					dispatchDrawerSliding(contentLeftCurrent, contentLeftMin, contentLeftMax);
					requestLayout();

					velocityTracker.addMovement(ev);
					moved = true;
				}
				else if (moved) {
					if (!(contentLeftMin < contentLeftCurrent // preserve new line
						&& contentLeftCurrent < contentLeftMax)) {

						onAnimationEnd(null);
						moved = false;
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			if (moved) {
				float velocity;
				{
					velocityTracker.computeCurrentVelocity(1000);

					velocity = velocityTracker.getXVelocity(pointerId);
				}
				velocityTracker.recycle();

				float velocityDirection = Math.signum(velocity);
				float velocityPower = velocity / flingVelocityMinimum;

				int contentLeftTarget;

				if (velocityPower > .96f) {
					contentLeftTarget = contentLeftMax;
				}
				else if (velocityPower < -1.32f) {
					contentLeftTarget = contentLeftMin;
				}
				else {
					contentLeftTarget =
						velocityDirection > 0 ^ contentLeftCurrent > contentLeftThreshold
							? contentLeftMax
							: contentLeftMin;
				}
				animator.setIntValues(contentLeftCurrent, contentLeftTarget);
				animator.setDuration(animationDuration);
				animator.start();

				if (contentLeftTarget == contentLeftMin) {
					if (state != STATE_OPENING) {
						dispatchDrawerOpening();
					}
				}
				else {
					if (state != STATE_CLOSING) {
						dispatchDrawerClosing();
					}
				}

				moved = false;
			}
			else {
				state = STATE_IDLE;
				performHandleClick(handle, ev, pointerIndex);
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentLeftCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentLeftCurrent, contentLeftMin, contentLeftMax);
			requestLayout();
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			if (contentLeftCurrent == contentLeftMin) {
				dispatchDrawerOpen();
			}
			else if (contentLeftCurrent == contentLeftMax) {
				dispatchDrawerClosed();
			}
		}

		@Override
		public void setOpen(boolean open, boolean animated) {
			if (animated) {
				if (open) {
					dispatchDrawerOpening();
				}
				else {
					dispatchDrawerClosing();
				}

				animator.setIntValues(contentLeftCurrent, open ? contentLeftMin : contentLeftMax);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentLeftCurrent = open ? contentLeftMin : contentLeftMax;
			}
		}
	}

	private final class BottomDrawerImpl implements DrawerImpl {

		private int contentTopCurrent;
		private int contentTopMax;
		private int contentTopMin;
		private int contentTopMiddle;

		private int contentTopThreshold;

		private View handle;

		@Override
		public void measure(SparseArray<View> drawerChildren, int widthMeasureSpec,
			int heightMeasureSpec) {
			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				int contentHeightMax = getMeasuredHeight();

				handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					LayoutParams lp = (LayoutParams) handle.getLayoutParams();

					contentHeightMax -= (handle.getMeasuredHeight() + lp.topMargin);
				}
				contentTopMax = getMeasuredHeight() - (int) drawerOffset;
				contentTopMin = Math.round(
					getMeasuredHeight() - Math.min(content.getMeasuredHeight(), contentHeightMax));
				contentTopMiddle = (contentTopMin + contentTopMax) / 2;

				if (state == STATE_IDLE) {
					contentTopCurrent = isDrawerOpen() ? contentTopMin : contentTopMax;
				}
			}
		}

		@Override
		public void layout(SparseArray<View> drawerChildren, int parentLeft, int parentTop,
			int parentRight, int parentBottom) {
			View content = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_CONTENT);

			if (content != null) {
				content.layout(parentLeft, contentTopCurrent, parentRight,
					contentTopCurrent + content.getMeasuredHeight());

				content.getHitRect(contentRect);

				View handle = drawerChildren.get(LayoutParams.NODE_TYPE_DRAWER_HANDLE);

				if (handle != null) {
					int handleLeft =
						parentLeft + (parentRight - parentLeft - handle.getMeasuredWidth()) >> 1;

					handle.layout(handleLeft, content.getTop() - handle.getMeasuredHeight(),
						handleLeft + handle.getMeasuredWidth(), content.getTop());

					handle.getHitRect(handleRect);

					int padding = touchSlop >> 1;

					handleRect.left -= padding;
					handleRect.top -= padding;
					handleRect.right += padding;
				}
				contentTopThreshold = Math.round(
					.68f * contentTopMiddle + .5f * handleRect.width());

				return;
			}
			handleRect.setEmpty();
		}

		private float y;
		private int   contentTopFixed;

		private VelocityTracker velocityTracker;

		@Override
		public void handleGrip(int pointerIndex, int pointerId, MotionEvent ev) {
			y = ev.getY(pointerIndex);
			contentTopFixed = contentTopCurrent;

			velocityTracker = VelocityTracker.obtain();
			velocityTracker.addMovement(ev);

			animator.cancel();
		}


		private boolean moved;

		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getY(pointerIndex) - y;

			if (Math.abs(diff) > touchSlop) {
				int contentTopCurrentNew = Math.max(
					Math.min(contentTopFixed + Math.round(diff), contentTopMax), contentTopMin);

				if (contentTopCurrent != contentTopCurrentNew) {
					if (contentTopCurrent > contentTopCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}
					state = STATE_PULLED;

					contentTopCurrent = contentTopCurrentNew;

					dispatchDrawerSliding(contentTopCurrent, contentTopMin, contentTopMax);
					requestLayout();

					velocityTracker.addMovement(ev);
					moved = true;
				}
				else if (moved) {
					if (!(contentTopMin < contentTopCurrent // preserve new line
						&& contentTopCurrent < contentTopMax)) {

						onAnimationEnd(null);
						moved = false;
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			if (moved) {
				float velocity;
				{
					velocityTracker.computeCurrentVelocity(1000);

					velocity = velocityTracker.getYVelocity(pointerId);
				}
				velocityTracker.recycle();

				float velocityDirection = Math.signum(velocity);
				float velocityPower = velocity / flingVelocityMinimum;

				int contentTopTarget;

				if (velocityPower > .96f) {
					contentTopTarget = contentTopMax;
				}
				else if (velocityPower < -1.32f) {
					contentTopTarget = contentTopMin;
				}
				else {
					contentTopTarget =
						velocityDirection > 0 ^ contentTopCurrent > contentTopThreshold
							? contentTopMax
							: contentTopMin;
				}
				animator.setIntValues(contentTopCurrent, contentTopTarget);
				animator.setDuration(animationDuration);
				animator.start();

				if (contentTopTarget == contentTopMin) {
					if (state != STATE_OPENING) {
						dispatchDrawerOpening();
					}
				}
				else {
					if (state != STATE_CLOSING) {
						dispatchDrawerClosing();
					}
				}

				moved = false;
			}
			else {
				state = STATE_IDLE;
				performHandleClick(handle, ev, pointerIndex);
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentTopCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentTopCurrent, contentTopMin, contentTopMax);
			requestLayout();
		}

		@Override
		public void onAnimationEnd(Animator animator) {
			if (contentTopCurrent == contentTopMin) {
				dispatchDrawerOpen();
			}
			else if (contentTopCurrent == contentTopMax) {
				dispatchDrawerClosed();
			}
		}

		@Override
		public void setOpen(boolean open, boolean animated) {
			if (animated) {
				if (open) {
					dispatchDrawerOpening();
				}
				else {
					dispatchDrawerClosing();
				}

				animator.setIntValues(contentTopCurrent, open ? contentTopMin : contentTopMax);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentTopCurrent = open ? contentTopMin : contentTopMax;
			}
		}
	}


	private int pointerId;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (touchEnabled) {
			if (state == STATE_IDLE) {
				final int movementAction = ev.getAction();

				if (movementAction == MotionEvent.ACTION_DOWN
					|| movementAction == MotionEvent.ACTION_POINTER_DOWN) {

					for (int i = ev.getPointerCount() - 1; i >= 0; --i) {
						int x = (int) ev.getX(i);
						int y = (int) ev.getY(i);
						if (handleRect.contains(x, y) || seizeContent && contentRect.contains(x, y)) {
							drawer.handleGrip(i, pointerId = ev.getPointerId(i), ev);
							state = STATE_PULLED;
							return true;
						}
					}
				}
			}
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (state != STATE_IDLE) {
			int pointerIndex = event.findPointerIndex(pointerId);

			if (pointerIndex >= 0) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						drawer.handlePull(pointerIndex, pointerId, event);
						break;
					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_UP:
						state = STATE_ROLLING;
						drawer.handleFree(pointerIndex, pointerId, event);
						return false;
				}
			}
			return true;
		}
		return super.onTouchEvent(event);
	}


	private Rect foregroundPadding = new Rect();

	@TargetApi(Build.VERSION_CODES.M)
	private boolean isForegroundInsidePadding() {
		Drawable foreground = getForeground();

		if (foreground != null) {
			return foreground.getPadding(foregroundPadding) // preserve new line
				&& foregroundPadding.left >= getPaddingLeft()
				&& foregroundPadding.right >= getPaddingRight()
				&& foregroundPadding.top >= getPaddingTop()
				&& foregroundPadding.bottom >= getPaddingBottom();
		}
		foregroundPadding.setEmpty();

		return false;
	}

	private int getPaddingLeftWithForeground() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return isForegroundInsidePadding()
				? Math.max(getPaddingLeft(), foregroundPadding.left)
				: getPaddingLeft() + foregroundPadding.left;
		}
		return getPaddingLeft();
	}

	private int getPaddingRightWithForeground() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return isForegroundInsidePadding() ? Math.max(getPaddingRight(),
				foregroundPadding.right) : getPaddingRight() + foregroundPadding.right;
		}
		return getPaddingRight();
	}

	private int getPaddingTopWithForeground() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return isForegroundInsidePadding()
				? Math.max(getPaddingTop(), foregroundPadding.top)
				: getPaddingTop() + foregroundPadding.top;
		}
		return getPaddingTop();
	}

	private int getPaddingBottomWithForeground() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return isForegroundInsidePadding() ? Math.max(getPaddingBottom(),
				foregroundPadding.bottom) : getPaddingBottom() + foregroundPadding.bottom;
		}
		return getPaddingBottom();
	}

	private final List<View>        matchParentChildren = new LinkedList<>();
	private final SparseArray<View> drawerChildren      = new SparseArray<>();

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final boolean measureMatchParentChildren =
			MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
				|| MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

		int childState = 0;
		int maxHeight = 0;
		int maxWidth = 0;

		int count = getChildCount();
		for (int i = 0; i < count; ++i) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

				childState = combineMeasuredStates(childState, child.getMeasuredState());

				maxWidth = Math.max(maxWidth,
					child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
				maxHeight = Math.max(maxHeight,
					child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

				if (lp.nodeType == LayoutParams.NODE_TYPE_DEFAULT) {
					if (measureMatchParentChildren) {
						if (lp.width == LayoutParams.MATCH_PARENT
							|| lp.height == LayoutParams.MATCH_PARENT) {

							matchParentChildren.add(child);
						}
					}
				}
				else {
					drawerChildren.put(lp.nodeType, child);
				}
			}
		}
		// Account for padding too
		maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
		maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground();

		// Check against our minimum height and width
		maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
		maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

		setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
			resolveSizeAndState(maxHeight, heightMeasureSpec,
				childState << MEASURED_HEIGHT_STATE_SHIFT));

		count = matchParentChildren.size();

		if (count > 1) {
			for (int i = 0; i < count; ++i) {
				final View child = matchParentChildren.get(i);
				final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

				final int childWidthMeasureSpec;
				if (lp.width == LayoutParams.MATCH_PARENT) {
					final int width = Math.max(0, getMeasuredWidth() // preserve new line
						- getPaddingLeftWithForeground() - getPaddingRightWithForeground()
						- lp.leftMargin - lp.rightMargin);

					childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
				}
				else {
					childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
						getPaddingLeftWithForeground() + getPaddingRightWithForeground() +
							lp.leftMargin + lp.rightMargin, lp.width);
				}
				final int childHeightMeasureSpec;
				if (lp.height == LayoutParams.MATCH_PARENT) {
					final int height = Math.max(0, getMeasuredHeight() // preserve new line
						- getPaddingTopWithForeground() - getPaddingBottomWithForeground()
						- lp.topMargin - lp.bottomMargin);

					childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
						MeasureSpec.EXACTLY);
				}
				else {
					childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
						getPaddingTopWithForeground() + getPaddingBottomWithForeground() +
							lp.topMargin + lp.bottomMargin, lp.height);
				}
				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			}
		}
		matchParentChildren.clear();

		drawer.measure(drawerChildren, widthMeasureSpec, heightMeasureSpec);
		drawerChildren.clear();
	}


	private static final int GRAVITY_DEFAULT = GravityCompat.START | Gravity.TOP;

	@Override
	@SuppressLint("RtlHardcoded")
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();

		final int parentLeft = getPaddingLeftWithForeground();
		final int parentRight = r - l - getPaddingRightWithForeground();

		final int parentTop = getPaddingTopWithForeground();
		final int parentBottom = b - t - getPaddingBottomWithForeground();

		for (int i = 0; i < count; ++i) {
			final View child = getChildAt(i);

			if (child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				if (lp.nodeType == LayoutParams.NODE_TYPE_DEFAULT) {
					int childLeft;

					final int width = child.getMeasuredWidth();
					final int horizontalGravity;

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						horizontalGravity = GravityCompat.getAbsoluteGravity(lp.gravity,
							getLayoutDirection());
					}
					else {
						horizontalGravity = lp.gravity;
					}
					switch (horizontalGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
						case Gravity.RIGHT:
							childLeft = parentRight - width - lp.rightMargin;
							break;
						case Gravity.LEFT:
							childLeft = parentLeft + lp.leftMargin;
							break;
						default:
							childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
								lp.leftMargin - lp.rightMargin;
					}
					int childTop;

					final int height = child.getMeasuredHeight();
					final int verticalGravity = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;

					switch (verticalGravity) {
						case Gravity.TOP:
							childTop = parentTop + lp.topMargin;
							break;
						case Gravity.BOTTOM:
							childTop = parentBottom - height - lp.bottomMargin;
							break;
						default:
							childTop = parentTop + (parentBottom - parentTop - height) / 2 +
								lp.topMargin - lp.bottomMargin;
					}
					child.layout(childLeft, childTop, childLeft + width, childTop + height);
				}
				else {
					drawerChildren.put(lp.nodeType, child);
				}
			}
		}
		if (drawerOpenTask != null) {
			drawerOpenTask.run();
		}
		drawer.layout(drawerChildren, parentLeft, parentTop, parentRight, parentBottom);
		drawerChildren.clear();
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	public static final class LayoutParams extends MarginLayoutParams {

		public static final int NODE_TYPE_DEFAULT        = 0;
		public static final int NODE_TYPE_DRAWER_CONTENT = 1;
		public static final int NODE_TYPE_DRAWER_HANDLE  = 2;

		public final int gravity;
		public final int nodeType;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.DrawerLayout_LayoutParams);
			{
				this.gravity = a.getInt(
					R.styleable.DrawerLayout_LayoutParams_android_layout_gravity, GRAVITY_DEFAULT);
				//noinspection WrongConstant
				this.nodeType = a.getInt(R.styleable.DrawerLayout_LayoutParams_nodeType,
					NODE_TYPE_DEFAULT);
			}
			a.recycle();
		}

		public LayoutParams(int width, int height) {
			super(width, height);

			this.gravity = GRAVITY_DEFAULT;
			this.nodeType = NODE_TYPE_DEFAULT;
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);

			int nodeType = NODE_TYPE_DEFAULT;

			if (source instanceof LayoutParams) {
				LayoutParams lp = ((LayoutParams) source);

				nodeType = lp.nodeType;
			}
			this.gravity = GRAVITY_DEFAULT;
			this.nodeType = nodeType;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);

			int nodeType = NODE_TYPE_DEFAULT;

			if (source instanceof LayoutParams) {
				LayoutParams lp = ((LayoutParams) source);

				nodeType = lp.nodeType;
			}
			this.gravity = GRAVITY_DEFAULT;
			this.nodeType = nodeType;
		}
	}
}
