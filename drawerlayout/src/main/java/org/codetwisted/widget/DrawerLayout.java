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
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
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
			if (!hasState(STATE_IDLE)) {
				setDrawerOpenImpl(false, false);
			}
			drawer = getDrawerImpl(this.gravity = gravity);
			drawer.reset();

			requestLayout();
		}
	}

	public int getGravity() {
		return gravity;
	}


	private boolean touchEnabled = true;

	public void setTouchEnabled(boolean touchEnabled) {
		this.touchEnabled = touchEnabled;
	}

	public boolean isTouchEnabled() {
		return touchEnabled;
	}


	private static final int STATE_FLAG_MASK     = 0xff << 24;
	private static final int STATE_FLAG_MOVING   = 0x01 << 24;
	private static final int STATE_FLAG_OPENING  = 0x02 << 24;
	private static final int STATE_FLAG_CLOSING  = 0x04 << 24;
	private static final int STATE_FLAG_ANIMATED = 0x08 << 24;

	private static final int STATE_IDLE     = 0x00;
	private static final int STATE_HELD     = 0x01;
	private static final int STATE_RELEASED = 0x02;


	private int state = STATE_IDLE;

	private boolean hasState(int state) {
		return (this.state & ~STATE_FLAG_MASK) == state;
	}

	private boolean hasStateFlag(int stateFlag) {
		return (this.state & stateFlag) != 0;
	}


	private void setState(int state) {
		setState(state, (this.state & STATE_FLAG_MASK));
	}

	private void setState(int state, int flags) {
		this.state = state | flags;
	}

	private void addStateFlag(int stateFlag) {
		this.state |= stateFlag;
	}

	private void removeStateFlag(int stateFlag) {
		this.state &= ~stateFlag;
	}


	private boolean drawerOpen;

	public boolean isDrawerOpen() {
		return drawerOpen;
	}

	public void setDrawerOpen(boolean drawerOpen, boolean animated) {
		setDrawerOpenImpl(drawerOpen, animated);
		requestLayout();
	}

	private void setDrawerOpenImpl(boolean drawerOpen, boolean animated) {
		animator.cancel();
		setState(STATE_IDLE, 0);

		if (drawerOpen) {
			dispatchDrawerOpening();
		}
		else {
			dispatchDrawerClosing();
		}
		drawer.setOpen(drawerOpen, animated);

		if (!animated) {
			if (drawerOpen) {
				dispatchDrawerOpen();
			}
			else {
				dispatchDrawerClosed();
			}
			setState(STATE_IDLE, 0);
		}
	}


	private float drawerOffset;

	public float getDrawerOffset() {
		return drawerOffset;
	}

	public void setDrawerOffset(float drawerOffset) {
		if (this.drawerOffset != drawerOffset) {
			if (!hasState(STATE_IDLE)) {
				setDrawerOpenImpl(false, false);
			}
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
		animator.setInterpolator(new LinearOutSlowInInterpolator());
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				drawer.onAnimationUpdate(animation);
				requestLayout();
			}
		});
		animator.addListener(new ValueAnimator.AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				addStateFlag(STATE_FLAG_ANIMATED);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				drawer.onAnimationEnd(animation);

				if (hasState(STATE_RELEASED)) {
					setState(STATE_IDLE, 0);
				}
				removeStateFlag(STATE_FLAG_ANIMATED
					| STATE_FLAG_CLOSING | STATE_FLAG_OPENING);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				/* Nothing to do */
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				throw new RuntimeException("Do not repeat animator!");
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

		void reset();
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
		drawerOpen = true;

		removeStateFlag(STATE_FLAG_CLOSING);
		if (!hasStateFlag(STATE_FLAG_OPENING)) {
			if (listener != null) {
				listener.onDrawerStartOpening();
			}
			addStateFlag(STATE_FLAG_OPENING);
		}
	}

	private void dispatchDrawerOpen() {
		if (listener != null) {
			listener.onDrawerOpened();
		}
		// state = STATE_IDLE; - this shouldn't be placed here,
		// as we prevent handle from being pulled when it is still in grip.
	}

	private void dispatchDrawerClosing() {
		removeStateFlag(STATE_FLAG_OPENING);
		if (!hasStateFlag(STATE_FLAG_CLOSING)) {
			if (listener != null) {
				listener.onDrawerStartClosing();
			}
			addStateFlag(STATE_FLAG_CLOSING);
		}
	}

	private void dispatchDrawerClosed() {
		drawerOpen = false;

		if (listener != null) {
			listener.onDrawerClosed();
		}
		// state = STATE_IDLE; - this shouldn't be placed here,
		// as we prevent handle from being pulled when it is still in grip.
	}

	private void dispatchDrawerSliding(int current, int from, int to) {
		if (listener != null) {
			listener.onDrawerSliding(current, from, to);
		}
	}


	private View handle;

	private final Rect handleRect  = new Rect();
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


		private boolean measured;

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

				if (!measured) {
					contentRightCurrent = isDrawerOpen() ? contentRightMax : contentRightMin;
					measured = true;
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

					int padding = touchSlop;

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
		}


		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getX(pointerIndex) - x;

			if (Math.abs(diff) > touchSlop) {
				if (hasStateFlag(STATE_FLAG_ANIMATED)) {
					animator.cancel();
				}
				int contentRightCurrentNew = Math.max(
					Math.min(contentLeftFixed + Math.round(diff), contentRightMax),
					contentRightMin);

				if (contentRightCurrent != contentRightCurrentNew) {
					addStateFlag(STATE_FLAG_MOVING);

					if (contentRightCurrent < contentRightCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}
					contentRightCurrent = contentRightCurrentNew;

					dispatchDrawerSliding(contentRightCurrent, contentRightMin, contentRightMax);
					requestLayout();

					velocityTracker.addMovement(ev);
				}
				else if (hasStateFlag(STATE_FLAG_MOVING)) {
					if (!(contentRightMin < contentRightCurrent // preserve new line
						&& contentRightCurrent < contentRightMax)) {

						onAnimationEnd(null);
						removeStateFlag(STATE_FLAG_MOVING);
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			float velocity;
			{
				velocityTracker.computeCurrentVelocity(1000);

				velocity = velocityTracker.getXVelocity(pointerId);
			}
			velocityTracker.recycle();

			float velocityDirection = Math.signum(velocity);
			float velocityPower = velocity / flingVelocityMinimum;

			int contentRightTarget, distance;

			if (velocityPower > 1.32f) {
				contentRightTarget = contentRightMax;
				distance = contentRightMax - contentRightCurrent;
			}
			else if (velocityPower < -.96f) {
				contentRightTarget = contentRightMin;
				distance = contentRightCurrent - contentRightMin;
			}
			else {
				if (velocityDirection > 0 ^ contentRightCurrent > contentRightThreshold) {
					contentRightTarget = contentRightMax;
					distance = contentRightMax - contentRightCurrent;
				}
				else {
					contentRightTarget = contentRightMin;
					distance = contentRightCurrent - contentRightMin;
				}
			}
			removeStateFlag(STATE_FLAG_MOVING);

			animator.setIntValues(contentRightCurrent, contentRightTarget);
			animator.setDuration(getAnimationDuration(velocity, distance));
			animator.start();

			if (contentRightTarget == contentRightMax) {
				dispatchDrawerOpening();
			}
			else if (contentRightTarget == contentRightMin) {
				dispatchDrawerClosing();
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentRightCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentRightCurrent, contentRightMin, contentRightMax);
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
				animator.setIntValues(contentRightCurrent,
					open ? contentRightMax : contentRightMin);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentRightCurrent = open ? contentRightMax : contentRightMin;
			}
		}

		@Override
		public void reset() {
			measured = false;
		}
	}

	private final class TopDrawerImpl implements DrawerImpl {

		private int contentBottomCurrent;
		private int contentBottomMin;
		private int contentBottomMax;
		private int contentBottomMiddle;

		private int contentBottomThreshold;


		private boolean measured;

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
			}
			if (!measured) {
				contentBottomCurrent = isDrawerOpen() ? contentBottomMax : contentBottomMin;
				measured = true;
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
						int padding = touchSlop;

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
		}


		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getY(pointerIndex) - y;

			if (Math.abs(diff) > touchSlop) {
				if (hasStateFlag(STATE_FLAG_ANIMATED)) {
					animator.cancel();
				}
				int contentBottomCurrentNew = Math.max(
					Math.min(contentBottomFixed + Math.round(diff), contentBottomMax),
					contentBottomMin);

				if (contentBottomCurrent != contentBottomCurrentNew) {
					addStateFlag(STATE_FLAG_MOVING);

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
				}
				else if (hasStateFlag(STATE_FLAG_MOVING)) {
					if (!(contentBottomMin < contentBottomCurrent // preserve new line
						&& contentBottomCurrent < contentBottomMax)) {

						onAnimationEnd(null);
						removeStateFlag(STATE_FLAG_MOVING);
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			float velocity;
			{
				velocityTracker.computeCurrentVelocity(1000);

				velocity = velocityTracker.getYVelocity(pointerId);
			}
			velocityTracker.recycle();

			float velocityDirection = Math.signum(velocity);
			float velocityPower = velocity / flingVelocityMinimum;

			int contentBottomTarget, distance;

			if (velocityPower > 1.32f) {
				contentBottomTarget = contentBottomMax;
				distance = contentBottomMax - contentBottomCurrent;
			}
			else if (velocityPower < -.96f) {
				contentBottomTarget = contentBottomMin;
				distance = contentBottomCurrent - contentBottomMin;
			}
			else {
				if (velocityDirection > 0 ^ contentBottomCurrent > contentBottomThreshold) {
					contentBottomTarget = contentBottomMax;
					distance = contentBottomMax - contentBottomCurrent;
				}
				else {
					contentBottomTarget = contentBottomMin;
					distance = contentBottomCurrent - contentBottomMin;
				}
			}
			removeStateFlag(STATE_FLAG_MOVING);

			animator.setIntValues(contentBottomCurrent, contentBottomTarget);
			animator.setDuration(getAnimationDuration(velocity, distance));
			animator.start();

			if (contentBottomTarget == contentBottomMax) {
				dispatchDrawerOpening();
			}
			else if (contentBottomTarget == contentBottomMin) {
				dispatchDrawerClosing();
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentBottomCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentBottomCurrent, contentBottomMin, contentBottomMax);
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
				animator.setIntValues(contentBottomCurrent,
					open ? contentBottomMax : contentBottomMin);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentBottomCurrent = open ? contentBottomMax : contentBottomMin;
			}
		}

		@Override
		public void reset() {
			measured = false;
		}
	}

	private final class RightDrawerImpl implements DrawerImpl {

		private int contentLeftCurrent;
		private int contentLeftMax;
		private int contentLeftMin;
		private int contentLeftMiddle;

		private int contentLeftThreshold;


		private boolean measured;

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

				if (!measured) {
					contentLeftCurrent = isDrawerOpen() ? contentLeftMin : contentLeftMax;
					measured = true;
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

					int padding = touchSlop;

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
		}


		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getX(pointerIndex) - x;

			if (Math.abs(diff) > touchSlop) {
				if (hasStateFlag(STATE_FLAG_ANIMATED)) {
					animator.cancel();
				}
				int contentLeftCurrentNew = Math.max(
					Math.min(contentLeftFixed + Math.round(diff), contentLeftMax), contentLeftMin);

				if (contentLeftCurrent != contentLeftCurrentNew) {
					addStateFlag(STATE_FLAG_MOVING);

					if (contentLeftCurrent > contentLeftCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}
					contentLeftCurrent = contentLeftCurrentNew;

					dispatchDrawerSliding(contentLeftCurrent, contentLeftMin, contentLeftMax);
					requestLayout();

					velocityTracker.addMovement(ev);
				}
				else if (hasStateFlag(STATE_FLAG_MOVING)) {
					if (!(contentLeftMin < contentLeftCurrent // preserve new line
						&& contentLeftCurrent < contentLeftMax)) {

						onAnimationEnd(null);
						removeStateFlag(STATE_FLAG_MOVING);
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			float velocity;
			{
				velocityTracker.computeCurrentVelocity(1000);

				velocity = velocityTracker.getXVelocity(pointerId);
			}
			velocityTracker.recycle();

			float velocityDirection = Math.signum(velocity);
			float velocityPower = velocity / flingVelocityMinimum;

			int contentLeftTarget, distance;

			if (velocityPower > .96f) {
				contentLeftTarget = contentLeftMax;
				distance = contentLeftMax - contentLeftCurrent;
			}
			else if (velocityPower < -1.32f) {
				contentLeftTarget = contentLeftMin;
				distance = contentLeftCurrent - contentLeftMin;
			}
			else {
				if (velocityDirection > 0 ^ contentLeftCurrent > contentLeftThreshold) {
					contentLeftTarget = contentLeftMax;
					distance = contentLeftMax - contentLeftCurrent;
				}
				else {
					contentLeftTarget = contentLeftMin;
					distance = contentLeftCurrent - contentLeftMin;
				}
			}
			removeStateFlag(STATE_FLAG_MOVING);

			animator.setIntValues(contentLeftCurrent, contentLeftTarget);
			animator.setDuration(getAnimationDuration(velocity, distance));
			animator.start();

			if (contentLeftTarget == contentLeftMin) {
				dispatchDrawerOpening();
			}
			else if (contentLeftTarget == contentLeftMax) {
				dispatchDrawerClosing();
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentLeftCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentLeftCurrent, contentLeftMin, contentLeftMax);
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
				animator.setIntValues(contentLeftCurrent, open ? contentLeftMin : contentLeftMax);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentLeftCurrent = open ? contentLeftMin : contentLeftMax;
			}
		}

		@Override
		public void reset() {
			measured = false;
		}
	}

	private final class BottomDrawerImpl implements DrawerImpl {

		private int contentTopCurrent;
		private int contentTopMax;
		private int contentTopMin;
		private int contentTopMiddle;

		private int contentTopThreshold;


		private boolean measured;

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

				if (!measured) {
					contentTopCurrent = isDrawerOpen() ? contentTopMin : contentTopMax;
					measured = true;
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

					int padding = touchSlop;

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
		}


		@Override
		public void handlePull(int pointerIndex, int pointerId, MotionEvent ev) {
			float diff = ev.getY(pointerIndex) - y;

			if (Math.abs(diff) > touchSlop) {
				if (hasStateFlag(STATE_FLAG_ANIMATED)) {
					animator.cancel();
				}
				int contentTopCurrentNew = Math.max(
					Math.min(contentTopFixed + Math.round(diff), contentTopMax), contentTopMin);

				if (contentTopCurrent != contentTopCurrentNew) {
					addStateFlag(STATE_FLAG_MOVING);

					if (contentTopCurrent > contentTopCurrentNew) {
						dispatchDrawerOpening();
					}
					else {
						dispatchDrawerClosing();
					}
					contentTopCurrent = contentTopCurrentNew;

					dispatchDrawerSliding(contentTopCurrent, contentTopMin, contentTopMax);
					requestLayout();

					velocityTracker.addMovement(ev);
				}
				else if (hasStateFlag(STATE_FLAG_MOVING)) {
					if (!(contentTopMin < contentTopCurrent // preserve new line
						&& contentTopCurrent < contentTopMax)) {

						onAnimationEnd(null);
						removeStateFlag(STATE_FLAG_MOVING);
					}
					velocityTracker.clear();
				}
			}
		}


		@Override
		public void handleFree(int pointerIndex, int pointerId, MotionEvent ev) {
			float velocity;
			{
				velocityTracker.computeCurrentVelocity(1000);

				velocity = velocityTracker.getYVelocity(pointerId);
			}
			velocityTracker.recycle();

			float velocityDirection = Math.signum(velocity);
			float velocityPower = velocity / flingVelocityMinimum;

			int contentTopTarget, distance;

			if (velocityPower > .96f) {
				contentTopTarget = contentTopMax;
				distance = contentTopMax - contentTopCurrent;
			}
			else if (velocityPower < -1.32f) {
				contentTopTarget = contentTopMin;
				distance = contentTopCurrent - contentTopMin;
			}
			else {
				if (velocityDirection > 0 ^ contentTopCurrent > contentTopThreshold) {
					contentTopTarget = contentTopMax;
					distance = contentTopMax - contentTopCurrent;
				}
				else {
					contentTopTarget = contentTopMin;
					distance = contentTopCurrent - contentTopMin;
				}
			}
			removeStateFlag(STATE_FLAG_MOVING);

			animator.setIntValues(contentTopCurrent, contentTopTarget);
			animator.setDuration(getAnimationDuration(velocity, distance));
			animator.start();

			if (contentTopTarget == contentTopMin) {
				dispatchDrawerOpening();
			}
			else if (contentTopTarget == contentTopMax) {
				dispatchDrawerClosing();
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			contentTopCurrent = (int) animation.getAnimatedValue();
			dispatchDrawerSliding(contentTopCurrent, contentTopMin, contentTopMax);
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
				animator.setIntValues(contentTopCurrent, open ? contentTopMin : contentTopMax);
				animator.setDuration(animationDuration);
				animator.start();
			}
			else {
				contentTopCurrent = open ? contentTopMin : contentTopMax;
			}
		}

		@Override
		public void reset() {
			measured = false;
		}
	}

	private long getAnimationDuration(float velocity, int distance) {
		return Math.min(Math.round(1000 * Math.sqrt(distance / Math.max(Math.abs(velocity), 1))),
			animationDuration);
	}


	private int pointerId;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (touchEnabled) {
			final int movementAction = ev.getAction();

			if (movementAction == MotionEvent.ACTION_DOWN
				|| movementAction == MotionEvent.ACTION_POINTER_DOWN) {

				boolean consumed = false;
				for (int i = ev.getPointerCount() - 1; i >= 0; --i) {
					int x = (int) ev.getX(i);
					int y = (int) ev.getY(i);

					boolean handleRectContains = handleRect.contains(x, y);
					boolean contentRectContains = contentRect.contains(x, y);

					consumed |= handleRectContains || contentRectContains;
					if (handleRectContains || seizeContent && contentRectContains) {
						drawer.handleGrip(i, pointerId = ev.getPointerId(i), ev);
						setState(STATE_HELD);
						return true;
					}
				}
				return consumed;
			}
			return super.onInterceptTouchEvent(ev);
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (touchEnabled) {
			if (hasState(STATE_HELD)) {
				int pointerIndex = event.findPointerIndex(pointerId);

				if (pointerIndex >= 0) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_MOVE:
							drawer.handlePull(pointerIndex, pointerId, event);
							break;
						case MotionEvent.ACTION_POINTER_UP:
						case MotionEvent.ACTION_UP:
							if (hasStateFlag(STATE_FLAG_MOVING)) {
								drawer.handleFree(pointerIndex, pointerId, event);
								setState(STATE_RELEASED);
							}
							else {
								if (!hasStateFlag(STATE_FLAG_CLOSING | STATE_FLAG_OPENING)) {
									setState(STATE_IDLE, 0);
									performHandleClick(handle, event, pointerIndex);
								}
								else {
									setState(STATE_IDLE);
								}
							}
							return false;
					}
				}
				return hasState(STATE_HELD);
			}
			return super.onTouchEvent(event);
		}
		return false;
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
		drawer.layout(drawerChildren, parentLeft, parentTop, parentRight, parentBottom);
		drawerChildren.clear();
	}

	@Override
	public void onViewAdded(View child) {
		super.onViewAdded(child);
		drawer.reset();
	}

	@Override
	public void onViewRemoved(View child) {
		super.onViewRemoved(child);
		drawer.reset();
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
