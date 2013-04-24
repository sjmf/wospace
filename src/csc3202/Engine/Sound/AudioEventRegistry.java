/**
 * 
 */
package csc3202.Engine.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple Event Registry 
 * @author a9134046
 *
 */
public class AudioEventRegistry {

	// Singleton with Eager initialization
	private static final AudioEventRegistry instance = new AudioEventRegistry();
	
	private HashMap<String, ArrayList<AudioEvent>> events;
	ArrayList<String> want_plugins;
	
	/**
	 * 
	 */
	private AudioEventRegistry() {
		events = new HashMap<String, ArrayList<AudioEvent>>();
		want_plugins = new ArrayList<String>();
		
		want_plugins.add(Analyser.beatPlugin);					// Add plugins to test
		want_plugins.add(Analyser.ampPlugin);
	}
	
	
	public HashMap<String, ArrayList<AudioEvent>> getAnalysis(String filename) {
		
		if(! events.keySet().contains(filename +"/"+ Analyser.beatPlugin)) {
			Map<String, ArrayList<AudioEvent>> features = Analyser.run(filename, want_plugins);
			
			Iterator<String> it = features.keySet().iterator();
			while(it.hasNext()) {
				String name = it.next();
				events.put(filename +":"+ name , features.get(name));
			}
		}
		
		return events;
	}

	
	public static synchronized AudioEventRegistry getInstance() {
		
		return instance;
	}
	
	
	/**
	 * Unit test! Hurrah!
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length > 0) {
			String filename = args[0];											// Read MP3 file from command line

			HashMap<String, ArrayList<AudioEvent>> events = AudioEventRegistry.getInstance().getAnalysis(filename);

			System.out.println(events.keySet());
//			System.out.println(events.get(filename + ":" + Analyser.beatPlugin.split(":")[1]));
//			System.out.println(events.get(filename + ":" + Analyser.ampPlugin.split(":")[1]));
		}
		else {
			System.err.println("No MP3 file to analyse specified on command line!");
		}
	}
}
