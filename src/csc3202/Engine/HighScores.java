
package csc3202.Engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * High scores file implementation
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
public class HighScores {

	/** Max number of scores to keep **/
	private static final int KEEP_SCORES = 8;
	
	/** Scores array- kept sorted in DESCENDING order **/
	private int[] scores;
	
	/** Array of names- positions should be kept in-line with scores array **/
	private String[] names;
	
	
	
	/**
	 * Create a new High Scores object
	 */
	public HighScores() {
		names = new String[KEEP_SCORES];
		scores = new int[KEEP_SCORES];
	}
	
	
	/**
	 * Check if a score is a new high score, and add it if it is
	 *
	 * @param score
	 * @return true or false
	 */
	public boolean isHighScore(int score) {
		
		if(score > scores[scores.length -1])
			return true;
		
		return false;
	}
	
	
	
	/**
	 * Stick the score in the array at the right place
	 * @param score
	 * @param name
	 */
	public void insertScoreSorted(int score, String name) {

		int temp_score = 0;				// Temporary variables
		String temp_name = "";
		
		for(int i=0; i<scores.length; i++) {
			
			if(scores[i] > score)
				continue;				// Skip- go to next entry
			
			// Almost a bubble sort:
			// 	move the rest of the elements down (forcing the lowest score off the end)
			// 	and insert elements at correct position
			
			temp_score = scores[i];
			temp_name = names[i];
			
			scores[i] = score;
			names[i] = name;
			
			score = temp_score;
			name = temp_name;
		}
	}
	
	
	/**
	 * Retrieve data
	 * @param i
	 * @return
	 */
	public String getName(int i) {
		return names[i];
	}
	
	
	public int getScore(int i) {
		return scores[i];
	}
	
	
	/**
	 * Read in a high scores file
	 */
	public void read() {
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(Globals.HIGH_SCORE_FILE));
			String line;
			String[] parts;
			int i=0;
			while ((line = br.readLine()) != null) {
								
				if(line.startsWith("#")) continue;	// Skip comments
				if(i > KEEP_SCORES)		 break;		// Don't bother reading more than 8 scores
				
				parts = line.split(":");
				
				names[i] = parts[0];
				scores[i] = Integer.parseInt(parts[1]);
				
				i++;
			}
			br.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			defaults();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			defaults();
		} catch (java.lang.NullPointerException e) {
			e.printStackTrace();
			defaults();
		}
	}
	
	
	
	/**
	 * Write high scores out to file (clobber original)
	 */
	public void write() {
		
		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(Globals.HIGH_SCORE_FILE));
			wr.write("# HIGH SCORES\n");
			
			int i=0;
			while(i < names.length) {
				wr.write(names[i] + ":" + scores[i] + "\n");
				i++;
			}
			
			wr.flush();
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Reset to defaults (will overwrite file)
	 */
	public void defaults() {
		
		names[0] = "ROB";	scores[0] = 12000;
		names[1] = "ANA";	scores[1] = 9000;
		names[2] = "DAN";	scores[2] = 8000;
		names[3] = "JOE";	scores[3] = 7500;
		names[4] = "TIM";	scores[4] = 5000;
		names[5] = "RIA";	scores[5] = 2000;
		names[6] = "LOU";	scores[6] = 1000;
		names[7] = "EDD";	scores[7] = 100;
	}
	
}
