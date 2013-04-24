package csc3202;


import csc3202.Engine.Engine;
import csc3202.Engine.GameData;
import csc3202.Engine.Globals;
import csc3202.States.LoaderState;


/**
 * Main class
 * 
 * Perform any set-up required and start the game Engine, which
 *  will handle any further changes to the state of play
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
class Main {
		
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Engine engine = new Engine();		// Load data and set-up state
		
		GameData data = new GameData(Globals.LIVES);				// Initialise a GameData object to handle score and lives
		
		if(args.length > 0)
			data.setMp3File(args[0]);
		else
			System.err.println("No MP3 Specified. You're getting the silent treatment.");
		
		
		engine.pushState(new LoaderState(data));		// Push LoaderState to load states when resources are done loading
		
		
		try {
			engine.run();			// Run the game engine
		} catch(Exception e) {
			engine.stop();			// Clean up threads
			throw e;
		}
	}
}
