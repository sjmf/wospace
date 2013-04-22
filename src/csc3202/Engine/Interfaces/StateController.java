package csc3202.Engine.Interfaces;

/**
 * Abstracted State control to an interface
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
public interface StateController {

	/**
	 * Wipe out all current loaded states and add the one passed in
	 * @param state
	 */
	public void changeState(GameState state);

	/**
	 * Put a new state on top (menu, for example) and pause the one underneath
	 * @param state
	 */
	public void pushState(GameState state);

	/**
	 * Remove the state on top
	 */
	public void popState();

	/**
	 * Init states in the stack 
	 * 	Run ONLY after GL initialisation
	 */
	public void initStates();
	
}