package csc3202.Engine;

import java.util.List;

import csc3202.Engine.Sound.BeatFile;

/**
 * Handle score and lives tracking in one class
 * 
 * This class is also handily passed down into all game states, so provides
 * a nice interface to access specific data without a Global context. Use it
 * for modifyable data, and Global for final static data.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class GameData {

	/** Game score as calculated by shooting enemies **/
	private int score;
	
	/** Number of remaining lives **/
	private int lives;
	
	/** High score file **/
	private HighScores highscores;
	
	/** Whether the game is paused **/
	private boolean paused = false;
	
	/** Whether the game is won (which gameover state to display) **/
	private boolean win = false;
	
	/** The file to run music analysis on for runstate values **/
	private String mp3file = null;
	
	/** Music analysis result */
	private List<BeatFile> analysis;

	/**
	 * Constructor with no of starting lives
	 * @param lives
	 */
	public GameData(int lives) {
		this.lives = lives;
		score = 0;
		highscores = new HighScores();
		highscores.read();
	}
	
	public HighScores getHighScores() {
		return highscores;
	}
	
	public void addScore(int points) {
		score += points;
	}
	
	public void decrementLives() {
		lives--;
	}
	
	public void reset(int lives) {
		this.lives = lives; 
		this.score = 0;
		this.paused = false;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getLives() {
		return lives;
	}
	
	public void setLives(int lives) {
		this.lives = lives;
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause() {
		this.paused = ! paused;
	}

	public boolean isGameWon() {
		return win;
	}

	public void setGameWon(boolean win) {
		this.win = win;
	}

	public String getMp3File() {
		return mp3file;
	}

	public void setMp3File(String mp3filename) {
		this.mp3file = mp3filename;
	}
	
	public List<BeatFile> getAnalysis() {
		return analysis;
	}

	public void setAnalysis(List<BeatFile> result) {
		this.analysis = result;
	}
}
