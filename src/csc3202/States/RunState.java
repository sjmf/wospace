package csc3202.States;

import static csc3202.Engine.Globals.*;
import static org.lwjgl.opengl.GL11.*;
import static java.lang.Math.*;

import csc3202.Engine.AI.EnemySpawner;
import csc3202.Engine.AI.RandomSpawner;
import csc3202.Engine.*;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Engine.Interfaces.GameState;
import csc3202.Engine.OBJLoader.OBJManager;
import csc3202.Engine.OBJLoader.OBJModel;
import csc3202.Engine.Sound.MP3;
import csc3202.Entities.*;
import csc3202.Entities.Ship.ShipState;

import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;


/**
 * The main game state
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012 / Apr 13
 */
public class RunState implements GameState {

	private enum PlayState {
		STARTING, 		// When a new round is starting
		PLAYING,		// Round in progress
		DEFEATED,		// An invasion was defeated (spawn another)
		GAME_OVER		// The game is over
	};

	private PlayState state = PlayState.STARTING;

	private Engine engine;
	private GameData data;	// Reference to the game-data-holding object
	
	// Camera variables
	private Camera camera;
	
	// X/y/z and pitch/yaw/roll vectors for different camera views
	private final Vector3f topdown_xyz = new Vector3f(-(Globals.FIELD_WIDTH / 2f), -262f, Globals.FIELD_HEIGHT / 2f);
	private final Vector3f topdown_pyr = new Vector3f(90f, 0f, 0f);
	
	
	// Different game entity types	
	private ArrayList<Laser> ship_lasers;
	private ArrayList<Laser> enemy_lasers;
	private ArrayList<Enemy> enemies;
	private ArrayList<Enemy> destroyed_enemies;
	
	private EnemySpawner spawner;

	// The player ship
	private Ship ship;
	
	// Key-down states for ship movement/firing
	private boolean keydown_left = false;
	private boolean keydown_right = false;
	private boolean keydown_up = false;
	private boolean keydown_down = false;
	private boolean fire = false;

	// Is the game paused?
	private boolean paused = false;
	
	// Have we shown the Game Over screen?
	private boolean game_over = false;
	
	private int mouse_x; 
	private int mouse_y;

	// Audio stuff probably needs to be abstracted to another class or even thread
	private MP3 mp3;
	private final String defaultMP3File = "res/Rushjet1_-_06_-_Return_to_Control.mp3";
	
	/**
	 * Constructor- Initialise State and set-up entities
	 */
	public RunState(GameData data) {
		
		this.data = data;
		this.mp3 = new MP3(defaultMP3File);
		spawner = new EnemySpawner();
	}
	
	

	@Override
	public GameState init(Engine engine) {

		this.mp3.play();
		
		this.engine = engine;
		this.camera = new Camera();
//		Hitbox.render = true;
		
		// Set up camera (deep copy vector so it doesn't get messed up when the camera moves)
		camera.setCameraPos(cloneVec3(topdown_xyz), cloneVec3(topdown_pyr));
		camera.lock();
		
		
		// Retrieve models we need a reference to
		OBJModel enemy_model = OBJManager.getManager().getModel(Globals.ENEMY_MODEL_PATH);
		enemy_model.setRot(new Vector3f(180,0,0));
		
		state = PlayState.STARTING; // Set state to starting state
		
		// Discard old entity lists so we don't get duplicates
		ship_lasers = new ArrayList<Laser>();
		enemy_lasers = new ArrayList<Laser>();
		enemies = new ArrayList<Enemy>();
		destroyed_enemies = new ArrayList<Enemy>();

		// Create Ship
		ship = new Ship();
		ship.setState(ShipState.OK);	// Ship at beginning of round does not have timed invincibility buff
		
		// Start spawning enemies
		spawner.startSpawning(new RandomSpawner());
		
		return this; // Success
	}

	
	
	/**
	 * Update the state of play.
	 * Ordering of code in this method is very important!
	 * 
	 * @return SUCCESS if Success, WAIT if paused
	 * @see Engine.Interfaces.GameState#update(long)
	 */
	@Override
	public int update(long delta) {
		
		if(state != PlayState.GAME_OVER)
			camera.update(delta);
				
		if (paused)
			return WAIT; // Do nothing if paused

		
		// Get new enemies
		spawner.populate(enemies);
		
		
		///////////////////////////////////////////////////////////////////////
		// Enemy updates & Collision detection

		
		Iterator<Enemy> de = destroyed_enemies.iterator();
		while(de.hasNext()) {
			Enemy i = de.next();
			if(i.update(delta) == -1) {
				de.remove();
			}
		}
		
		// Move on-screen lasers and check exit from arena bounds
		Iterator<Laser> li = ship_lasers.iterator();
		boolean laserRemoved = false;
		while (li.hasNext()) {
			Laser l = li.next();
			l.update(delta);
			
			if (l.move(delta) == DONE) {
				li.remove();	// Need to avoid ConcurrentModificationException
			}
		}
		
		// Check lasers collision with enemy
		li = ship_lasers.iterator();
		laserRemoved = false;
		Iterator<Enemy> ei;
		while (li.hasNext()) {
			Laser l = li.next();	
			
			ei = enemies.iterator();
			while(ei.hasNext()) {
				Enemy e = ei.next();
				if(l.collides(e)) {
					destroyed_enemies.add(e);				// Add to destruction animation stack
					e.destroy();
					ei.remove();	// Remove enemy
					li.remove();	// Remove laser 
					laserRemoved = true;
					data.addScore(10);	// Increment score
					break;			// avoiding IllegalStateException
				}
			}
			if(laserRemoved) {
				li = ship_lasers.iterator();
				laserRemoved = false;
			}
		}

		///////////////////////////////////////////////////////////////////////
		// Ship collision check
		int collided = 0;
		
		li = enemy_lasers.iterator();
		while(li.hasNext())	{
		    Laser l = li.next();
		    if(l.move((delta)) == -1) 
		    	li.remove();		
			
			// Check ship collision with fire.
			// Order important here because ship can be invincible
			if(ship.collides(l)) {
				l.destroy();
				li.remove();
				collided++;	// Bang! You're dead.
			}
		}
		
		ei = enemies.iterator();
		while(ei.hasNext()) {

			Enemy e = ei.next();
			e.update(delta);
			if(ship.collides(e)) {
				collided++;	// Ship collision with enemy

				destroyed_enemies.add(e);
				e.destroy();
				ei.remove();
			}
			else if(e.move(delta) == DONE) {
				e.destroy();
				ei.remove();
			}  
		}
		
		
		if (collided > 0) {
			state = PlayState.DEFEATED;
			ship.setDirection(cloneVec3(Entity.NONE));	// Workaround for ship continuing in direction on death

			ship.destroy();								// Call destroy on ship. Note that this only triggers the destruction animation.
		}
		
		////////////////////////////////////////////////////////////////////
		// Check remaining lives
		if(data.getLives() < 0) {
			if(!game_over) {	// You've had your chips!				// Create the "Game Over" state
				spawner.stopSpawning();
				engine.pushState(new GameOverState(data).init(engine));	//  and push it onto the stack
				game_over = true;										//  - but only once!
			}
			
			return DONE; 				// Return ship destroyed if last life (0) used
		}
		
		
		////////////////////////////////////////////////////////////////////
		// Keypress evaluation
		Vector3f direction = new Vector3f(0,0,0);
		
		if (keydown_left && !keydown_right) {
			direction = Vector3f.add(direction, Entity.LEFT, null);
		} else if (keydown_right && !keydown_left) {
			direction = Vector3f.add(direction, Entity.RIGHT, null);
		}
		
		if(keydown_up && !keydown_down) {
			direction = Vector3f.add(direction, Entity.UP, null);
		} else if(keydown_down && !keydown_up) {
			direction = Vector3f.add(direction, Entity.DOWN, null);
		} 
		
		ship.setDirection(direction);
		
		
		////////////////////////////////////////////////////////////////////
		// Ship Updates
		if( ship.update(delta) == DONE ) {			// Ship Updates: DONE if animation complete (respawn or game over)
			data.decrementLives();					// Decrement lives
			if(data.getLives() >= 0) {
				ship = new Ship();		// Respawn ship
			} else {
				state = PlayState.GAME_OVER;
			}
				
			return SUCCESS;								// Don't do anything else this tick
		}

		// Move the ship at correct (calculated) speed
		ship.move(SHIP_SPEED * delta);


			
		////////////////////////////////////////////////////////////////////
		// Firing
		if (fire) {
			ship.fireLaser(ship_lasers);		// Ship lasers
		}

		return SUCCESS;
	}

	
	
	@Override
	public int render() {

		glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_ENABLE_BIT | GL_TRANSFORM_BIT);

		glColor3f(1.0f, 1.0f, 1.0f);
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
			engine.set3D();
		    
			glMatrixMode(GL_MODELVIEW);

		    glEnable(GL_TEXTURE_2D);
		    glEnable(GL_BLEND);
		    glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		    
			glPushMatrix();
				camera.updateView();							// Update FPS camera
				engine.light();									// And lighting
								
				if(state != PlayState.GAME_OVER)
					ship.render();
				
				for (Laser l : ship_lasers) 
					l.render();
				
				for(Laser l : enemy_lasers)
					l.render();
				
				for(Enemy e : enemies)
					e.render();

				for(Enemy e : destroyed_enemies)
					e.render();
				
				FIELD_HITBOX.render();
				
			glPopMatrix();
	
			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		
		glPopAttrib();

		
		// Debug rendering of mouse/world coordinate conversion
		if(Hitbox.render) {
			glPushAttrib(GL_DEPTH_BUFFER_BIT | GL_ENABLE_BIT | GL_TRANSFORM_BIT);
			
			glMatrixMode(GL_PROJECTION);
			glPushMatrix();
				engine.set2D();
				
				glMatrixMode(GL_MODELVIEW);
	
			    glDisable(GL_TEXTURE_2D);
			    glPushMatrix();
			    glColor3f( 255, 255, 255 );
				glBegin( GL_LINE_LOOP );
					glVertex3f(ship.getPosition().x * Globals.coord_ratio_x, -ship.getPosition().z * Globals.coord_ratio_y, 0.0f);
					glVertex3f(mouse_x, -ship.getPosition().z * Globals.coord_ratio_y, 0.0f );
					glVertex3f(mouse_x, mouse_y, 0.0f );
				glEnd();
			    
				glPopMatrix();
				
				glMatrixMode(GL_PROJECTION);
			glPopMatrix();
			glPopAttrib();
		}
		
		return SUCCESS;
	}



	@Override
	public void mouseInput(int mouse_x, int mouse_y, boolean leftDown, boolean rightDown) {
		
		this.mouse_x = mouse_x;
		this.mouse_y = mouse_y;
		
		// Convert mouse coordinates to 2D world coordinates
		float world_x = mouse_x / Globals.coord_ratio_x;
		float world_y = mouse_y / Globals.coord_ratio_y;
		
		// Calculate new ship orientation using trigonometry
		double adjacent = world_x - ship.getPosition().x ;		// Width of triangle
		double opposite = world_y + ship.getPosition().z ;		// Height of triangle
		
		// TODO: consider how to do this without sqrt!
		double hypotenuse = sqrt( pow(adjacent,2) + pow(opposite,2) );	// Pythagoras finds hyp
		
		double angle = asin(opposite / hypotenuse);
		
		// Correct angle for quadrants in radians - faster to use comparisons on adj&opp than tan/cos theta
		if(adjacent < 0) {
			if(opposite < 0)			// 3rd quadrant (adj -ve, opp -ve)
				angle = PI - angle;		// -ve angle
			 else						// 2nd quadrant (adj -ve. opp +ve)
				angle = PI - angle;
			
		} else if(opposite < 0) { 			// 4th quadrant (adj +ve, opp -ve)
				angle = 2*PI + angle;
		}								// else 1st quadrant (+ve +ve) no action
		
		// Get the unit vector of the angle and set it on the ship
		ship.setOrientation(new Vector3f((float) cos(angle - PI/2), 0.0f, (float) sin(angle - PI/2)));
		
//		System.out.println(
//			  "_MOUSE   x:" + mouse_x + " y:" + mouse_y +
//			"\n_WORLD   x:" + world_x + " y:" + world_y +
//			"\n_SHIP    x:"+ ship.getPosition().x + " y:" + ship.getPosition().z +
//			"\n_LENGTHS o:" + (int) opposite + " a:" + (int) adjacent + " h:" + (int) hypotenuse +
//			"\n_ANGLE:  " + angle * RAD + "\n"
//		);
		
		camera.mouseInput();
		
		if(leftDown)
			fire=true;
		else
			fire=false;
	}

	
	
	@Override
	public void keyInput(int key) {

		if(state == PlayState.GAME_OVER)
			return;
		
		camera.keyInput(key);
		
		if (Keyboard.getEventKeyState()) {

			switch (key) {
				// Movement key bindings
			case Keyboard.KEY_LEFT:
			case Keyboard.KEY_A:
				keydown_left = true;
				break;
			case Keyboard.KEY_RIGHT:
			case Keyboard.KEY_D:
				keydown_right = true;
				break;
			case Keyboard.KEY_UP:
			case Keyboard.KEY_W:
				keydown_up = true;
				break;
			case Keyboard.KEY_DOWN:
			case Keyboard.KEY_S:
				keydown_down = true;
				break;
			case Keyboard.KEY_SPACE:
				fire = true;
				break;

				// Control key bindings
			case Keyboard.KEY_F3:
				Hitbox.render = !Hitbox.render;			// Toggle hitbox rendering on/off
				break;
			case Keyboard.KEY_P:	// Fall through to KEY_F4
			case Keyboard.KEY_F4:
//				camera.toggleLock();
				data.pause();
				if(paused)
					this.resume();
				else
					this.pause();						// Toggle pause for the bottom state
				break;
// Debug:
//			case Keyboard.KEY_K:						// Set score to 0 to trigger "Game Over" message
//				ship.setState(ShipState.DESTROYED);		// Destroy directly. Don't run destroy() animation
//				ship.hide();
//				data.setLives(0);
//				break;
//			case Keyboard.KEY_G:						// Toggle Insanity Mode on
//				Globals.ship_fire_rate = 10;			//  Note: removed, broke too many minds. Make it a powerup instead!
//				Globals.enemy_fire_rate = 10; 			//ms
//				break;
			case Keyboard.KEY_ESCAPE:					// Return to menu state
//				data.reset(Globals.LIVES);
				engine.stop();
				break;
			default:
				break;
			}

		} else {	// Key-up handling

			switch (key) {
			case Keyboard.KEY_LEFT:
			case Keyboard.KEY_A:
				keydown_left = false;
				break;
			case Keyboard.KEY_RIGHT:
			case Keyboard.KEY_D:
				keydown_right = false;
				break;
			case Keyboard.KEY_UP:
			case Keyboard.KEY_W:
				keydown_up = false;
				break;
			case Keyboard.KEY_DOWN:
			case Keyboard.KEY_S:
				keydown_down = false;
				break;
			case Keyboard.KEY_SPACE:
				fire = false;
				break;
			default:
				break;
			}
		}
	}
	

	
	@Override
	public void pause() {
		paused = !paused;
		spawner.stopSpawning();
	}

	

	@Override
	public void resume() {
		paused = false;
		spawner.startSpawning(new RandomSpawner());
	}



	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		spawner.stopSpawning();
		mp3.stop();
	}
}
