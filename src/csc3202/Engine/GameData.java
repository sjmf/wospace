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
	private int score = 0;
	
	/** Number of remaining lives **/
	private int lives;
	
	/** Score multiplier **/
	private int multiplier = 1;
	
	/** Bomb powerup count **/
	private int bombs = 0;
	
	/** High score file **/
	private HighScores highscores;
	
	/** Whether the game is won (which gameover state to display) **/
	private boolean win = false;
	
	/** The file to run music analysis on **/
	private String mp3file = null;
	
	/** Music analysis result */
	private List<BeatFile> analysis;
	
	/** Timestamp of when the game was started **/
	private long startTime = 0;
	
	/** Whether the game is paused **/
	private boolean paused = false;

	/**
	 * Constructor with no of starting lives
	 * @param lives
	 */
	public GameData() {
		this.lives = Globals.LIVES;
		this.bombs = Globals.BOMBS_INITIAL_SUPPLY;
		this.multiplier = 1;
		score = 0;
		highscores = new HighScores();
		highscores.read();
	}
	
	public HighScores getHighScores() {
		return highscores;
	}
	
	public void addScore(int points) {
		score += points * multiplier;
	}
	
	public void decrementLives() {
		lives--;
	}
	
	public void reset() {
		this.lives = Globals.LIVES;
		this.bombs = Globals.BOMBS_INITIAL_SUPPLY;
		this.score = 0;
		this.multiplier = 1;
		this.paused = false;
		this.win = false;
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

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause() {
		this.paused = true;
	}
	
	public void resume() {
		this.paused = false;
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

	public int getBombs() {
		return bombs;
	}

	public void incBombs() {
		if(bombs < Globals.MAX_BOMBS)
			this.bombs++;
	}

	public void decBombs() {
		if(bombs > 0)
			this.bombs--;
	}

	public int getMultiplier() {
		return multiplier;
	}

	public void incMultiplier() {
		this.multiplier++;
	}
	
	public void resetMultiplier() {
		this.multiplier = 1;
	}
}
