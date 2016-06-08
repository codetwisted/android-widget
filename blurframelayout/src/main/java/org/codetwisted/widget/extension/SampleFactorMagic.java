package org.codetwisted.widget.extension;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {
	BlurPanelExtension.SAMPLE_FACTOR_NONE,
	BlurPanelExtension.SAMPLE_FACTOR_4,
	BlurPanelExtension.SAMPLE_FACTOR_8,
	BlurPanelExtension.SAMPLE_FACTOR_16,
	BlurPanelExtension.SAMPLE_FACTOR_32
})
@Retention(RetentionPolicy.SOURCE)
public @interface SampleFactorMagic {
}
