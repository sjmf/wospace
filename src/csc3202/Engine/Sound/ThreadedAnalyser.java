package csc3202.Engine.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.vamp_plugins.PluginLoader.LoadFailedException;

/**
 * Get an analysis result by launching a thread. Parent class can check
 *  response availability as often as it likes.
 * @author a9134046
 */
public class ThreadedAnalyser {

	private ArrayList<String> want_plugins;
	private final AnalysisSource aSource;
	private final AnalysisHandler aHandle;
	private Thread runner;
	
	private Analyser analyser;
	
	/** 
	 * Because parameterized generics can't be infered by instanceof at 
	 * runtime, wrap the whole HashMap in an object
	 * @author a9134046
	 */
	private class AudioEventResponse {
	    private List<BeatFile> events;
	    
	    public AudioEventResponse(List<BeatFile> events) {
	    	this.events = events;
	    }

		public List<BeatFile> getEvents() {
			return events;
		}
	}
	
	/**
	 * Observer pattern event source for AudioAnalysis
	 * @author a9134046
	 */
	private class AnalysisSource extends Observable implements Runnable {
		
	    private final String filename;
	    private volatile AudioEventResponse events;
		private volatile Analyser analyser = null;
	    
	    public AnalysisSource(Analyser analyser, String filename) {
	    	this.filename = filename;
	    	this.analyser = analyser;
	    }
	    
		@Override
		public void run() {
			System.out.println("Audio Analysis thread started");
			
			setChanged();
			events = new AudioEventResponse(Analyser.run(analyser, filename));
			notifyObservers(events);
		}
		
		public synchronized void interrupt() {
			analyser.interrupt();
		}
	}
	
	/**
	 * Observer pattern handler for AudioAnalysis
	 * @author a9134046
	 */
	private class AnalysisHandler implements Observer {

		private AudioEventResponse resp = null;
		@Override
		public void update(Observable o, Object arg) {
			if (arg instanceof AudioEventResponse) {
				resp = ((AudioEventResponse) arg);
			}
		}
		
		public AudioEventResponse getResponse() {
			return resp;
		}
	}
	
	
	/**
	 * Construct a threaded analyser class
	 * @throws LoadFailedException 
	 */
	public ThreadedAnalyser(String filename) throws LoadFailedException {
		
		want_plugins = new ArrayList<String>();
		
		want_plugins.add(Analyser.BEAT_PLUGIN);					// Add plugins to test
		want_plugins.add(Analyser.AMP_PLUGIN);
		
		analyser = new Analyser(want_plugins);		
		aSource = new AnalysisSource(analyser, filename); 
		aHandle = new AnalysisHandler();   
		
		aSource.addObserver(aHandle);
		runner = null;
	}
	
	/** Start the event thread */
	public void start() {
		runner = new Thread(aSource);
        runner.start();
        runner.setPriority( Thread.NORM_PRIORITY + 2 ); 
	}
	
	/** Interrupt file analysis **/
	public void interrupt() {
		aSource.interrupt();
	}
	
	/* Provided for convenience to check if analysis is done */
	public boolean resultAvailable() {
		return (aHandle.getResponse() != null);
	}
	
	/* Retrieve the analysis result, or null if it is not yet available */
	public List<BeatFile> getResult() {
		return aHandle.getResponse().getEvents();
	}
	
	/* Unit test */
	public static void main(String[] args) {
		List<BeatFile> res;
		
		if(args.length > 0) {
			try {
				String filename = args[0];

				ThreadedAnalyser analyser = new ThreadedAnalyser(filename);

				analyser.start();
				
				// Go around and around the render loop until we have a result
				while(! analyser.resultAvailable()) {
					Thread.sleep(1000);	// Pretend we're processing something else
					System.out.println("....... Heartbeat from caller thread");
				}
				
				res = analyser.getResult();
				
				System.out.println(res.size() + " results received from thread");
				
				for(BeatFile bf : res)
					System.out.println(bf);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (LoadFailedException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("No MP3 file to analyse specified on command line!");
		}
	}
}
