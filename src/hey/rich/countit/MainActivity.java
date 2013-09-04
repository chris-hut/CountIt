package hey.rich.countit;

import hey.rich.floating.Timer;
import wei.mark.standout.StandOutWindow;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String LOG_TAG = "MainActivity";

	private static boolean mTimerRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Timer isn't running currently
		mTimerRunning = false;
		setContentView(R.layout.activity_main);
		final Button launch = (Button) findViewById(R.id.launchFloatingTimerButon);
		launch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: manage the floating windows to allow multiple ones and
				// whatnot
				// Close any old floating windows
				if (mTimerRunning) {
					mTimerRunning = false;
					closeFloatingWindow();
					launch.setText(R.string.launch_floating_timer);
				} else {
					mTimerRunning = true;
					Log.d(LOG_TAG, "About to show floating window.");
					openFloatingWindow();
					launch.setText(R.string.close_floating_timer);
				}
			}
		});
	}

	private void closeFloatingWindow() {
		StandOutWindow.close(this, Timer.class, StandOutWindow.DEFAULT_ID);
	}

	private void openFloatingWindow() {
		StandOutWindow.show(this, Timer.class, StandOutWindow.DEFAULT_ID);
	}
}
