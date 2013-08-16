package hey.rich.Formats;

public class TimerFormat {

	private String mFormat;
	private boolean useHours;
	
	public TimerFormat(){
		mFormat = Formats.DEFAULT_FORMAT;
		useHours = false;
	}
	
	public String timeToString(long time){
		int milliSeconds = (int) (time % 1000);
		// Only want every 10 milliseconds - we're not fast enough for better
		milliSeconds /= 10;
		int seconds = (int) (time / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		
		return String.format(mFormat, minutes, seconds, milliSeconds);
	}
}
