package hey.rich.countit;

import java.util.ArrayList;
import java.util.List;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Timer extends StandOutWindow {

	private static final String LOG_TAG = "Timer";

	private TextView mTimerText;
	private ImageView mStartStopView;
	private ImageView mLapResetView;
	private ImageView mCloseView;

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

	private int mId;

	@Override
	public String getAppName() {
		return "Timer";
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_more;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		mId = id;
		Log.d(LOG_TAG, "Created timer with id: " + id);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_timer, frame, true);

		mStartStopView = (ImageView) view
				.findViewById(R.id.imageButtonPlayStop);
		mLapResetView = (ImageView) view.findViewById(R.id.imageButtonLapReset);
		mCloseView = (ImageView) view.findViewById(R.id.timer_close);

		// Set default state to stoped
		mCurrentState = STATE.STOPPED;

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
		mCurrentTime = 0L;
		updateTimer();
		mCurrentTime = 0L;
	}

	// Called before this window is closed
	@Override
	public boolean onClose(int id, Window window){
		// State should be stoped
		mCurrentState = STATE.STOPPED;
		// update Timer one last time
		updateTimer();
		return false;
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

		mStartStopView.setOnClickListener(new View.OnClickListener() {

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

		mLapResetView.setOnClickListener(new View.OnClickListener() {

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

		mCloseView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Close the window he-yo
				close(mId);
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
			mStartStopView.setImageResource(android.R.drawable.ic_media_pause);
			mLapResetView.setImageResource(android.R.drawable.ic_menu_save);

		} else if (mCurrentState == STATE.STOPPED) {
			mStartStopView.setImageResource(android.R.drawable.ic_media_play);
			mLapResetView.setImageResource(android.R.drawable.ic_menu_revert);
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

	// Make every window essentially the same size
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		return new StandOutLayoutParams(id, 400, 300,
				StandOutLayoutParams.CENTER,
				StandOutLayoutParams.CENTER, 100, 100);
	}

	// / We want system window decorations, we want to drag the body, we want
	// the ability to hide windows, and we want to tap the window to bring to
	// front
	@Override
	public int getFlags(int id) {
		return StandOutFlags.FLAG_BODY_MOVE_ENABLE
				| StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
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

	@Override
	public Animation getShowAnimation(int id) {
		if (isExistingId(id)) {
			// restore
			return AnimationUtils.loadAnimation(this,
					android.R.anim.slide_in_left);
		} else {
			// show
			return super.getShowAnimation(id);
		}
	}

	@Override
	public Animation getHideAnimation(int id) {
		return AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right);
	}

	@Override
	public List<DropDownListItem> getDropDownItems(int id) {
		List<DropDownListItem> items = new ArrayList<DropDownListItem>();
		items.add(new DropDownListItem(android.R.drawable.ic_menu_help,
				"About", new Runnable() {
					@Override
					public void run() {
						Toast.makeText(Timer.this,
								getAppName() + " is a timer!",
								Toast.LENGTH_SHORT).show();
					}
				}));
		items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences,
				"Settings", new Runnable() {
					@Override
					public void run() {
						// TODO: Add some settings here!
						Toast.makeText(Timer.this, "No settings yet",
								Toast.LENGTH_SHORT).show();
					}
				}));
		return items;
	}

}
