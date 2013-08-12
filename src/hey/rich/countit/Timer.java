package hey.rich.countit;

import java.util.TimerTask;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class Timer extends StandOutWindow {

	private static final String LOG_TAG = "Timer";

	private TextView mTimerText;
	private ImageButton mStartStopButton;
	private ImageButton mLapResetButton;

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
	public String getAppName() {
		return "FloatingTimerWindow";
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_close_clear_cancel;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		Log.d(LOG_TAG, "in createAndAttachView");
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_timer, frame, true);

		mStartStopButton = (ImageButton) view
				.findViewById(R.id.imageButtonPlayStop);
		mLapResetButton = (ImageButton) view
				.findViewById(R.id.imageButtonLapReset);

		// Set default state to stoped
		mCurrentState = STATE.STOPPED;

		updateButtonIcons();

		mTimerText = (TextView) view.findViewById(R.id.timer);

		setUpViewElements();

		/*
		 * When creating the timer, we don't want any "current time" as we are
		 * only ever going to be created from the button press in the main
		 * activity, if we are ever killed and then restored it is not currently
		 * expected of us to keep the previous value of the timer.
		 * 
		 * So we reset the current time to 0, if this is not present, when we
		 * update the timer from this call, we will be calling updateTimer with
		 * the state equal to STATE.STOPPED, this means that we will be saving
		 * the current time something we do not want or need to do - this leads
		 * to overflow and an incorrect timer value.
		 */
		updateTimer();
		mCurrentTime = 0L;
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

		mStartStopButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: UPDATE BUTTONS HAHA
				// Check state
				Log.d(LOG_TAG, "Clicked stop/start button");
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
				Log.d(LOG_TAG, "Clicked lap/reset button");
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
			if (mCurrentState == null) {
				throw new RuntimeException("Current state is null");
			}
			// Invalid state
			throw new RuntimeException("Invalid state: "
					+ mCurrentState.toString());
		}
		updateButtonIcons();
	}

	/** Updates the button icons based on state {@link mCurrentState} */
	private void updateButtonIcons() {
		if (mCurrentState == STATE.RUNNING) {
			mStartStopButton
					.setImageResource(android.R.drawable.ic_media_pause);
			mLapResetButton.setImageResource(android.R.drawable.ic_menu_save);

		} else if (mCurrentState == STATE.STOPPED) {
			mStartStopButton.setImageResource(android.R.drawable.ic_media_play);
			mLapResetButton.setImageResource(android.R.drawable.ic_menu_revert);
		} else {
			if (mCurrentState == null) {
				throw new RuntimeException("Current state is null");
			}
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

	// Center the window
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		return new StandOutLayoutParams(id, 250, 300,
				StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);
	}

	// Move the window by dragging the view
	@Override
	public int getFlags(int id) {
		return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
				| StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return getString(R.string.floating_notification_cancel_text);
	}

	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return StandOutWindow.getCloseIntent(this, Timer.class, id);
	}
}
