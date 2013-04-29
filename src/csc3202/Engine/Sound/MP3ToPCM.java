/**
 * 
 */
package csc3202.Engine.Sound;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.*;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

/**
 * Load an MP3 and provide access to an AudioInputStream containing raw signed PCM data
 * 
 * @author Sam Mitchell Finnigan a9134046
 * @version 22nd Apr 2013
 */
public class MP3ToPCM {

	private AudioInputStream in;
	private AudioInputStream audioIn;
	private AudioFormat baseFormat;
	private AudioFormat decodedFormat;
	
	private Map<String, Object> metadata;
	
	private volatile boolean paused = false;
	private volatile boolean running = true;
	
	/**
	 * Construct a RAW PCM MP3 decoder
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 */
	public MP3ToPCM(String filename) throws UnsupportedAudioFileException, IOException {
		
		MpegAudioFileReader reader = new MpegAudioFileReader();
	
		this.in = reader.getAudioInputStream(new File(filename));
		
		this.baseFormat    = in.getFormat();
		this.decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,     // encoding            the audio encoding technique
				baseFormat.getSampleRate(),          // sampleRate          the number of samples per second
				16,                                  // sampleSizeInBits    the number of bits in each sample
				baseFormat.getChannels(),            // channels            the number of channels (1 for mono, 2 for stereo, and so on)
				baseFormat.getChannels() * 2,        // frameSize           the number of bytes in each frame
				baseFormat.getSampleRate(),          // frameRate           the number of frames per second
				false                                // bigEndian           true= big-endian byte order (false means little-endian)
			);
		
		this.metadata = reader.getAudioFileFormat(new File(filename)).properties();

		this.audioIn = AudioSystem.getAudioInputStream(decodedFormat, in);
	}
	
	
	public Map<String, Object> getMp3_properties() {
		return metadata;
	}


	/** Get the duration of the MP3 (for timers etc) */
	public long getDuration() {
		return ((Long) metadata.get("duration")).longValue();
	}
	
	
	/** Get the title string from the MP3 ID3 tag */
	public String getTitle() {
		return (String) metadata.get("title");
	}
	
	
	/** Get the artist string from the MP3 ID3 tag */
	public String getArtist() {
		return (String) metadata.get("author");
	}
	

	public AudioInputStream getAudioIn() {
		return audioIn;
	}


	public AudioFormat getDecodedFormat() {
		return decodedFormat;
	}


	public void play() {
		// Play now.
		running = true;
		
		/** run in new thread to play in background */
		new Thread() {
			public void run() {
				System.out.println(">> MP3 thread started");
				this.setPriority(NORM_PRIORITY + 2);	// Elevated priority
				try {
					rawplay(decodedFormat, audioIn);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
				System.out.println("<< MP3 thread exit");
			}
		}.start();
	}

	
	public void stop() {
		running = false;
	}
	
	public void pause() {
		paused = true;
	}
	
	public void resume() {
		paused = false;
	}

	
	/**
	 * Play an AudioInputStream by reading bytes out to the Sound device
	 * @param targetFormat
	 * @param din
	 * @throws IOException
	 * @throws LineUnavailableException
	 */
	private synchronized void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
		
		byte[] data = new byte[1024];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0;
			while (nBytesRead != -1 && running) {
				
				if(! paused) {	// Handle paused state
					// Read from stream and write to audio device
					nBytesRead = din.read(data, 0, data.length);
					if (nBytesRead != -1)
						line.write(data, 0, nBytesRead);
				} else {
					try {
						Thread.sleep(50);	// Resume in 50th of ms 
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}
	
	/**
	 * Get output stream
	 * @param audioFormat
	 * @return
	 * @throws LineUnavailableException
	 */
	protected static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

	
	/** 
	 * Test client 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		if(args.length > 0) {
			String filename = args[0];
			MP3ToPCM mp3 = new MP3ToPCM(filename);
			mp3.play();
		} else {
			System.err.println("No MP3 file to play specified on command line!");
		}
	}
}
