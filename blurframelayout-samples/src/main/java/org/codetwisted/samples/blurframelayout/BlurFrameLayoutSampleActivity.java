package org.codetwisted.samples.blurframelayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import org.codetwisted.widget.BlurFrameLayout;
import org.codetwisted.widget.KernelBasedAlgorithm;
import org.codetwisted.widget.extension.BlurAlgorithmProvider;

public class BlurFrameLayoutSampleActivity extends AppCompatActivity {

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blur_a);

		final BlurFrameLayout blurFrameLayout = (BlurFrameLayout)findViewById(R.id.frame_blur_layout);

		SeekBar seekBlurAmount = (SeekBar)findViewById(R.id.seek_blur_amount);
		seekBlurAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				BlurAlgorithmProvider blurProvider = blurFrameLayout.getBlurAlgorithmProvider();

				if (blurProvider instanceof KernelBasedAlgorithm) {
					((KernelBasedAlgorithm)blurProvider).setRadius(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				/* onStartTrackingTouch method stub */
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				/* onStopTrackingTouch method stub */
			}
		});
	}
}
