package csc3202.Engine.Sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.player.Player;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

/**
 * Play an MP3 and provide access to relevant metadata.
 * 
 * @author Sam Mitchell Finnigan
 * 
 * Based on http://stackoverflow.com/questions/3125934
 * 
 */
public class ThreadedMP3 {

	private String filename;
	
	private volatile Player player;
	
	private final Map<String, Object> mp3_properties;
	
	/** constructor that takes the name of an MP3 file */
	public ThreadedMP3(String filename) {
		
		this.filename = filename;

		// Read MP3 metadata from ID3 tag
		AudioFileFormat baseFormat = null;
		try {
			baseFormat = new MpegAudioFileReader().getAudioFileFormat(new File(filename));
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mp3_properties = baseFormat.properties();
	}
	
	
	public Map<String, Object> getMp3_properties() {
		return mp3_properties;
	}


	/** Get the duration of the MP3 (for timers etc) */
	public long getDuration() {
		return ((Long) mp3_properties.get("duration")).longValue();
	}
	
	
	/** Get the title string from the MP3 ID3 tag */
	public String getTitle() {
		return (String) mp3_properties.get("title");
	}
	
	
	/** Get the artist string from the MP3 ID3 tag */
	public String getArtist() {
		return (String) mp3_properties.get("author");
	}
	
	
	/** Stop the MP3 playing */
	public void stop() {
		if (player != null)
			player.close();
	}
	
	
	/** Return whether the track has finished playing **/
	public boolean finished() {
		return player.isComplete();
	}
	

	/** play the MP3 file to the sound card */
	public void play() {
		try {
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			player = new Player(bis);
		} catch (Exception e) {
			System.err.println("Problem playing file " + filename);
			System.err.println(e);
		}

		/** run in new thread to play in background */
		new Thread() {
			public void run() {
				System.out.println("MP3 thread started");
				try {
					player.play();
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		}.start();
	}

	
	/** test client */
	public static void main(String[] args) {
		if(args.length > 0) {
			String filename = args[0];
			ThreadedMP3 mp3 = new ThreadedMP3(filename);
			mp3.play();
		} else {
			System.err.println("No MP3 file to play specified on command line!");
		}
	}

}