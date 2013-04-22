package csc3202.Engine.Sound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;

import org.vamp_plugins.*;
import org.vamp_plugins.PluginLoader.LoadFailedException;

/**
 * Waveform analysis class
 * @author Sam Mitchell Finnigan
 */
public class Analyser {
	
	/** Exception thrown when analysis fails, for whatever reason */
	public class AnalyserException extends Exception {
	    private static final long serialVersionUID = -7896917649872471663L;

		public AnalyserException(String message) {
	        super(message);
	    }
	}
	
	private final List<String> want_plugins;
	private final Map<String, Plugin> loaded_plugins;

	
	private static final int sampleRate = 44100;	// For purposes of this class, this will always be 44.1KHz (WAV)
	
	private boolean initialised = false;
	
	/**
	 * Construct an Analyser object
	 */
	public Analyser(ArrayList<String> plugins_to_load) { 
		want_plugins = plugins_to_load;
		loaded_plugins = new HashMap<String, Plugin>();
	}
	
	
	
	public Map<String, Plugin> getLoaded_plugins() throws AnalyserException {
		if(initialised)
			return loaded_plugins;
		else
			throw new AnalyserException("Analyser not initialized!");
	}


	
	
	
	/**
	 * Load plugins
	 * @throws LoadFailedException
	 */
	public void init() throws LoadFailedException {
		
		PluginLoader loader = PluginLoader.getInstance();
		Plugin p = null;
		
		String[] plugins = loader.listPlugins();

		// Check for plugin existance
		if(plugins.length == 0) {
			System.err.println("jVamp plugins missing!");
			return;
		}
		
		// Iterate the installed plugins and load the ones we want
//		System.out.println("Installed plugins:");
		for(String s : plugins) {
//			System.out.print(s);
			
			for(String key : want_plugins) {
				if(s.equals(key)) {
					
					// Load plugin
					loaded_plugins.put(
							key, 
							loader.loadPlugin(key, sampleRate, PluginLoader.AdapterFlags.ADAPT_ALL)
						);
					
//					System.out.print("\t loaded!");
				}
			}
//			System.out.println();
		}
		
		// Iterate over and initialize loaded plugins
		if(loaded_plugins.size() > 0) {
			Iterator<String> it = loaded_plugins.keySet().iterator();
			
			while(it.hasNext()) {
				String key = it.next();
			    p = loaded_plugins.get(key);
			    
				System.out.println("Loaded VAMP plugin: " + key);
	
			    describePlugin(loader, p, key);
			    
			    boolean b = p.initialise(1, 1024, 1024);	// inputChannels, stepSize, blockSize
			    
//			    System.out.println("Plugin initialise returned " + b + "\n");
			    if (!b) {
			    	throw new RuntimeException("Plugin initialise failed");
			    }
			}
		}
		
		
		initialised = true;
	}
	
	
	
	/**
	 * Analyse an audio stream and return its features 
	 * 
	 * @param audioIn: Signed PCM 16-bit little-endian AudioInputStreams
	 * @return Map of timecodes to onset probabilites
	 * @throws IOException 
	 * @throws LineUnavailableException 
	 */
	public static Map<Integer, List<Feature>> analyseStream(Plugin p,
								AudioFormat format, AudioInputStream audioIn) 
						throws IOException, LineUnavailableException {
		
		if ( format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED )
			throw new IOException("analyseStream accepts only PCM_SIGNED format audio data!");
				
		int sampleSizeBytes = format.getSampleSizeInBits() / 8;
		int channels = format.getChannels();
		
		Map<Integer, List<Feature>> features = new TreeMap<Integer, List<Feature>>();
		Map<Integer, List<Feature>> temp = null;
		
		byte[] data = new byte[sampleSizeBytes * 4 * 1024];						// Sample size * sizeof float * 1024 samples
		float[][] buffer = new float[1][data.length / channels];
		
		int nBytesRead = 0;
		int block = 0;
		int frame = 0;
		while (nBytesRead != -1) {
			
			nBytesRead = audioIn.read(data, 0, data.length);					// Read data & fill buffer
			
			if (nBytesRead != -1) {												// Check we have not reached EOF
				
				int j=0;
				for(int i=0; i<nBytesRead; i+=sampleSizeBytes * channels) {		// Fill float buffer from byte buffer, averaging channels
					long acc=0;													// Long accumulator in case of overflow in the highest-order bit
					
					for(int k=0; k<channels; k++) {								// Convert each channel to int and add
						acc+= asInt(data[k+i], 
									data[k+i+1], 
									data[k+i+2], 
									data[k+i+3]);
					}
					
					buffer[0][j] = Float.intBitsToFloat((int) acc/channels);	// Average channels in a stereo (or surround) stream

					j++;
				}
				frame = block * 1024;
				
				// Analyse test data
				RealTime timestamp = RealTime.frame2RealTime(frame, sampleRate);
				
				temp = p.process(buffer, timestamp);
				features.putAll(temp);
				
//				printFeatures(temp);
				timestamp.dispose();
			}
			
			block++;
		}

		features.putAll(p.getRemainingFeatures());
		
	    p.dispose();
		
		return features;
	}
	
	
	/**
	 * TEST
	 */
	public Map<Integer, List<Feature>> test(Plugin p) {
		
		Map<Integer, List<Feature>> features = null;
		
	    // This is the bit where we read a block/frame. 1 channel 1024 samples
	    float[][] buffer = new float[1][1024];
	    for (int block = 0; block < 1024; ++block) {
	    	
	    	// READ: Fill with test data (read next 1024 bytes, as it were)
			for (int i = 0; i < 1024; ++i) {
			    buffer[0][i] = 0.0f;
			}
			if (block == 512) {
			    buffer[0][0] = 0.5f;
			    buffer[0][1] = -0.5f;
			}
			
			// Analyse test data
			RealTime timestamp = RealTime.frame2RealTime(block * 1024, sampleRate);
			features = p.process(buffer, timestamp);

			timestamp.dispose();

			printFeatures(features);
	    }

	    features = p.getRemainingFeatures();

	    p.dispose();
	    
	    return features;
	}
	
	
	
	/**
	 * Print post-analysis audio track features (debug)
	 * @param features
	 */
    public static void printFeatures(Map<Integer, List<Feature>> features) {
		
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
	
	
    
    /**
     * Write plugin capabilities to terminal
     * @param loader
     * @param p
     */
	public void describePlugin(PluginLoader loader, Plugin p, String key) {

	    System.out.println();
	    String[] cat = loader.getPluginCategory(key);
	    System.out.print("category: ");
	    for (int i = 0; i < cat.length; ++i) {
			System.out.print(cat[i]);
			if (i+1 < cat.length) 
				System.out.print(" > ");
	    }
	    System.out.println();
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

		System.out.println("Preferred block size: " + p.getPreferredBlockSize() );
	}
	
	
	
	/**
	 * Convert 4 bytes to a 32-bit int as described at 
	 * 		http://stackoverflow.com/questions/4513498
	 * Used in byte/float conversion
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return int
	 */
	public static final int asInt(byte a, byte b, byte c, byte d) {
		return (a & 0xFF) 
	            | ((b & 0xFF) << 8) 
	            | ((c & 0xFF) << 16) 
	            | ((d & 0xFF) << 24);
	}
	
	

	/** 
	 * Unit test - Analyse an MP3 provided as command-line parameter
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		ArrayList<String> want_plugins = new ArrayList<String>();
		
		// Comment & uncomment to test. Only first is used.
		want_plugins.add("qm-vamp-plugins:qm-barbeattracker");
//		want_plugins.add("qm-vamp-plugins:qm-onsetdetector");
//		want_plugins.add("vamp-example-plugins:amplitudefollower");
		
		
		if(args.length > 0) {
			
			String filename = args[0];
			
			Analyser a = new Analyser(want_plugins);
			MP3RawPCM mp3 = new MP3RawPCM(filename);
			long mp3_duration = new ThreadedMP3(filename).getDuration();
			
			a.init();
			
			Map<String, Plugin> loaded_plugins = a.getLoaded_plugins();
			
			Plugin p = loaded_plugins.get(want_plugins.get(0));
						
//			p.selectProgram("General purpose");
//			System.out.println("Selected Program: " +  p.getCurrentProgram());
			
//			p.setParameter("attack", 10);
//			p.setParameter("release", 60);
			
			// Time execution
			long startTime = System.nanoTime();
			Map<Integer, List<Feature>> features =
					Analyser.analyseStream(p, mp3.getDecodedFormat(), mp3.getAudioIn());

			printFeatures(features);
			
			long duration = System.nanoTime() - startTime;
			
			System.out.println("Analysed:");
			System.out.println(" " + features.values().size() + " features in " + (float)(duration / 1000000000.0f) + " seconds");
			System.out.println(" of an MP3 of length " + (int)(mp3_duration / 1000000) + " seconds");
						
		} else {
			System.err.println("No MP3 file to analyse specified on command line!");
		}
	}
}
