package hey.rich.countit;

import wei.mark.standout.StandOutWindow;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String LOG_TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button launch = (Button) findViewById(R.id.launchFloatingTimerButon);
		launch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: manage the floating windows to allow multiple ones and
				// whatnot
				// Close any old floating windows
				closeFloatingWindow();
				Log.d(LOG_TAG, "About to show floating window.");
				// Launch the floating window
				openFloatingWindow();
			}
		});
	}

	private void closeFloatingWindow() {
		StandOutWindow.closeAll(this, Timer.class);
	}

	private void openFloatingWindow() {
		StandOutWindow.show(this, new Timer().getClass(),
				StandOutWindow.DEFAULT_ID);
	}
}
