package hey.rich.floating;

import hey.rich.Formats.Formats;
import hey.rich.Formats.TimerFormat;
import hey.rich.countit.R;

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
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Timer extends StandOutWindow {

	private static final String TAG = "Timer";
	public static final int DEFAULT_ID = 1;

	// Timer view elements
	private TextView mTimerText;

	/** Contains various states application can be in */
	private static enum STATE {
		RUNNING, STOPPED;
	}

	/**
	 * If true when {@link updateValue()} is called and {@link mCurretState} is
	 * set to STATE.STOPPED, current time will be cleared/reset to 0
	 */
	private boolean mClearTimer = false;

	/** Current state of the application */
	private STATE mCurrentState;

	private long mStartTime = 0L;
	private long mCurrentTime = 0L;

	private TimerFormat mTimerFormat;

	@Override
	public String getAppName() {
		return "Timer";
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_more;
	}

	final Handler h = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			long millis = System.currentTimeMillis() - mStartTime;
			// Only care about every 10 milliseconds
			mTimerText.setText(mTimerFormat.timeToString(millis));

			return false;
		}
	});

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		mTimerFormat = new TimerFormat(Formats.DEFAULT_FORMAT, true);

		Log.d(TAG, "Created timer with id: " + id);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_timer, frame, true);

		// Set default state to stopped
		mCurrentState = STATE.STOPPED;

		mTimerText = (TextView) view.findViewById(R.id.timer);
		mTimerText.setText(mTimerFormat.timeToString(0));

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
		mCurrentTime = 0L;
		updateTimer();
		mCurrentTime = 0L;
	}

	private void setUpViewElements() {
		mTimerText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
	}

	// Called before this window is closed
	@Override
	public boolean onClose(int id, Window window) {
		// State should be stopped
		mCurrentState = STATE.STOPPED;
		// update Timer one last time
		updateTimer();
		return false;
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
				mTimerText.setText(mTimerFormat.timeToString(0));
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
	}

	// tells handler to send a message
	class CustomTimerTask extends TimerTask {
		@Override
		public void run() {
			h.sendEmptyMessage(0);
		}
	}

	// Make every window essentially the same size
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		// TODO: Hardcode these values...don't
		return new StandOutLayoutParams(id, 350, 100,
				StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);
	}

	@Override
	public int getFlags(int id) {
		return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
				| StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return "Click to close " + getAppName();
	}

	@Override
	public String getPersistentNotificationTitle(int id) {
		return getAppName() + " Running";
	}

	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return StandOutWindow.getCloseIntent(this, Timer.class, id);
	}
}
