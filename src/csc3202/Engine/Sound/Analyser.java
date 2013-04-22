package csc3202.Engine.Sound;

import org.vamp_plugins.*;
import org.vamp_plugins.PluginLoader.LoadFailedException;

/**
 * Onset analysis test
 * @author sam
 *
 */
public class Analyser {
	
	private final String onsetPluginString = "qm-vamp-plugins:qm-onsetdetector";

	public Analyser() throws LoadFailedException {
		
		boolean foundPlugin = false;
		PluginLoader loader = PluginLoader.getInstance();
		String[] plugins = loader.listPlugins();

		// Check for plugin existance
		if(plugins.length == 0) {
			System.err.println("jVamp plugins missing!");
		}
		
		for(String s : plugins) {
//			System.out.println(s);
			if(s.equals(onsetPluginString))
				foundPlugin = true;
		}
		
		Plugin onsetDetector = null;
		
		if(foundPlugin) {
			onsetDetector = loader.loadPlugin(onsetPluginString, 44100, 0);

			System.out.println("Loaded VAMP plugin: " + onsetPluginString);
			
			System.out.println("Preferred block size: " + onsetDetector.getPreferredBlockSize() );
						
//			onsetDetector.initialise(inputChannels, stepSize, blockSize)
//			onsetDetector.getRemainingFeatures();
		}
	}

	/** Unit test **/
	public static void main(String[] args) {
		try {
			new Analyser();
		} catch (LoadFailedException e) {
			e.printStackTrace();
		}
	}
}
