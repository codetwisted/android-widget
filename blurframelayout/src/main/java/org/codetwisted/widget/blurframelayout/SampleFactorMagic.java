package org.codetwisted.widget.blurframelayout;

import android.support.annotation.IntDef;

@IntDef(value = {
	BlurPanelExtension.SAMPLE_FACTOR_NONE,
	BlurPanelExtension.SAMPLE_FACTOR_4,
	BlurPanelExtension.SAMPLE_FACTOR_8,
	BlurPanelExtension.SAMPLE_FACTOR_16,
	BlurPanelExtension.SAMPLE_FACTOR_32
})
public @interface SampleFactorMagic {
}
