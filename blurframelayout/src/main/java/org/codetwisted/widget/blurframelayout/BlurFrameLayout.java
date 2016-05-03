package org.codetwisted.widget.blurframelayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class BlurFrameLayout extends FrameLayout {

	private static final String TAG = BlurFrameLayout.class.getName();


	public BlurFrameLayout(Context context) {
		super(context);

		initWidget(context, null, 0, 0);
	}

	public BlurFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		initWidget(context, attrs, 0, 0);
	}

	public BlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initWidget(context, attrs, defStyleAttr,0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public BlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		initWidget(context, attrs, defStyleAttr, defStyleRes);
	}


	@NonNull
	static BlurAlgorithmProvider pickBlurAlgorithmProvider() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return new IntrinsicBlurAlgorithmProvider();
		}
		// TODO: implement CPU-bound blur algorithm provider here, for instance:
		// https://github.com/kikoso/android-stackblur/tree/master/StackBlur
		throw new UnsupportedOperationException("Blur algorithm provider not found.");
	}


	private final BlurPanelExtension<BlurAlgorithmProvider> blurPanelExtension
		= new BlurPanelExtension<>(this, pickBlurAlgorithmProvider());

	private void initWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		blurPanelExtension.initStyleable(context, attrs, defStyleAttr, defStyleRes);
	}

	public BlurAlgorithmProvider getBlurAlgorithmProvider() {
		return blurPanelExtension.getBlurAlgorithmProvider();
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed) {
			blurPanelExtension.invalidateBlur(false);
		}
	}

	@Override
	protected void dispatchDraw(@NonNull Canvas canvas) {
		blurPanelExtension.drawBlur(canvas);

		super.dispatchDraw(canvas);
	}
}
