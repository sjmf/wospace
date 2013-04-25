
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
    		long time=0;
    		int index=0;
    		
    		// Buffer and value for moving average calculation
    		float[] avgbuf = new float[SAMPLES];
    		float movingavg = 0.0f;
    		
    		AudioEvent e = null;
    		try {
	    		List<AudioEvent> events = amplitude.getEvents();
	    		
	    		while(index < events.size() && running) {
	    			
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
	    			
	    			avgbuf[index%SAMPLES] = e.value;
	    			
	    			for(float a : avgbuf)		// Run moving average calculation
	    				movingavg += a;
	    			movingavg /= SAMPLES;
	    			
	    			Globals.game_speed = movingavg * MULTIPLIER;
	    			Globals.fire_speed = movingavg;
	    			
	    			// Sleep Time to next update (can skip if not running fast enough to keep up)
	    			time = System.currentTimeMillis();
	    			if(startRun + e.time > time) {
	    				Thread.sleep(e.time - amplitude.getEvents().get(index-1).time);
	    			}
	    			index++;
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
