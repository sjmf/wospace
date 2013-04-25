package csc3202.Engine.Sound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;

import org.vamp_plugins.*;
import org.vamp_plugins.PluginLoader.LoadFailedException;

/**
 * Waveform analysis class
 * 
 * @author Sam Mitchell Finnigan
 * @version Apr '13
 */
public class Analyser {
	
	public static final String BEAT_PLUGIN = "qm-vamp-plugins:qm-barbeattracker";
	public static final String AMP_PLUGIN =  "vamp-example-plugins:amplitudefollower";
	
	/** Exception thrown when analysis fails, for whatever reason */
	public class AnalyserException extends Exception {
	    private static final long serialVersionUID = -7896917649872471663L;

		public AnalyserException(String message) {
	        super(message);
	    }
	}
	
	private final List<String> want_plugins;
	private final Map<String, Plugin> loaded_plugins;

	/**
	 * Oh god this is horrible:
	 * I can't work out why the amplitude plugin returns a correct timestamp, 
	 * but the bar and beat tracker needs to be divided by 4. Set this to true
	 * to divide timestamps by the frame size.
	 */
	public static boolean framesize_hack = true;
	
	private static final int sampleRate = 44100;	// For purposes of this class, this will always be 44.1KHz (WAV)
	
	private boolean initialised = false;
	private volatile boolean interrupted = false;
	
	/**
	 * Construct an Analyser object
	 * Singleton :)
	 */
	protected Analyser(ArrayList<String> plugins_to_load) { 
		want_plugins = plugins_to_load;
		loaded_plugins = new HashMap<String, Plugin>();
	}
	
	
	
	public Map<String, Plugin> getLoadedPlugins() throws AnalyserException {
		if(initialised)
			return loaded_plugins;
		else
			throw new AnalyserException("Analyser not initialized!");
	}


	public List<String> getWantedPlugins() {
		return want_plugins;
	}
	
	
	/** Interrupt analysis **/
	public void interrupt() {
		interrupted = true;
	}
	
	
	public boolean isInterrupted() {
		return interrupted;
	}


	
	
	
	/**
	 * Load plugins
	 * @throws LoadFailedException
	 */
	public void init() throws LoadFailedException {
		
		PluginLoader loader = PluginLoader.getInstance();
		Plugin p = null;
		
		String[] paths = loader.getPluginPath();
		String[] plugins = loader.listPlugins();
		
		System.out.print("VAMP plugin path(s): ");
		for(String path : paths) 
			System.out.println(path);

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
	 * Analyse an audio stream and return a feature in a more accessible format
	 *  
	 * @param featureIndex  Feature Map index to return. We don't care about other features. Make damn sure to get it right, or you'll get nothing back.
	 * @param p             Plugin to use for analysis. Only two have been tested.
	 * @param format        AudioFormat containing data about the stream
	 * @param audioIn       Signed PCM 16-bit AudioInputStreams
	 * 
	 * @return Map of timecodes to onset probabilites
	 * @throws IOException 
	 * @throws LineUnavailableException 
	 */
	public ArrayList<AudioEvent> analyseStream(	int featureIndex, 
												Plugin p, 
												AudioFormat format, 
												AudioInputStream audioIn) 
								throws IOException, LineUnavailableException {
		
		if ( format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED )
			throw new IOException("analyseStream accepts only PCM_SIGNED format audio data!");
		
		int framesize = format.getFrameSize();
		int blocksize = p.getPreferredBlockSize();								// stepSize
		if(blocksize == 0) blocksize = 1024*format.getFrameSize();
		
		final int sampleBytes = format.getSampleSizeInBits() / 8;
		final int channels = format.getChannels();

		Map<Integer, List<Feature>> temp = null;
		ArrayList<AudioEvent> result = new ArrayList<AudioEvent>();
		
		byte[] data = new byte[blocksize];										// Read 1024 samples
		float[][] buffer = new float[1][data.length / channels];
		
		int bytesRead = 0;
		int block = 0;															// currentStep
		int frame = 0;
		while (bytesRead != -1 && ! interrupted) {
			
			bytesRead = audioIn.read(data, 0, data.length);						// Read data & fill buffer
			frame = block * blocksize;
			
			if (bytesRead != -1) {												// Check we have not reached EOF
				
				mkFloatBuffer(bytesRead, sampleBytes, channels, data, buffer);	// Move data around for VAMP which likes float arrays
				
				RealTime timestamp = RealTime.frame2RealTime(frame, (int) format.getSampleRate());
				
				temp = p.process(buffer, timestamp);							// Run actual analysis using VAMP plugin
				addToEventList(result, temp, featureIndex, framesize);			// and convert it to custom format for this program
				
				timestamp.dispose();
			}
			
			++block;
		}

		if(!interrupted) {
			temp = p.getRemainingFeatures();
			addToEventList(result, temp, featureIndex, framesize);
//			printFeatures(temp);
		    p.dispose();
		}
		
		
		return result;
	}



	/**
	 * Convert buffer of PCM-format bytes to VAMP-compatible normalised floats
	 * 
	 * @param nBytesRead
	 * @param sampleBytes
	 * @param channels
	 * @param data
	 * @param buffer
	 */
	private static void mkFloatBuffer(int nBytesRead, int sampleBytes,
										int channels, byte[] data, 
										float[][] buffer) {
		int j = 0;
		for (int i = 0; i < nBytesRead; i += sampleBytes * channels) {      	// Fill float buffer from byte buffer, averaging channels
			short acc = 0;                                                      // 16-bit accumulator (16-bit signed PCM)                      
			for (int k = 0; k < channels; k++)									// For each channel:                                           
				acc += toShort(data[i + k], data[i + k + 1]) / channels;        //    Convert two bytes to 16-bit short and average channels   

			buffer[0][j] = acc / 32768f;										// Convert short to -1 to 1 normalised float
			if( buffer[0][j] > 1 ) buffer[0][j] = 1;
			if( buffer[0][j] < -1 ) buffer[0][j] = -1;							// http://stackoverflow.com/questions/15087668
			j++;
		}
	}
	
	
	
	/**
	 * Read out a feature map from VAMP into a format 
	 *  containing only the data we specifically requested
	 *  
	 * @param result
	 * @param temp
	 * @param key
	 */
	private static void addToEventList( ArrayList<AudioEvent> result, 
										Map<Integer, List<Feature>> temp,
										int key, int framesize ) throws NumberFormatException {
		
		for (Map.Entry<Integer, List<Feature>> mi : temp.entrySet()) {
			
			if(mi.getKey() != key)	continue;
			
			for(Feature li : mi.getValue()) {
				RealTime t = li.timestamp;
				//TODO: Hack alert! Figure out why timestamps are a multiple of the frame-size
				long time = (((long)t.sec())*1000 + (long)t.msec());
				if(framesize_hack) time /= framesize;	

				if(li.values.length > 0) {										// Amplitude has values
					result.add(new AudioEvent(time,	li.values[0]));				
				} else {														// Beats/bars use labels, don't know why
					result.add(new AudioEvent(time,	Float.parseFloat(li.label)));
				}																// Can't think why any plugin would just want to return times?

//				System.out.println(time +" "+ ((li.values.length > 0) ? li.values[0] : li.label));
				t.dispose();
			}
		}
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

		System.out.println("Preferred block size: " + p.getPreferredBlockSize() + "\n");
	}
	
	
	
	/**
	 * Convert 2 bytes to a 16-bit short
	 * @param a
	 * @param b
	 * @return short
	 */
	public static final short toShort(byte a, byte b) {
		return (short) ((a & 0xFF) | ((b & 0xFF) << 8));
	}
	
	

	/** 
	 * Analyse the MP3 file at filename using the list of plugins provided 
	 * @throws Exception 
	 */
	public static List<BeatFile> run(Analyser a, String filename) {
			
		System.out.println("File:\n" + filename + "\n");
		
		List<BeatFile> beats = new ArrayList<BeatFile>();
		
		try {
			a.init();
			
			Map<String, Plugin> loaded_plugins = a.getLoadedPlugins();
			
			if(loaded_plugins.values().size() < a.getWantedPlugins().size())
				System.err.println("Missing plugins");
			
			MP3ToPCM mp3;
			long mp3_duration;
			
			int i=0;
			for(Plugin p : loaded_plugins.values()) {
				
				System.out.println("\nRunning plugin: " + p.getName());
				
				mp3 = new MP3ToPCM(filename);
				mp3_duration = mp3.getDuration();
				System.out.println(mp3.getDecodedFormat());
				p.setParameter("attack", 0.1f);
				p.setParameter("release", 0.5f);
				
				// Time execution
				long startTime = System.nanoTime();
				String id = p.getIdentifier();

				BeatFile bf = new BeatFile( filename, id, null );

				// Only call analyse stream if we didn't find a cached BeatFile
				if( bf.isCached() ) {
					bf.read();
					System.out.println("BeatCache Read from File");
				} 
				else {
					bf.setEvents( a.analyseStream(0, p, 
									mp3.getDecodedFormat(), 
									mp3.getAudioIn()));
					bf.write();
				}
				beats.add(bf);
				
				long duration = System.nanoTime() - startTime;
	
				System.out.println("Analysed using plugin: " + id );
				System.out.println("  " + beats.get(i++).getEvents().size() + " features in " + (float)(duration / 1000000000.0f) + " seconds");
				System.out.println("  of an MP3 of length " + (int)(mp3_duration / 1000000) + " seconds");
				
				// Toggle hack
				Analyser.framesize_hack = false;
				
				if(a.isInterrupted()) break;
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (AnalyserException e) {
			e.printStackTrace();
		} catch (LoadFailedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return beats;
	}
}
