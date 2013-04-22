/**
 * 
 */
package csc3202.States;

import static csc3202.Engine.Globals.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.TextureImpl;

import csc3202.Engine.*;
import csc3202.Engine.Interfaces.GameState;

/**
 * Game Over message and High-Score recording
 * Abstracted into its own state, Nov '12
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
@SuppressWarnings("deprecation")
public class GameOverState implements GameState {

	private static final int NAME_LETTERS = 3;
	
	private static final String SCORE = "Score: ";
	private static final String HIGH_SCORE = "New High Score: ";
	private static final String GAME_OVER = "GAME OVER";
	private static final String ENTER_NAME = "Enter your Name:";
	private static final String RESTART_MSG = "Press R or Fire to restart";
	
	private enum HighScoreStates {
		NO_HIGH_SCORE,
		HS_NAME_ENTRY,
		HS_WAITING
	};
	
	private HighScoreStates state;
	private String death_message;
	
	private char[] name_chars = new char[3];
	private String name = "";
	private int chars_entered = 0;
	
	private Engine engine;
	
	private GameData data;
	
	/**
	 * Construct a GameOverState
	 */
	public GameOverState(GameData data) {
		this.data = data;
		state = HighScoreStates.NO_HIGH_SCORE;
	}

	
	
	@Override
	public GameState init(Engine engine) {
		
		this.engine = engine;
		
    	int i = Engine.rand.nextInt(death_msgs.length);
    	death_message = death_msgs[i];
    	
    	// High score checking and addition
    	if(data.getHighScores().isHighScore(data.getScore())) {
    		state = HighScoreStates.HS_NAME_ENTRY;
    	}
    	
		return this;
	}
	
	

	@Override
	public int update(long delta) {
		
    	StringBuilder str = new StringBuilder();
    	for(char c : name_chars) {
    		if (c == '\u0000') {
    			if((System.currentTimeMillis() / 500) %2 == 0)
    				str.append('_');
    			else
    				str.append(' ');
    		} else {
    			str.append(c);
    		}
    	}
    	
    	this.name = str.toString();
    	
		
		if(chars_entered >= NAME_LETTERS
				&& state != HighScoreStates.HS_WAITING) {
			
			state = HighScoreStates.HS_WAITING;
			data.getHighScores().insertScoreSorted(data.getScore(), name);
			data.getHighScores().write();
		}
		
		return SUCCESS;
	}

	
	
	@Override
	public int render() 
	{
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
			engine.set2D();
					
			glMatrixMode(GL_MODELVIEW);
			String score = Integer.toString(data.getScore());
			
			TextureImpl.bindNone();								// Release bindings for other textures (i.e. models)
			glEnable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			
		    glPushMatrix();
			    glScalef(1.0f, -1.0f, 1.0f);
				glTranslatef(0.0f, -Globals.window_height, 1.0f);
				
		    	FontManager.getManager().getFont(48f)					// DRAW TITLE "GAME OVER" TEXT
		    		.drawString(
		    				Globals.window_width/2 - (GAME_OVER.length() * Globals.FONT_48PT / 2), 
		    				Globals.window_height/2 - 80, 
		    				GAME_OVER, Color.red);
		    	
		    	
		    	if(state == HighScoreStates.NO_HIGH_SCORE) {			// DRAW STATE WHEN NO NEW HS
		    		
			    	FontManager.getManager().getFont(24f)
			    		.drawString(
			    				Globals.window_width/2 - ((SCORE.length() + score.length()) * Globals.FONT_24PT / 2), 
			    				Globals.window_height/2, 
			    				SCORE + data.getScore()
			    			);
			    	
			    	FontManager.getManager().getFont(24f)				// DRAW DEATH MESSAGE
			    		.drawString(
			    				Globals.window_width/2 - (death_message.length() * FONT_24PT) /2, 
			    				Globals.window_height/2 + 65, 
			    				death_message, 
			    				Color.green
			    			);
			    	
		    	} else {	// DRAW HIGHSCORE NAME ENTRY STATE
		    		
		    		FontManager.getManager().getFont(24f)
		    			.drawString(
			    				Globals.window_width/2 - ((HIGH_SCORE.length() + score.length()) * Globals.FONT_24PT / 2),
		    					Globals.window_height/2, 
		    					HIGH_SCORE + data.getScore()
		    				);
		    		
			    	FontManager.getManager().getFont(24f)				// DRAW NAME ENTRY MESSAGE
			    		.drawString(
			    				Globals.window_width/2 - (ENTER_NAME.length() * FONT_24PT) /2, 
			    				Globals.window_height/2 +40, 
			    				ENTER_NAME, 
			    				Color.green
			    			);
			    	
			    	FontManager.getManager().getFont(36f)				// DRAW NAME
			    		.drawString(
			    				Globals.window_width/2 - (name.length() * FONT_36PT) /2, 
			    				Globals.window_height/2 + 80, 
			    				name
			    			);
			    	
		    	}
		    	
		    	if( state == HighScoreStates.NO_HIGH_SCORE 
		    			|| state == HighScoreStates.HS_WAITING ) {
	
			    	FontManager.getManager().getFont(18f)				// DRAW RESTART MESSAGE
			    		.drawString(
			    				Globals.window_width/2 - (RESTART_MSG.length() * FONT_18PT) /2, 
			    				Globals.window_height/2 + 140, 
			    				RESTART_MSG
			    			);
		    	}
		    	
		    glPopMatrix();
		    glDisable(GL_TEXTURE_2D);

			
			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		
		return SUCCESS;
	}
	
	

	@Override
	public void keyInput(int key) {
		
		if(state == HighScoreStates.HS_NAME_ENTRY) {
			if (Keyboard.getEventKeyState()) {
				
				String keyName = Keyboard.getKeyName(key);
				int index = Keyboard.getKeyIndex(keyName);
	
//				System.out.println(index + "\t" + keyName + "\t" + isKeyLetter(index));
				
				if(isKeyLetter(index)) {						// Add to name array
					
					name_chars[chars_entered] = keyName.charAt(0);
					if(chars_entered < NAME_LETTERS)
						chars_entered++;
					
				} else if( index == 14 || index == 211 ) {		// Backspace or DEL

					if(chars_entered > 0)
						chars_entered--;
					
					name_chars[chars_entered] = '\u0000';
				}
			}
		} else {
			if(Keyboard.getEventKeyState()) {
				switch(key) {
					case Keyboard.KEY_RETURN: 	// Fall through to restart
					case Keyboard.KEY_SPACE:
					case Keyboard.KEY_R:		// Restart
						data.reset(Globals.LIVES);
						engine.changeState(new RunState(data).init(engine));		// Push new states
						engine.pushState(new OverlayState(data).init(engine));
						break;
					case Keyboard.KEY_ESCAPE:				// Return to menu state
						data.reset(Globals.LIVES);
						engine.changeState(new MenuState(data).init(engine));
						engine.pushState(new OverlayState(data).init(engine));
						break;
					default:
						break;
				}
			}
		}
	}
	
	
	
	/**
	 * Used by keyInput to check if a pressed key is a qwerty keyboard character
	 * @param id - the key ID
	 * @return true or false
	 */
	private boolean isKeyLetter(int id) {
		
		return ( (id >= 16 && id <= 25)
				|| (id >= 30 && id <= 38)
				|| (id >= 44 && id <=50) );
	}
	
	

	@Override
	public void mouseInput(int x, int y, boolean leftDown, boolean rightDown) { }
	
	@Override
	public void pause() { }
	
	@Override
	public void resume() { }



	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
