package csc3202.Engine;

import static org.lwjgl.opengl.GL11.*;
import csc3202.Engine.Interfaces.DisplayList;
import csc3202.Engine.Interfaces.GameState;
import csc3202.Engine.Interfaces.StateController;
import csc3202.Engine.Interfaces.Texturable;
import csc3202.Engine.OBJLoader.OBJManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.TextureImpl;

/**
 * Engine class run when the game is started.
 * 
 * Handles the main program loop and maintains a List of
 * GameStates- which may be stacked.
 * 
 * Updates, input handling and rendering operations are
 * then passed down into registered GameStates
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Engine implements StateController {
	
	private List<GameState> states;
	private HashSet<DisplayList> lists;
	private HashSet<Texturable> texturables;
	

	/** frames per second */
	int fps = 0;
	
	/** last fps time */
	long last_fps = 0;
	
	private volatile boolean running = true;
	private boolean tog_fullscreen = false;
	private boolean is_fullscreen = false;	// true = Fullscreen by default
	
	/** Render in wireframe mode **/
	private boolean wireframe = false;
	
	
	/**
	 * Constructor
	 */
	public Engine() {
		states = new ArrayList<GameState>();
		lists = new HashSet<DisplayList>();
		texturables = new HashSet<Texturable>();
		
		OBJManager.setEngine(this);
	}
	
	
	
	/**
	 * Run the main engine loop
	 */
	public void run() {
		
	    /////////////////////////////////////////////////////////////////////		
		// Initialization
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		try {
			setGlobalDisplayMode();
			init(is_fullscreen);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		
		// Set-up initial states as pushed onto the stack (hopefully) by calling class
		initStates();
		
		long currentFrame = Sys.getTime();
	    long lastFrame = currentFrame;
	    long deltaTime = 0;

	    last_fps = now(); // Init fps timer
	    
	    int error;
	    
	    /////////////////////////////////////////////////////////////////////
		// Main program loop
		while (!Display.isCloseRequested() && running) {
			
			System.gc();	// Collect last frame's garbage (may or may not work, System.gc() is only a hint to the garbage collector
			
			currentFrame = Sys.getTime();
		    deltaTime = currentFrame - lastFrame;
		    lastFrame = currentFrame;

			check_fullscreen();
			
			pollInput();
			update(deltaTime);
			render();
			Display.update(); 		// Flushes OpenGL pipeline and swaps back and front buffers. perhaps waits for v-sync.
			
			updateFPS(); 			// update FPS Counter
			
			if((error = glGetError()) != 0)
				System.out.println("oops! " + error);
		}

		if(running)
			this.stop();		// Clean up states
		
		Display.destroy();
	}
	
	
	
	/**
	 * Stop the engine
	 */
	public void stop() {
		running = false;
		
		// Clean-up states
		for (GameState s : states) {
			s.cleanup();
		}
	}
	
	
	
	/**
	 * Check if the engine is running.
	 * Anyone with a reference can do this.
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	
	
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		
		if (now() - last_fps > 1000) {	// Sufficient granularity
			Display.setTitle(Globals.WINDOW_TITLE + " - " + fps + "fps");
			fps = 0;
			last_fps += 1000;
		}
		fps++;	// Increment Frame counter
	}
	
	
	
	/**
	 * What's the time, Mister Wolf?
	 * @return
	 */
	public long now() {
		return System.nanoTime() / 1000000; // call before loop to initialise fps timer;
	}
	
	
	
	/**
	 * Toggle fullscreen if appropriate bool is set in the Engine class.
	 */
	public void check_fullscreen() {
		
		try {
			// Toggle fullscreen - recreate the entire context.
			if(tog_fullscreen && !is_fullscreen) {
				Display.destroy();
				init(true);
				is_fullscreen = true;
			}
			else if(tog_fullscreen && is_fullscreen) {
				Display.destroy();
				init(false);
				is_fullscreen = false;
			}
			
			tog_fullscreen = false;
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Poll for keyboard and mouse input, and call the appropriate
	 * handler methods further down
	 */
	public void pollInput() {
		
		// Call for all states
		for (GameState s : states) {
			s.mouseInput(Mouse.getX(), Mouse.getY(), Mouse.isButtonDown(0), Mouse.isButtonDown(1));
		}
		
		// Handle keyboard input
		while (Keyboard.next()) {
			
			int key = Keyboard.getEventKey();

			// Call for all states
			try {
				for (GameState s : states) {
					s.keyInput(key);
				}
			} catch (java.util.ConcurrentModificationException e) {
				System.out.println("State stack changed");
									// This isn't really the greatest way to do this ever-
				return;				// Exceptions should never be used for flow control like this!
			}
			
			if (Keyboard.getEventKeyState()) {
								
				switch(key) {
					case Keyboard.KEY_F11:
						tog_fullscreen = true;
						break;
//					case Keyboard.KEY_F2:
//						wireframe = !wireframe;
//						break;
					default:
						break;
				}
			}
		}
	}
	
	
	
	/**
	 * Init OpenGL.
	 * 
	 * This method is called when the Engine initially runs, and
	 * subsequently when switching between windowed and fullscreen
	 *  
	 * @param fullscreen
	 * @throws LWJGLException
	 */
	public void init(boolean fullscreen) throws LWJGLException {
		
		Display.setTitle(Globals.WINDOW_TITLE);
		
		Mouse.setGrabbed(true);		// Hide mouse pointer
		Globals.coord_ratio_x = Globals.window_width / Globals.FIELD_WIDTH;
		Globals.coord_ratio_y = Globals.window_height / Globals.FIELD_HEIGHT;
		
		if(fullscreen) {
			Display.setFullscreen(true);
			Display.create();
			glViewport(0, 0, Display.getDesktopDisplayMode().getWidth(), Display.getDesktopDisplayMode().getHeight());
		}
		else {
			Display.setDisplayMode(new DisplayMode(Globals.window_width, Globals.window_height));
			Display.create();
			glViewport(0, 0, Globals.window_width, Globals.window_height);
		}
	
		Display.setVSyncEnabled(true);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		glClearColor (0.0f, 0.0f, 0.0f, 1.0f);				// Black Background
		glClearDepth (1.0f);								// Depth Buffer

		FontManager.getManager().resetFonts();				// Reset fonts
		resetTexturables();									// Reset texturable objects
		resetDisplayLists();								// Reset display lists on models
		
		
		glLineWidth(1.2f);
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
	}



	/**
	 * Calculate and set the optimal display mode
	 * @throws LWJGLException
	 */
	private void setGlobalDisplayMode() throws LWJGLException {
		
		DisplayMode[] modes = Display.getAvailableDisplayModes();
		
		int max_x = 0, max_y = 0;
		for(DisplayMode m : modes) {
//			System.out.println(m.toString());
			
			if(max_x < m.getWidth())
				max_x = m.getWidth();
			
			if(max_y < m.getHeight())
				max_y = m.getHeight();
		}

		// Do "safe" init- don't change the default if it's smaller.
		if(Globals.window_width > max_x && Globals.window_height > max_y) {
			Globals.window_width = max_x;
			Globals.window_height = max_y;
		}
	}
	
	
	
	/**
	 * Update all GameStates registered in the Engine-
	 * 	this will subsequently update their objects.
	 * 
	 * @param delta
	 */
	public void update(long delta) {
		
		try {
			for (GameState s : states) {
				s.update(delta);
			}
		} catch (java.util.ConcurrentModificationException e) {
			System.out.println("State stack changed");
			return;		// Thrown when a GameOver state is added to the stack.
		}
	}
	
	
	
	/**
	 * Clear the screen and call each registered State's render() method
	 */
	public void render() {

		// Clear the screen so we don't see previous frame
		glClearColor (0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		if(wireframe)
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);	// Debug
		else
			glPolygonMode(GL_FRONT, GL_FILL);
		
		for (GameState s : states) {

		    glPushAttrib(GL_ENABLE_BIT);
			s.render();
		    glPopAttrib();
		}
	}
	
	
	
	/**
	 * Set perspective to Orthographic 
	 */
	public void set2D() {
		
		glDisable(GL_DEPTH_TEST);			// Disable Depth
		glDisable(GL_LIGHTING); 			// lighting
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		glOrtho(0.0f, Globals.window_width, 0.0f, Globals.window_height, 1.0f, -1.0f );	// Specify orthographic projection 
	}
	
	
	
	/**
	 * Set projection to Perspective
	 */
	public void set3D() {

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		glEnable(GL_DEPTH_TEST);								// Z-buffer depth test
		glDepthFunc (GL_LEQUAL);								// Type Of Depth Testing
		glHint (GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);		// Set Perspective Calculations To Most Accurate
		
		GLU.gluPerspective (
				45.0f, 
				(float) Globals.window_width 
					/ (float) Globals.window_height,				// Calculate The Aspect Ratio Of The Window
				0.1f, 
				2000.0f
			);
	}
	
	
	
	/**
	 * Turn on the lighting
	 */
	public void light() {

		ByteBuffer lighting_buffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
		
		float[] model = {0.1f, 0.1f, 0.1f, 1.0f};

		float[] ambient = {0.05f, 0.05f, 0.05f, 1.0f};
		float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] specular = {0.8f, 0.8f, 0.8f, 1.0f};
		float[] position0 = {10.0f, Globals.FIELD_WIDTH, 10.0f, 0.0f};
		float[] position1 = {-10.0f, 0.0f, -Globals.FIELD_HEIGHT, 0.0f};
		
        glShadeModel(GL_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

		glLightModel(GL_LIGHT_MODEL_AMBIENT, 
				(FloatBuffer) lighting_buffer.asFloatBuffer().put(model));
		
		glLight(GL_LIGHT0, GL_AMBIENT, (FloatBuffer) lighting_buffer.asFloatBuffer().put(ambient).flip());
		glLight(GL_LIGHT0, GL_DIFFUSE, (FloatBuffer) lighting_buffer.asFloatBuffer().put(diffuse).flip());
		glLight(GL_LIGHT0, GL_SPECULAR, (FloatBuffer) lighting_buffer.asFloatBuffer().put(specular).flip());
		glLight(GL_LIGHT0, GL_POSITION, (FloatBuffer) lighting_buffer.asFloatBuffer().put(position0).flip());
		
		glLight(GL_LIGHT1, GL_AMBIENT, (FloatBuffer) lighting_buffer.asFloatBuffer().put(ambient).flip());
		glLight(GL_LIGHT1, GL_DIFFUSE, (FloatBuffer) lighting_buffer.asFloatBuffer().put(diffuse).flip());
		glLight(GL_LIGHT1, GL_SPECULAR, (FloatBuffer) lighting_buffer.asFloatBuffer().put(specular).flip());
		glLight(GL_LIGHT1, GL_POSITION, (FloatBuffer) lighting_buffer.asFloatBuffer().put(position1).flip());

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_DIFFUSE);
	}
	


	//////////////////////////////////////////////////////////////////////////////////
	// STATE HANDLING
	
	
	/**
	 * Wipe out all current loaded states and add the one passed in
	 * @param state
	 */
	@Override
	public void changeState(GameState state) {

		// Clean the current state
		for(GameState s : states)
			s.cleanup();
		
		states.clear();

		// store and init the new state
		states.add(state);
	}
	
	

	/**
	 * Put a new state on top (menu, for example) and pause the one underneath
	 * @param state
	 */
	@Override
	public void pushState(GameState state) {
		// store and init the new state
		states.add(state);
	}

	
	
	/**
	 * Remove the state on top
	 */
	@Override
	public void popState() {
		// clean-up the current state
		states.get(states.size() -1).cleanup();
		states.remove( states.size() -1 );

		// resume previous state
		if ( ! states.isEmpty() ) {
			states.get( states.size() -1 ).resume();
		}
	}
	
	

	/**
	 * Init states in the stack 
	 * 	Run ONLY after GL initialisation
	 */
	@Override
	public void initStates() {
		
		for (GameState s : states) {
			s.init(this);
		}
	}
	
	
	/**
	 * Register a model which must be re-initialised on fullscreen changes
	 */
	public void registerList(DisplayList list) {
		
		if(! lists.contains(list))
			lists.add(list);
	}
	
	
	/**
	 * Register a model which must be re-initialised on fullscreen changes
	 */
	public void registerTexturable(Texturable tex) {
		
		if(! texturables.contains(tex))
			texturables.add(tex);
	}
	
	
	/**
	 * Iterate through and reset display lists
	 */
	private void resetTexturables() {
		
		TextureImpl.bindNone();
		TextureManager.getManager().clear();
		
		for (Texturable t: texturables) {
			t.loadTextures();				// Reload textures into state
		}
	}
	
	
	/**
	 * Iterate through and reset display lists
	 */
	private void resetDisplayLists() {
		
		for (DisplayList l : lists) {
			l.useDisplayList();
		}
	}
}
