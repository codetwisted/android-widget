package org.codetwisted.widget.blurframelayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public interface BlurAlgorithmProvider {

	void associate(@NonNull BlurPanelExtension blurPanelExtension);

	void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle, int defStyleRes);


	void onDrawingCacheUpdate(@NonNull Bitmap drawingCache);

	void blur(@NonNull Bitmap bitmapPanel);


	boolean isDisabled();
}
