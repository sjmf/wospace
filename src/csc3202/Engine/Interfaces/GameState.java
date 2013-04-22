package csc3202.Engine.Interfaces;

import csc3202.Engine.Engine;

/**
 * An interface describing a state of play; could be a menu or gameplay
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public interface GameState {
	
	/** Set up the game to its initial state. Returns itself for convenience **/
	GameState init(Engine engine);

	/** Update the Game State using the delta for interpolation **/
	int update(long delta);
	
	/** Render all objects inside the Game State **/
	int render();
	
	/** Handle keyboard input passed down from the Engine **/	
	void keyInput(int key);
	
	/** Handle mouse input passed down from the Engine **/
	void mouseInput(int x, int y, boolean leftDown, boolean rightDown);

	/** Toggle paused state */
	void pause();
	
	/** Always resume. Function is not a toggle **/
	void resume();
	
	/** Do any cleanup that needs doing to close gracefully **/
	void cleanup();
}
