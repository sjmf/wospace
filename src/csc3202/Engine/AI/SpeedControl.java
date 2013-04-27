
package csc3202.Engine.AI;

import java.util.List;

import csc3202.Engine.Globals;
import csc3202.Engine.Sound.AudioEvent;
import csc3202.Engine.Sound.BeatFile;

/**
 * Perform a moving average on amplitude data and set the global game
 * speed accordingly
 * 
 * @author Sam Mitchell Finnigan - a9134046
 */
public class SpeedControl {

	private static final int SAMPLES = 50;
	private static final int MULTIPLIER = 10;
    private final BeatFile amplitude;
    private SpeedThread s_thread = null;
    
    private class SpeedThread implements Runnable {
    	
    	private boolean running = true;
    	private boolean paused = false;
    	private boolean unpaused = false;
    	
    	@Override
        public void run() {
    		System.out.println(">> SpeedController thread started");
    		
    		// Loop variables
    		long startRun= System.currentTimeMillis();
    		long startPause=0;
    		int index=0;
    		
    		// Buffer and value for moving average calculation
    		float[] avgbuf = new float[SAMPLES];
    		float movingavg = 0.0f;
    		
    		// Game speed calculations
    		float avg = amplitude.getAvg();
    		float e_min = 0.4f * avg;		// - 60%
    		float e_max = 1.6f * avg;		// + 60%
    		
    		AudioEvent e = null;
    		try {
	    		List<AudioEvent> events = amplitude.getEvents();
	    		
	    		while(++index < events.size() && running) {
	    			
	    			if(paused) {				// Handle pause condition
	    				if(startPause == 0l) {
	    					startPause = System.currentTimeMillis();
	    				} else if(unpaused) {
	    					paused = unpaused = false;
	    					startRun += System.currentTimeMillis() - startPause;	// Maintain step with MP3 by incrementing start by time paused 
	    				}
	    				continue;
	    			}
	    			
	    			// Update game speed with moving average
	    			e = events.get(index);
	    			
	    			avgbuf[index%SAMPLES] = Globals.normalise(e.value, e_min, e_max);
	    			
	    			for(float a : avgbuf)		// Run moving average calculation
	    				movingavg += a;
	    			movingavg /= SAMPLES;
	    			
	    			if(movingavg > 0 && movingavg < e_max) {
	    				Globals.game_speed = 0.1f + movingavg * MULTIPLIER;
	    			} else if (movingavg < 0) {
	    				Globals.game_speed = 0;
	    			} else if(movingavg > e_max) {
	    				Globals.game_speed = 1 * MULTIPLIER;
	    			}
	    			
	    			System.out.println(Globals.game_speed);
	    			
	    			// Sleep Time to next update (can skip if not running fast enough to keep up)
	    			if(startRun + e.time > System.currentTimeMillis()) {
	    				Thread.sleep(e.time - amplitude.getEvents().get(index-1).time);
	    			}
	    		}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
    		
    		System.out.println("<< SpeedController thread exit");
        }
    	
    	public void stop() {
    		running = false;
    	}
    	
    	/** Pause */
    	public void pause() {
    		paused = true;
    	}
    	
    	/** Unpause */
    	public void unpause() {
    		unpaused = true;
    	}
    }
    

	/**
	 * Give the speed controller the BeatFile for Amplitude
	 */
	public SpeedControl(BeatFile amplitude) {
		this.amplitude = amplitude;
		this.s_thread = null;
	}
	
	public void start() {
		if(this.s_thread != null)
			this.s_thread.stop();	// Stop any existing threads
		
		this.s_thread = new SpeedThread();
		Thread thread = new Thread(s_thread);
		thread.start();
		
	}
	
	public void stop() {
		if(this.s_thread != null)
			this.s_thread.stop();	// Stop any existing threads
		this.s_thread = null;
	}
	
	/* Toggle pause in encapsulated thread */
	public void pause() {
		this.s_thread.pause();
	}
	
	/** Unpause */
	public void resume() {
		this.s_thread.unpause();
	}
}
