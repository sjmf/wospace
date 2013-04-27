package csc3202.Engine.Sound;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * A BeatFile is an in-memory data structure containing time-series info 
 *  analysed from an audio stream. Methods are provided for reading and
 *  writing the data out to / from file to cache results of a beat analysis.
 *  
 * @author Sam Mitchell Finnigan - a9134046
 */
public class BeatFile implements Serializable {
	/* Serializable version for cache files **/
	private static final long serialVersionUID = -1328802690000963978L;
	
	private static final String CACHE_DIRECTORY = "beatcache";
	private static final String FILE_EXTENSION = "wub";
	
	private String fileName = null;					// res/file.mp3
	private String pluginName = null;				// qm-barbeattracker
	private List<AudioEvent> events = null;
	
	// Artist and title as read from ID3 tag
	private String artist = null;
	private String title = null;
	private long duration = 0l;
	
	private float peak = 0;
	public float average = 0;
	
	/** Construct an empty BeatFile **/
	public BeatFile() {	}
	
	/** Construct the beats! */
	public BeatFile(String fileName, String pluginName, List<AudioEvent> events) {
		
		this.fileName = fileName;
		this.pluginName = pluginName;
		this.events = events;
	}
	
	public float getAvg() {
		return average;
	}
	
	public float calcAvg() {
		System.out.println("Calculating loudness...");
		for(AudioEvent e : events)
			average += e.value / events.size();
		return average;
	}

	public float getPeak() {
		return peak;
	}
	
	/** Calculate or return cached peak value */
	public float calcPeak() {
		System.out.println("Analysing peak");
		for(AudioEvent e : events)
			if(peak < e.value) peak = e.value;
		
		return peak;
	}

	/** Get the name of the file concatenated with the plugin string **/
	public String getPluginName() {
		return pluginName;
	}

	/** 
	 * Directly set the name of the BeatFile. 
	 * Things rely on this being correct, so use makeName()
	 */
	public void setName(String name) {
		this.pluginName = name;
	}

	public String getFilename() {
		return fileName;
	}

	public void setFilename(String filename) {
		this.fileName = filename;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	/** Get the event list from this BeatFile **/
	public List<AudioEvent> getEvents() {
		return events;
	}

	/** Set an event list **/
	public void setEvents(List<AudioEvent> events) {
		this.events = events;
	}

	/**
	 * Read in the cached BeatFile.
	 * Returns null and prints an error if read fails
	 * @param audioFile
	 * @param pluginString
	 * @return
	 */
	public static BeatFile read(String path) {
		
		ObjectInputStream reader = null;
		BeatFile bf = null;
		try {
			reader= new ObjectInputStream( 
					new BufferedInputStream( 
					new FileInputStream( path )));
			
			bf = (BeatFile) reader.readObject();
			reader.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("BeatFile could not be read due to old version. Updating!");
		} 
		
		return bf;
	}
	
	/** 
	 * Write out a BeatFile using object serialization 
	 */
	public static void write(BeatFile bf) {

		ObjectOutputStream output = null;
		File out = new File(bf.makePath());
		File cachedir = new File(CACHE_DIRECTORY);
		
		try {
			if(! cachedir.exists())		// Check directory exists
				cachedir.mkdirs();			// if not create it
			if(! out.exists())			// Check file exists
				out.createNewFile();		// if not create it
			
			output = new ObjectOutputStream(
					 new BufferedOutputStream(
					 new FileOutputStream( out )));
			
			output.writeObject(bf);			// Write value
			
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Get filename from mp3 and pluginString */
	public String makePath() {
 		// Trim file extension and path in a semi-portable fashion
		String partname = fileName.substring(fileName.lastIndexOf(File.separator)+1, fileName.length());
		return CACHE_DIRECTORY + "/" 
				+ partname.substring(0, partname.lastIndexOf('.'))
				+ "_" + pluginName 
				+ "." + FILE_EXTENSION;
	}
	
	/** Check for the existence of a beat-file */
	public boolean isCached() {
		return new File(makePath()).exists();
	}
	
	@Override
	public String toString()  {
		return pluginName + ": "+ events.size() +" events\n" + events;
	}
}
