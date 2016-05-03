package org.codetwisted.extension;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class WidgetExtension<T extends View> extends ExtensionBase<T> {

	public abstract void initStyleable(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle, int defStyleRes);


	public WidgetExtension(@NonNull T viewExtended) {
		super(viewExtended);
	}


	protected Resources getResources() {
		return getInstanceExtended().getResources();
	}

}
