/**
 * 
 */
package csc3202.Engine.Sound;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

/**
 * Load an MP3 and provide access to an AudioInputStream containing raw signed PCM data
 * 
 * @author Sam Mitchell Finnigan a9134046
 * @version 22nd Apr 2013
 */
public class MP3RawPCM {

	final String filename;
	final AudioInputStream in;
	final AudioInputStream audioIn;
	final AudioFormat baseFormat;
	final AudioFormat decodedFormat;
	
	
	/**
	 * Construct a RAW PCM MP3 decoder
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 */
	public MP3RawPCM(String filename) throws UnsupportedAudioFileException, IOException {
		
		this.filename = filename;
		
		this.in = AudioSystem.getAudioInputStream(new File(filename));
		
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

		this.audioIn = AudioSystem.getAudioInputStream(decodedFormat, in);
	}
	

	public AudioInputStream getAudioIn() {
		return audioIn;
	}


	public AudioFormat getDecodedFormat() {
		return decodedFormat;
	}


	public void testPlay() throws IOException, LineUnavailableException {
		
		// Play now.
		rawplay(decodedFormat, audioIn);
		in.close();
	}

	
	/**
	 * Play an AudioInputStream by reading bytes out to the Sound device
	 * @param targetFormat
	 * @param din
	 * @throws IOException
	 * @throws LineUnavailableException
	 */
	@SuppressWarnings("unused")
	private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
		
		byte[] data = new byte[1024];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);
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
			MP3RawPCM mp3 = new MP3RawPCM(filename);
			mp3.testPlay();
		} else {
			System.err.println("No MP3 file to play specified on command line!");
		}
	}
}
