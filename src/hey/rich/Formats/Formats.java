package hey.rich.Formats;

public class Formats {

	/*
	 * For all of the following formats follow the syntax: H: Hours, M: Minutes, S: Seconds,
	 * s: Milliseconds 
	 * 
	 * So that for a format such as MM:SS:ss This means that when the time
	 * displayed is: 4:08:15 that 5 minutes, 8 seconds, and 150 milliseconds
	 * have elapsed since the start button press.
	 */

	/** The default format for the timer: MM:SS:ss */
	public final static String DEFAULT_FORMAT = "%d:%02d:%02d";

	/**
	 * A more accurate format for the timer MM:SS:sss
	 * <p>
	 * Note this timer is not accurate by the millisecond, its accuracy extends
	 * to only ever 3 milliseconds or so. When using this format, make sure to
	 * change your timer so that it is responding every millisecond instead of
	 * every 10.
	 */
	public final static String ACCURATE_FORMAT = "%d:%02d:%03d";

	/**
	 * A format that only displays seconds and minutes. MM:SS
	 * <p>
	 * To decrease CPU usage, change your Timer so that we only update the time
	 * every 60000 milliseconds instead of every 2.
	 */
	public final static String MINUTE_SECOND_FORMAT = "%d:%02d";

	/**The natural progression from the default format, adds a display for hours.
	 * HH:MM:SS:ss*/
	public final static String EXTENDED_FORMAT = "%d:%02d:%02d:%02d";
}
