package csc3202.Engine.Sound;

import java.util.List;
import java.util.Map;

import org.vamp_plugins.*;
import org.vamp_plugins.PluginLoader.LoadFailedException;

/**
 * Onset analysis test
 * @author sam
 *
 */
public class Analyser {
	
	private final String key = "qm-vamp-plugins:qm-onsetdetector";

	private final int sampleRate = 44100;
	
	public Analyser() { System.out.println(System.getProperty("os.arch")); }
	
	public void init() throws LoadFailedException {
		
		boolean foundPlugin = false;
		PluginLoader loader = PluginLoader.getInstance();
		
		String[] plugins = loader.listPlugins();

		// Check for plugin existance
		if(plugins.length == 0) {
			System.err.println("jVamp plugins missing!");
		}
		
		for(String s : plugins) {
//			System.out.println(s);
			if(s.equals(key))
				foundPlugin = true;
		}
		
		Plugin p = null;
		
		if(foundPlugin) {
		    p = loader.loadPlugin(key, sampleRate, PluginLoader.AdapterFlags.ADAPT_ALL);
		    
			System.out.println("Loaded VAMP plugin: " + key);
			System.out.println("Preferred block size: " + p.getPreferredBlockSize() );

		    describePlugin(loader, p);
		    
		    boolean b = p.initialise(1, 1024, 1024);
		    System.out.println("Plugin initialise returned " + b);
		    if (!b) {
		    	throw new RuntimeException("Plugin initialise failed");
		    }
		    
//		    float[][] buffers = new float[1][1024];
//		    for (int block = 0; block < 1024; ++block) {
//				for (int i = 0; i < 1024; ++i) {
//				    buffers[0][i] = 0.0f;
//				}
//				if (block == 512) {
//				    buffers[0][0] = 0.5f;
//				    buffers[0][1] = -0.5f;
//				}
//				RealTime timestamp = RealTime.frame2RealTime(block * 1024, 44100);
//				Map<Integer, List<Feature>>
//				    features = p.process(buffers, timestamp);
//
//				timestamp.dispose();
//
//				printFeatures(features);
//		    }
		}
	}
	
	
	public void describePlugin(PluginLoader loader, Plugin p) {

	    String[] cat = loader.getPluginCategory(key);
	    System.out.print("category: ");
	    for (int i = 0; i < cat.length; ++i) {
			System.out.print(cat[i]);
			if (i+1 < cat.length) 
				System.out.print(" > ");
	    }
	    System.out.println("");
	    System.out.println("identifier: " + p.getIdentifier());
	    System.out.println("name: " + p.getName());
	    System.out.println("description: " + p.getDescription());
	    System.out.println("version: " + p.getPluginVersion());

	    Plugin.InputDomain domain = p.getInputDomain();
	    if (domain == Plugin.InputDomain.TIME_DOMAIN) {
			System.out.println("This is a time-domain plugin");
	    } else {
			System.out.println("This is a frequency-domain plugin");
	    }

	    ParameterDescriptor[] params = p.getParameterDescriptors();
	    System.out.println("Plugin has " + params.length + " parameters(s)");
	    for (int i = 0; i < params.length; ++i) {
			System.out.println(i + ": " + params[i].identifier + " (" + params[i].name + ")");
	    }

	    String[] progs = p.getPrograms();
	    System.out.println("Plugin has " + progs.length + " program(s)");
	    for (int i = 0; i < progs.length; ++i) {
			System.out.println(i + ": " + progs[i]);
	    }

	    OutputDescriptor[] outputs = p.getOutputDescriptors();
	    System.out.println("Plugin has " + outputs.length + " output(s)");
	    for (int i = 0; i < outputs.length; ++i) {
			System.out.println(i + ": " + outputs[i].identifier + " (sample type: " + outputs[i].sampleType + ")");
	    }
	}
	
    private static void printFeatures(Map<Integer, List<Feature>> features) {
		for (Map.Entry<Integer, List<Feature>> mi : features.entrySet()) {
			System.out.print(mi.getKey() + ": ");
			for (Feature li : mi.getValue()) {
				System.out.print("[" + li.timestamp + "= ");
				for (float v : li.values) {
					System.out.print(v + " ");
				}
				System.out.print("] (\"");
				System.out.print(li.label);
				System.out.print("\") ");
			}
			System.out.println("");
		}
	}

	/** Unit test 
	 * @throws Exception **/
	public static void main(String[] args) throws Exception {
		new Analyser().init();
	}
}
