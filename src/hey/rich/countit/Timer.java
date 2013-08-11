package hey.rich.countit;

import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Timer extends Activity {

	private static final String LOG_TAG = "Timer";
	private static final String CURRENT_TIMER_VALUE = "current_timer_value";
	private static final String SHARED_PREFERENCES = "shared_preferences_name";
	private static final String RUNNING_STATE = STATE.RUNNING.toString();

	private TextView mTimerText;
	private ImageButton mStartStopButton;
	private ImageButton mLapResetButton;

	private SharedPreferences mPrefs;

	/** Contains various states application can be in */
	private static enum STATE {
		RUNNING, STOPPED;
	}

	/**
	 * If true when {@link updateValue()} is called and {@link mCurretState} is
	 * set to STATE.STOPPED, current time will be cleared/reset to 0
	 */
	private static boolean mClearTimer = false;

	/** Current state of the application */
	private static STATE mCurrentState;

	private long mStartTime = 0L;
	private long mCurrentTime = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		mPrefs = getSharedPreferences(SHARED_PREFERENCES, 0);

		mCurrentState = (mPrefs.getBoolean(RUNNING_STATE, false)) ? STATE.RUNNING
				: STATE.STOPPED;

		setUpViewElements();

		mCurrentTime = mPrefs.getLong(CURRENT_TIMER_VALUE, 0L);
		updateTime(mCurrentTime);

		// TODO: Check if we have any saved data and then load the correct state
		updateTimer();
	}

	/**
	 * Sets up all of the elements in the current view.
	 * <p>
	 * Currently this is just: stop/start button, lap/reset button, and the
	 * actual timer (Chronometer).
	 * <p>
	 * In the future there will be: ListView that contains the actual "laps"
	 */
	private void setUpViewElements() {
		// Set the format of the timer
		// TODO: make this customizable
		mStartStopButton = (ImageButton) findViewById(R.id.imageButtonPlayStop);
		mLapResetButton = (ImageButton) findViewById(R.id.imageButtonLapReset);

		updateButtonIcons();

		mTimerText = (TextView) findViewById(R.id.timer);

		mStartStopButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: UPDATE BUTTONS HAHA
				// Check state
				if (mCurrentState == STATE.STOPPED) {

					mCurrentState = STATE.RUNNING;

					updateTimer();

				} else if (mCurrentState == STATE.RUNNING) {

					mCurrentState = STATE.STOPPED;
					updateTimer();

				} else {
					// Invalid state
					throw new RuntimeException("Invalid state: "
							+ mCurrentState.toString());
				}

			}
		});

		mLapResetButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Check state
				if (mCurrentState == STATE.STOPPED) {
					// Reset time
					if (!mClearTimer) {
						// Only allow user to reset time if it is not already
						// reset
						mClearTimer = true;
						updateTimer();
					}
				} else if (mCurrentState == STATE.RUNNING) {
					// Create a lap
					// TODO: implement lap things
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Using shared preferences because we want to save state if we're
		// killed
		SharedPreferences.Editor ed = mPrefs.edit();
		// TODO: Check if this will break with mashing buttons and rotating
		if (mCurrentState == STATE.RUNNING) {
			mCurrentTime = System.currentTimeMillis() - mStartTime;
		}
		ed.putLong(CURRENT_TIMER_VALUE, mCurrentTime);
		ed.putBoolean(RUNNING_STATE, (mCurrentState == STATE.RUNNING));
		ed.commit();
		// TODO: Investigate if I need to save the current state in here as
		// well?
	}

	// Timer
	private static java.util.Timer mTimer = new java.util.Timer();

	// TODO: Should be a much nicer way to do this
	private void updateTimer() {
		if (mCurrentState == STATE.RUNNING) {
			// TODO: Think of a nice way to: update clearing flag here as well
			// as only call this the first time we want to clear timer - not
			// calling this so many times.
			// Start the timer
			if (mClearTimer) {
				mStartTime = System.currentTimeMillis();
				mClearTimer = false;
			} else {
				mStartTime = System.currentTimeMillis() - mCurrentTime;
			}
			mTimer = new java.util.Timer();
			mTimer.schedule(new CustomTimerTask(), 0, 10);
		} else if (mCurrentState == STATE.STOPPED) {
			// Stopping timer
			if (mClearTimer) {
				mCurrentTime = 0L;
				mTimerText.setText(String.format("%d:%02d:%02d", 0, 0, 0));
			} else {
				mTimer.cancel();
				mTimer.purge();
				mCurrentTime = System.currentTimeMillis() - mStartTime;
			}

		} else {
			// Invalid state
			throw new RuntimeException("Invalid state: "
					+ mCurrentState.toString());
		}
		updateButtonIcons();
	}

	/** Updates the button icons based on state {@link mCurrentState} */
	private void updateButtonIcons() {
		if (mCurrentState == STATE.RUNNING) {
			mStartStopButton.setImageResource(android.R.drawable.ic_media_pause);
			mLapResetButton.setImageResource(android.R.drawable.ic_menu_save);

		} else if (mCurrentState == STATE.STOPPED) {
			mStartStopButton
					.setImageResource(android.R.drawable.ic_media_play);
			mLapResetButton.setImageResource(android.R.drawable.ic_menu_revert);
		} else {
			throw new RuntimeException("Invalid state: "
					+ mCurrentState.toString());
		}
	}

	/**
	 * Updates the timer Textview {@link mTimerText} based on
	 * {@link mCurrentTime}
	 */
	private void updateTime(long time) {
		int milliSeconds = (int) (time % 1000);
		milliSeconds /= 10;
		int seconds = (int) (time / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;

		updateTimerText(minutes, seconds, milliSeconds);

	}

	final Handler h = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			long millis = System.currentTimeMillis() - mStartTime;
			// Only care about every 10 milliseconds
			updateTime(millis);

			return false;
		}
	});

	/**
	 * Updates the text view {@link mTimerText} in the specified format. TODO:
	 * Multiple formats here!
	 * 
	 * @param minutes
	 *            Number of minutes
	 * @param seconds
	 *            Number of seconds
	 * @param milliSeconds
	 *            Number of milliSeconds
	 */
	private void updateTimerText(long minutes, long seconds, long milliSeconds) {
		if (mTimerText != null) {
			mTimerText.setText(String.format("%d:%02d:%02d", minutes, seconds,
					milliSeconds));
		}
	}

	// tells handler to send a message
	class CustomTimerTask extends TimerTask {
		@Override
		public void run() {
			h.sendEmptyMessage(0);
		}
	}
}
