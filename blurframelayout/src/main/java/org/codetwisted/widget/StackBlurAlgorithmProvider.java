package org.codetwisted.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.codetwisted.widget.extension.BlurAlgorithmProvider;
import org.codetwisted.widget.extension.BlurPanelExtension;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class StackBlurAlgorithmProvider implements BlurAlgorithmProvider, KernelBasedAlgorithm {

	static {
		System.loadLibrary("crystax");
		System.loadLibrary("stack-blur");
	}

	@SuppressWarnings("JniMissingFunction")
	public static native void functionToBlur(Bitmap bitmapOut, int radius, int threadCount, int threadIndex, int round);


	private BlurPanelExtension blurPanelExtension;

	@Override
	public void associate(@NonNull BlurPanelExtension blurPanelExtension) {
		this.blurPanelExtension = blurPanelExtension;
	}


	private boolean isInitialized;

	@Override
	public void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle,
		int defStyleRes) {

		float blurRadius = 0;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.KernelBasedBlurAlgorithm, defStyle, defStyleRes);
			{
				blurRadius = a.getFloat(R.styleable.KernelBasedBlurAlgorithm_blurRadius,
					blurRadius);
			}
			a.recycle();
		}
		setRadius(blurRadius);

		isInitialized = true;
	}


	private Buffer drawingCacheBuffer;

	@Override
	public void onDrawingCacheUpdate(@NonNull Bitmap drawingCache) {
		if (this.drawingCacheBuffer != null) {
			drawingCacheBuffer.clear();
		}
		drawingCacheBuffer = ByteBuffer.allocateDirect(drawingCache.getByteCount());
		drawingCache.copyPixelsToBuffer(drawingCacheBuffer);
	}


	private int radius;

	@Override
	public void blur(@NonNull Bitmap bitmapPanel) {
		drawingCacheBuffer.rewind();
		bitmapPanel.copyPixelsFromBuffer(drawingCacheBuffer);

		functionToBlur(bitmapPanel, radius, 1, 0, 1);
		functionToBlur(bitmapPanel, radius, 1, 1, 2);
	}

	@Override
	public boolean isDisabled() {
		return radius <= 0;
	}

	@Override
	public void setRadius(float radius) {
		this.radius = Math.round(radius);

		if (isInitialized) {
			blurPanelExtension.invalidateBlur(true);
		}
	}

	@Override
	public float getRadius() {
		return radius;
	}
}
