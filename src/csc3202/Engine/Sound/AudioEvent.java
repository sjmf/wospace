package csc3202.Engine.Sound;

import java.io.Serializable;

/**
 * A key-value pair representing an audio feature at a certain time.
 * 
 * ... because Java has no Pair class.
 * 
 * @author Sam Mitchell Finnigan
 * @version Apr 13
 */
public final class AudioEvent implements Serializable {
	/** Generated Serial Version */
	private static final long serialVersionUID = 5695030829844777347L;
	
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
