/**
 * 
 */
package csc3202.Engine.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * @author a9134046
 *
 */
public class AnalyserThread {

	
	private class AnalysisRunner extends Observable implements Runnable {
		
	    private volatile boolean running = true;

		@Override
		public void run() {
			System.out.println("Analysis thread started");
			
			while (running) {
				try {
					// Tell observers that there are changes
					setChanged();
//					notifyObservers(makeEnemies());		// Don't mind me, just making enemies...
					
					Thread.sleep(1000);					// In a music-based implementation, replace this with onset detection?
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}
		}

		/**
		 * Stop spawning enemies
		 */
		public void stop() {
			running = false;
		}
		
		private void doAnalysis() {
			
//			HashMap<String, ArrayList<AudioEvent>> events = AudioEventRegistry.getInstance().getAnalysis(filename);

//			System.out.println(events.keySet());
//			System.out.println(events.get(filename + ":" + Analyser.beatPlugin.split(":")[1]));
//			System.out.println(events.get(filename + ":" + Analyser.ampPlugin.split(":")[1]));
		}
	}
	
	private class AnalysisListener implements Observer {

		@Override
		public void update(Observable o, Object arg) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	/**
	 * 
	 */
	public AnalyserThread() {
		// TODO Auto-generated constructor stub
	}
}
