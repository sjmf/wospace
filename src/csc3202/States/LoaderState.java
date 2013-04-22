
package csc3202.States;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import org.newdawn.slick.Color;

import csc3202.Engine.Engine;
import csc3202.Engine.FontManager;
import csc3202.Engine.GameData;
import csc3202.Engine.Globals;
import csc3202.Engine.Interfaces.GameState;
import csc3202.Engine.OBJLoader.OBJManager;
import csc3202.Engine.OBJLoader.OBJModel;


/**
 * A state displayed while objects and textures are still loading
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Dec 2012
 */
public class LoaderState implements GameState {

	private static final String TEXT = "LOADING";
	private String next_resource = "";
	
	/** Models to load */
	private static final String[] MODELS = {
		Globals.SHIP_MODEL_PATH,
		Globals.LASER_MODEL_PATH,
		Globals.ENEMY_MODEL_PATH
	};
	
	// Blending can disable colours
	private static final boolean[] blending_on = {
		false, true, true
	};
	
	/** No. of models models_loaded **/
	private int models_loaded = 0;
	private boolean skip_first = true;	// Skip first update in order to render a frame first (updates happen prior to rendering in the engine) 
	
	private StringBuffer loadbar = new StringBuffer("-");
	
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
		
		return this;
	}



	@Override
	public int update(long delta) {
		
		preload(engine);					// Start loadin' models & textures
		
		// Display "loading" while pre-loading models
		if(OBJManager.getManager().isLoaded()) {

			OBJManager.getManager().useDisplayLists();
			
			// Push initial game states onto the stack maintained by Engine
			engine.changeState(new RunState(data).init(engine));
			engine.pushState(new OverlayState(data).init(engine));		// and Overlay states
			
			glColor3f(1f,1f,1f);			// Workaround for everything becoming last set colour after load
			
			return SUCCESS;
		}
		
		return SUCCESS;
	}



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
								(Globals.window_height / 2) - 65f, TEXT, Color.green);
				
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


	// Unused
	@Override
	public void keyInput(int key) {}

	@Override
	public void mouseInput(int x, int y, boolean leftDown, boolean rightDown) { }

	@Override
	public void pause() {}

	@Override
	public void resume() {}
	
	
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
	public void preload(Engine engine) {
		
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
			
			if(models_loaded < MODELS.length)
				next_resource =	MODELS[models_loaded].substring(
						MODELS[models_loaded].lastIndexOf("/") +1, 
						MODELS[models_loaded].length() 
					);
		} 
		
		
		// Do this only when everything is loaded
		if(models_loaded >= MODELS.length) {
			OBJManager.getManager().setLoaded();
		} else {
			loadbar.append("-");
		}
	}



	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
