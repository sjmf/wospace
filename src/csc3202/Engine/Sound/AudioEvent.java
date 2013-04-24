package csc3202.Engine.Sound;

/**
 * A key-value pair representing an audio feature at a certain time.
 * 
 * ... because Java has no Pair class.
 * 
 * @author Sam Mitchell Finnigan
 * @version Apr 13
 */
public class AudioEvent {
	
	// 32-bit microsecond time will be fine, no such thing as a 138-year long song.
	public final long time;
	public final float value;

	public AudioEvent(long time, float value) {
		
		this.time = time;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return time + ": " + value;
	}
}
