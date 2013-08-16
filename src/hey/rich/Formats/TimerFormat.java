package hey.rich.Formats;

public class TimerFormat {

	// Constants

	private String mFormat;
	private boolean mUpdateFormatWithTime;
	private boolean mCalculateHours;

	public TimerFormat() {
		this(Formats.DEFAULT_FORMAT, false);
		mFormat = Formats.DEFAULT_FORMAT;
	}

	public TimerFormat(String format) {
		// TODO: We are assuming that only nice formats will be passed into
		// here, check this in the future
		this(format, false);
	}

	public TimerFormat(String format, boolean update) {
		mFormat = format;
		mUpdateFormatWithTime = update;
		if (mFormat.equals(Formats.EXTENDED_FORMAT)) {
			mCalculateHours = true;
		} else {
			mCalculateHours = false;
		}
	}

	public String timeToString(long time) {
		int milliSeconds = (int) (time % 1000);
		// Only want every 10 milliseconds - we're not fast enough for better
		milliSeconds /= 10;
		int seconds = (int) (time / 1000);
		int minutes = seconds / 60;
		seconds %= 60;
		if (mCalculateHours) {
			mFormat = Formats.EXTENDED_FORMAT;
			int hours = minutes / 60;
			minutes %= 60;
			return String.format(mFormat, hours, minutes, seconds, milliSeconds);
		}
		
		if(mUpdateFormatWithTime){
			if(minutes >= 60){
				// More than 60 minutes have elapsed
				mCalculateHours = true;
			}
		} 

		return String.format(mFormat, minutes, seconds, milliSeconds);
	}
}
