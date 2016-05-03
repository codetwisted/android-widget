package org.codetwisted.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.codetwisted.widget.extension.BlurAlgorithmProvider;
import org.codetwisted.widget.extension.BlurPanelExtension;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class IntrinsicBlurAlgorithmProvider implements BlurAlgorithmProvider, KernelBasedAlgorithm {

	public static final float KERNEL_RADIUS_MAX = 25f;


	private BlurPanelExtension blurPanelExtension;

	@Override
	public void associate(@NonNull BlurPanelExtension blurPanelExtension) {
		this.blurPanelExtension = blurPanelExtension;
	}


	private RenderScript        renderScript;
	private ScriptIntrinsicBlur blur;

	private boolean isInitialized;

	@Override
	public void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle,
		int defStyleRes) {

		renderScript = RenderScript.create(context);
		blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

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


	private Allocation input;

	@Override
	public void onDrawingCacheUpdate(@NonNull Bitmap drawingCache) {
		input = Allocation.createFromBitmap(renderScript, drawingCache,
			Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SCRIPT);

		if (!input.getElement()
			.isCompatible(Element.U8_4(renderScript))) {

			blur = ScriptIntrinsicBlur.create(renderScript, input.getElement());
			blur.setRadius(blurRadiusActual);
		}
	}


	private boolean disabled;

	@Override
	public void blur(@NonNull Bitmap bitmapPanel) {
		if (!disabled) {
			Allocation output = Allocation.createTyped(renderScript, input.getType());

			blur.setInput(input);
			blur.forEach(output);

			output.copyTo(bitmapPanel);
		}
	}

	@Override
	public boolean isDisabled() {
		return disabled;

	}


	private float blurRadius;
	private float blurRadiusActual;

	@Override
	public void setRadius(float blurRadius) {
		disabled = blurRadius <= 0;

		if (!disabled) {
			blur.setRadius(blurRadiusActual = Math.max(
				Math.min(blurPanelExtension.mapValueGivenSampleFactor(this.blurRadius = blurRadius),
					KERNEL_RADIUS_MAX), .1f));
		}
		if (isInitialized) {
			blurPanelExtension.invalidateBlur(true);
		}
	}

	@Override
	public float getRadius() {
		return blurRadius;
	}
}
