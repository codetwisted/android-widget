package org.codetwisted.samples.drawerlayout;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.codetwisted.widget.DrawerLayout;

import java.util.Arrays;

public class DrawerLayoutDebugActivity extends AppCompatActivity {

	private final int GRAVITY[] = {
		GravityCompat.START,
		Gravity.TOP,
		GravityCompat.END,
		Gravity.BOTTOM
	};

	private int gravityCurrentIndex;

	private DrawerLayout drawerLayout;

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawer_layout_a);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		gravityCurrentIndex = Arrays.binarySearch(GRAVITY, drawerLayout.getGravity());

		final View panelHandle = findViewById(R.id.group_panel_handle);
		panelHandle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.setDrawerOpen(!drawerLayout.isDrawerOpen(), true);
			}
		});

		final TextView textContentPlaceholder = (TextView) findViewById(R.id.text_content_placeholder);
		drawerLayout.setListener(new DrawerLayout.ListenerAdapter(){
			@Override
			public void onDrawerStartOpening() {
				textContentPlaceholder.setText("Open in progress...");
			}

			@Override
			public void onDrawerStartClosing() {
				textContentPlaceholder.setText("Close in progress...");
			}

			@Override
			public void onDrawerOpened() {
				textContentPlaceholder.setText("Opened");
			}

			@Override
			public void onDrawerClosed() {
				textContentPlaceholder.setText("Closed");
			}
		});

		View buttonSwitchGravity = findViewById(R.id.button_switch_drawer_gravity);
		buttonSwitchGravity.setOnClickListener(new View.OnClickListener() {
			@Override
			@SuppressWarnings("SuspiciousNameCombination")
			public void onClick(View v) {
				int gravity = GRAVITY[(++gravityCurrentIndex) % GRAVITY.length];

				DrawerLayout.LayoutParams lp
					= (DrawerLayout.LayoutParams) panelHandle.getLayoutParams();

				if (findGravityOrientation(gravity) != findGravityOrientation(drawerLayout.getGravity())) {
					int height = lp.width;

					lp.width = lp.height;
					lp.height = height;
				}
				drawerLayout.setGravity(gravity);
			}
		});

		SeekBar seekBar = (SeekBar) findViewById(R.id.seeker_drawer_offset);
		seekBar.setMax((int) (32 * getResources().getDisplayMetrics().density));
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				drawerLayout.setDrawerOffset(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				/* Nothing to do */
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				/* Nothing to do */
			}
		});

		EditText animationTime = (EditText) findViewById(R.id.edit_animation_time);
		animationTime.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				/* do nothing */
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				/* do nothing */
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					long time = Long.parseLong(s.toString());
					drawerLayout.setAnimationDuration(time);
				}
				catch (NumberFormatException e) {
					/* how could it be? */
				}
			}
		});
		animationTime.clearFocus();
	}

	private static int findGravityOrientation(int gravity) {
		return (gravity & Gravity.VERTICAL_GRAVITY_MASK) != 0
			? Gravity.VERTICAL_GRAVITY_MASK
			: GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (super.onCreateOptionsMenu(menu)) {
			getMenuInflater().inflate(R.menu.drawer_layout, menu);

			return true;
		}
		return false;
	}


	private boolean animated;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		animated = menu.findItem(R.id.menu_toggle_drawer_animated)
			.isChecked();

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_toggle_drawer:
				drawerLayout.setDrawerOpen(!drawerLayout.isDrawerOpen(), animated);
				return true;

			case R.id.menu_toggle_drawer_animated:
				item.setChecked(!item.isChecked());
				animated = item.isChecked();
				return true;

			case R.id.menu_drawer_content_seize:
				item.setChecked(!item.isChecked());
				drawerLayout.setSeizeContent(item.isChecked());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
