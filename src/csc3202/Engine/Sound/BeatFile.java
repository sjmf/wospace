package csc3202.Engine.Sound;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A BeatFile is an in-memory data structure containing time-series info 
 *  analysed from an audio stream. Methods are provided for reading and
 *  writing the data out to / from file to cache results of a beat analysis.
 *  
 * @author Sam Mitchell Finnigan - a9134046
 */
public class BeatFile {
	
	private static final String CACHE_DIRECTORY = "beatcache";
	private static final String FILE_EXTENSION = "wub";
	
	private String fileName = null;					// res/file.mp3
	private String pluginName = null;				// qm-barbeattracker
	private List<AudioEvent> events = null;
	
	/** Construct an empty BeatFile **/
	public BeatFile() {	}
	
	/** Construct the beats! */
	public BeatFile(String fileName, String pluginName, List<AudioEvent> events) {
		
		this.fileName = fileName;
		this.pluginName = pluginName;
		this.events = events;
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

	/** Get the event list from this BeatFile **/
	public List<AudioEvent> getEvents() {
		return events;
	}

	/** Set an event list **/
	public void setEvents(List<AudioEvent> events) {
		this.events = events;
	}

	/**
	 * Read in the cached BeatFile
	 * @param audioFile
	 * @param pluginString
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void read() {
		
		ObjectInputStream reader = null;
		
		try {
			reader= new ObjectInputStream( 
					new BufferedInputStream( 
					new FileInputStream( this.makePath() )));
			
			events = (List<AudioEvent>) reader.readObject();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Write out a BeatFile using object serialization 
	 */
	public void write() {

		ObjectOutputStream output = null;
		File out = new File(this.makePath());
		File cachedir = new File(CACHE_DIRECTORY);
		
		try {
			if(! cachedir.exists())		// Check directory exists
				cachedir.mkdirs();			// if not create it
			if(! out.exists())			// Check file exists
				out.createNewFile();		// if not create it
			
			output = new ObjectOutputStream(
					 new BufferedOutputStream(
					 new FileOutputStream( out )));
			
			output.writeObject(events);			// Write value
			
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Get filename from mp3 and pluginString */
	private String makePath() {
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
