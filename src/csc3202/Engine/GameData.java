package csc3202.Engine;

/**
 * Handle score and lives tracking in one class
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
}
