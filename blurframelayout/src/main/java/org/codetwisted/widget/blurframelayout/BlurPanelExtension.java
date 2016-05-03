package org.codetwisted.widget.blurframelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import org.codetwisted.internal.DebugUtils;
import org.codetwisted.extension.WidgetExtension;
import org.codetwisted.widget.R;

public class BlurPanelExtension<T extends BlurAlgorithmProvider> extends WidgetExtension<View> {

	private static final String TAG = BlurPanelExtension.class.getName();


	public static final int SAMPLE_FACTOR_NONE  = 0;
	public static final int SAMPLE_FACTOR_4     = 4;
	public static final int SAMPLE_FACTOR_8     = 8;
	public static final int SAMPLE_FACTOR_16    = 16;
	public static final int SAMPLE_FACTOR_32    = 32;


	private boolean isInitialized;

	@Override
	public void initStyleable(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle, int defStyleRes) {
		@SampleFactorMagic
		int sampleFactor = SAMPLE_FACTOR_NONE;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurPanelExtension, defStyle, defStyleRes);
			{
				//noinspection WrongConstant
				sampleFactor = a.getInteger(R.styleable.BlurPanelExtension_sampleFactor, sampleFactor);
			}
			a.recycle();
		}
		setSampleFactor(sampleFactor);

		blurAlgorithmProvider.associate(this);
		blurAlgorithmProvider.init(context, attrs, defStyle, defStyleRes);

		isInitialized = true;
	}


	private final T blurAlgorithmProvider;

	@NonNull
	public T getBlurAlgorithmProvider() {
		return blurAlgorithmProvider;
	}


	private float sampleFactor;
	private boolean engageSampling;

	private int sampleFactorCategory;

	public void setSampleFactor(@SampleFactorMagic int sampleFactor) {
		if (sampleFactorCategory != sampleFactor) {
			sampleFactorCategory = sampleFactor;

			if (engageSampling = sampleFactor != SAMPLE_FACTOR_NONE) {
				this.sampleFactor = 1f / sampleFactor;
			} else {
				this.sampleFactor = 0;
			}
			if (isInitialized) {
				invalidateBlur(false);
			}
		}
	}

	public int getSampleFactor() {
		return sampleFactorCategory;
	}


	public float mapValueGivenSampleFactor(float value) {
		return engageSampling ? value * sampleFactor : value;
	}


	private boolean dirty = true;

	public boolean isDirty() {
		return dirty;
	}


	private Bitmap blurCache;

	public void invalidateBlur(boolean fast) {
		if (blurCache != null) {
			if (!fast) {
				blurCache.recycle();

				blurCache = null;
			}
			dirty = true;
		}
		getInstanceExtended().invalidate();
	}


	private final Matrix matrix      = new Matrix();
	private final Paint  bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	public void drawBlur(@NonNull Canvas canvas) {
		if (!blurAlgorithmProvider.isDisabled()
				&& buildBlurCacheIfNecessary()) {
			updateBlurCache();

			int saveCount = canvas.save();
			{
				canvas.concat(matrix);
				canvas.drawBitmap(blurCache, 0, 0, bitmapPaint);
			}
			canvas.restoreToCount(saveCount);
		}
	}


	private boolean buildBlurCacheIfNecessary() {
		if (blurCache == null) {
			View instance = getInstanceExtended();

			int w = instance.getWidth(), h = instance.getHeight();

			if (w > 0 && h > 0) {
				ViewParent viewParent = instance.getParent();

				if (viewParent instanceof View) {
					View parent = (View) viewParent;

					DebugUtils.TraceSectionToken traceSectionToken = DebugUtils.beginTraceSection();

					instance.setVisibility(View.INVISIBLE);
					{
						boolean willNotCacheDrawing = parent.willNotCacheDrawing();

						parent.setWillNotCacheDrawing(false);
						parent.buildDrawingCache();

						Bitmap parentDrawingCache = parent.getDrawingCache();

						if (parentDrawingCache != null) {
							if (engageSampling) {
								matrix.reset();
								matrix.setScale(sampleFactor, sampleFactor);
								{
									blurCache = Bitmap.createBitmap(parentDrawingCache, instance.getLeft(), instance.getTop(), w, h, matrix, false);
								}
								matrix.invert(matrix);
							} else {
								blurCache = Bitmap.createBitmap(parentDrawingCache, instance.getLeft(), instance.getTop(), w, h);
							}
							blurAlgorithmProvider.onDrawingCacheUpdate(blurCache);
						}
						parent.destroyDrawingCache();
						parent.setWillNotCacheDrawing(willNotCacheDrawing);
					}
					instance.setVisibility(View.VISIBLE);

					Log.d(TAG, DebugUtils.endTraceSection(traceSectionToken).toString());
				}
			}
		}
		return blurCache != null;
	}

	private void updateBlurCache() {
		blurAlgorithmProvider.blur(blurCache);

		dirty = false;
	}


	public BlurPanelExtension(@NonNull View viewExtended, @NonNull T blurAlgorithmProvider) {
		super(viewExtended);

		this.blurAlgorithmProvider = blurAlgorithmProvider;
	}
}
