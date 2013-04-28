package csc3202.Engine;

import org.lwjgl.util.vector.Vector3f;


/**
 * The game is starting to look like more constants than logic.
 * I moved them in here. import static <this class> where needed.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Globals {
	
	public static final float RAD = (float) (180 / Math.PI);
	
	// Class status return values (more semantic than magic numbers)
	public static final int WAIT = 1; 
	public static final int SUCCESS = 0;
	public static final int DONE = -1;
	public static final int DESTROY = -2;
	public static final int FAILURE = 0xFF;
	
	// Window dimensions
	// Display holders used by many classes
	public static final int FIELD_WIDTH = 600;									// Playing-field size
	public static final int FIELD_HEIGHT = 330;
	public static final Hitbox FIELD_HITBOX = 									// 2 hours debugging.
			new Hitbox( new Vector3f(0, -10, -FIELD_HEIGHT),					// Order is sodding important.
						new Vector3f(FIELD_WIDTH,0,0));							// I'm not bitter.
	
	public static int window_width = 1280; // 3840;//							// Default render window size
	public static int window_height = 720; // 1080;//							// for windowed resolutions (720p)
	
	public static float coord_ratio_x = window_width / FIELD_WIDTH;
	public static float coord_ratio_y = window_height / FIELD_HEIGHT;
	
	public static int text_scale_width = 2000; 									// Render text on a larger size
	public static int text_scale_height = 1000;									//  to make it look more crisp

	public static final String WINDOW_TITLE = "Waste Of Space (pre-alpha)";
	
	// File to read/write
	public static final String HIGH_SCORE_FILE = "res/scores.txt";
		
	
	// Font properties for centering text
	public static final float FONT_48PT = 24.0f;								// More or less?
	public static final float FONT_36PT = 18.0f;								// ????
	public static final float FONT_24PT = 12.0f;								// Width of a character at 24pt
	public static final float FONT_18PT = 09.0f;								// Width of a character at 18pt
	
	
	// Overlay constants
	public static final float BORDER_WIDTH = 55.0f;
	public static final String OVERLAY_FONT = "res/Anonymous_Pro.ttf";
	
	
	// Maximum distance from edge of field allowed
	public static final int X_OFFSET = 20;
	public static final int Y_OFFSET = 20;
	public static final int Z_OFFSET = 20;
	
	
	// Destruction animation constants
	public static final float ALPHA_FADE = 0.001f;								// Alpha fade speed 
	public static final float ROTATE_SPEED = 0.05f;								// Rotate fade speed
	public static final float FALL_SPEED = 0.06f;								// Fall speed for fall-based destruction transforms

	
	// Enemy setup constants
	public static final float MAX_ENEMY_SPEED = 0.12f;							// units/f
	public static final float ENEMY_SPEED = 0.02f;
	public static final float ENEMY_LASER_SPEED = 0.05f;
	public static final int ANIMATION_DELAY = 500;								// Delay before flipping enemy state
	public static int enemy_fire_rate = 100;									// Min. Laser firing delay
	
	
	// Ship data
	public static final int LIVES = 3;
	public static final int SHIP_FIRE_RATE = 120;								// ms
	public static final int SHIP_MAX_FIRE_RATE= 40;
	public static final int POWERUP_FIRE_RATE_INC = 40;							// decrease ship fire delay by 20ms
	public static final int POWERUP_SPAWN_RATE = 20000;							// ms
	public static final int POWERUP_DURATION = 10000;							// 10 s
	public static final long INVINCIBILITY_FLASH_DURATION = 500;				// 1/2 second
	public static final long INVINCIBILITY_DURATION = 3000;						// 4 seconds
	public static final float SHIP_SPEED = 0.15f;
	public static final float LASER_SPEED = 0.3f;
	public static final float SHIP_VELOCITY_INC = 0.01f;
	public static final int BOMBS_INITIAL_SUPPLY = 1;
	public static final int MAX_BOMBS = 4;
	
	
	// Entity model paths
	public static final String ENEMY_MODEL_PATH =  "res/models/box.obj";
	public static final String LASER_MODEL_PATH = "res/models/laser.obj";
	public static final String SHIP_MODEL_PATH = "res/models/ship.obj";
	public static final String BOMB_MODEL_PATH = "res/models/bomb.obj";
	public static final String POWERUP_MODEL_PATH = "res/models/power.obj";
	
	
	// Base game speed by which others are multiplied
	public static volatile float game_speed = 0f;
	public static volatile long spawn_rate = 500l;	// Spawn threadsleep

	
	// Not meaningful to instantiate this class
	private Globals() {}
}
