
package csc3202.States;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.vamp_plugins.PluginLoader.LoadFailedException;

import csc3202.Engine.Engine;
import csc3202.Engine.FontManager;
import csc3202.Engine.GameData;
import csc3202.Engine.Globals;
import csc3202.Engine.Interfaces.GameState;
import csc3202.Engine.OBJLoader.OBJManager;
import csc3202.Engine.OBJLoader.OBJModel;
import csc3202.Engine.Sound.ThreadedAnalyser;


/**
 * A state displayed while objects and textures are still loading
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Dec 2012
 */
public class LoaderState implements GameState {

	private static final String TEXT = "LOADING";
	private static final int LOADBAR_LEN = 10;
	private static final int LOADBAR_UPDATE_DELAY = 100;						// every 100ms increment loadbar
	private static final String LOADBAR_AUDIO_MSG = "Analysing beats";			// every 100ms increment loadbar
	
	private ThreadedAnalyser analyser;
	
	private String next_resource = "";
	
	/** Models to load */
	private static final String[] MODELS = {
		Globals.SHIP_MODEL_PATH,
		Globals.LASER_MODEL_PATH,
		Globals.ENEMY_MODEL_PATH,
		Globals.TRACKER_MODEL_PATH,
		Globals.BLASTER_MODEL_PATH,
		Globals.BOMB_MODEL_PATH,
		Globals.POWERUP_MODEL_PATH,
	};
	
	// Blending can disable colours - Keep this array the same length as MODELS
	private static final boolean[] blending_on = {
		false, true, true, true, false, false, false
	};
	
	/** No. of models models_loaded **/
	private int models_loaded = 0;
	private boolean analysis_finished = false;
	
	private boolean skip_first = true;	// Skip first update in order to render a frame first (updates happen prior to rendering in the engine) 
	
	private StringBuilder loadbar = new StringBuilder("-");
	private long lastLoaderIncrement = 0;
	
	private GameData data;
	
	private Engine engine;
	
	/**
	 * Construct
	 */
	public LoaderState(GameData data) {
		this.data = data;
	}

	

	@Override
	public GameState init(Engine engine) {
		
		this.engine = engine;
		lastLoaderIncrement = System.currentTimeMillis();
		
		try {
			analyser = new ThreadedAnalyser(data.getMp3File());
			analyser.start();
		} catch (LoadFailedException e) {
			e.printStackTrace();
		}
		
		return this;
	}



	@Override
	public int update(long delta) {
		
		preload(engine);					// Start loadin' models & textures
		
		// Display "loading" while pre-loading models
		if(OBJManager.getManager().isLoaded() && analysis_finished) {
			OBJManager.getManager().useDisplayLists();

			data.setAnalysis(analyser.getResult());
			
			// Push initial game states onto the stack maintained by Engine
			engine.changeState(new RunState(data).init(engine));
			engine.pushState(new OverlayState(data).init(engine));		// and Overlay states
			
			glColor3f(1f,1f,1f);			// Workaround for everything becoming last set colour after load
			
			return SUCCESS;
		}
		
		return SUCCESS;
	}
	
	
	/**
	 * Pre-load models (i.e. on program launch).
	 * Normal behaviour is lazy-load. Use this to pre-load
	 * all models into the program.
	 * 
	 * Models which are pre-models_loaded currently need to be hard-coded,
	 *  may want to pass a list instead...
	 * 
	 * Other threads should check .isLoaded() for status
	 */
	private void preload(Engine engine) {
		
		if(skip_first) {

			skip_first = false;
			next_resource =
					MODELS[models_loaded].substring(
						MODELS[models_loaded].lastIndexOf("/") +1, 
						MODELS[models_loaded].length() );
			return;
		}
		
		// Load models
		if(models_loaded < MODELS.length) {
			OBJModel model = OBJManager.getManager().getModel(MODELS[models_loaded++]);
			
			if(blending_on[models_loaded -1])
				model.alphaBlend(true);
			
			model.useDisplayList();
			
			if(models_loaded < MODELS.length) {
				next_resource =	MODELS[models_loaded].substring(
						MODELS[models_loaded].lastIndexOf("/") +1, 
						MODELS[models_loaded].length() 
					);
			} else {
				next_resource = LOADBAR_AUDIO_MSG;		// Set to "analysing" as that's usually the longest thing
			}
		}
		
		// Analyse Music
		analysis_finished = analyser.resultAvailable();
		
		// Reduce load from OpenGL thread so that the analyser gets more CPU time
		// This actually works!
		try {
			Thread.sleep(200);				// 5fps max
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		// Do this only when everything is loaded
		if(models_loaded >= MODELS.length && analysis_finished) {
			OBJManager.getManager().setLoaded();
		} else {
			incrementLoadBar();
		}
	}


	/**
	 * Render the loader
	 */
	@SuppressWarnings("deprecation")
	@Override
	public int render() {
		
		glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_ENABLE_BIT | GL_TRANSFORM_BIT);
		
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
			engine.set2D();
			
			glMatrixMode(GL_MODELVIEW);
			
			glDisable(GL_DEPTH_TEST);
		    glEnable(GL_TEXTURE_2D);
		    glEnable(GL_BLEND);
		    glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		    
			glPushMatrix();
				glScalef(1.0f, -1.0f, 1.0f);
				glTranslatef(0.0f, -Globals.window_height, 1.0f);
				
				FontManager.getManager().getFont(48f)
					.drawString(Globals.window_width/2 - (TEXT.length() * Globals.FONT_48PT / 2), 
								(Globals.window_height / 2) - 65f, TEXT, Color.orange);
				
				FontManager.getManager().getFont(24f)
					.drawString(Globals.window_width/2 - (loadbar.toString().length() * Globals.FONT_24PT / 2), 
								(Globals.window_height / 2), loadbar.toString(), Color.red);
				
				FontManager.getManager().getFont(18f)
				.drawString(Globals.window_width/2 - (next_resource.toString().length() * Globals.FONT_18PT / 2), 
							(Globals.window_height / 2) + 50f, next_resource.toString(), Color.white);
				
			glPopMatrix();

			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		
		glPopAttrib();
		
		return SUCCESS;
	}

	
	private void incrementLoadBar() {
		
		if(System.currentTimeMillis() > lastLoaderIncrement + LOADBAR_UPDATE_DELAY) {
			
			lastLoaderIncrement = System.currentTimeMillis();
			
			if(loadbar.length() < LOADBAR_LEN)
				loadbar.append("-");
			else
				loadbar.setLength(0);		// empty
		}
	}


	
	@Override
	public void keyInput(int key) {
		if (Keyboard.getEventKeyState()) {
			switch (key) {
				case Keyboard.KEY_ESCAPE:				// Return to menu state
					engine.stop();
				default:
					break;
			}
		}
	}
	
	// Unused
	@Override
	public void mouseInput(int x, int y, boolean leftDown, boolean rightDown) {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void cleanup() {
		analyser.interrupt();
	}
}
