package csc3202.States;

import static csc3202.Engine.Globals.*;
import static org.lwjgl.opengl.GL11.*;

import csc3202.Engine.AI.SpawnControl;
import csc3202.Engine.AI.SpeedControl;
import csc3202.Engine.*;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Engine.Interfaces.GameState;
import csc3202.Engine.OBJLoader.OBJManager;
import csc3202.Engine.OBJLoader.OBJModel;
import csc3202.Engine.Sound.MP3ToPCM;
import csc3202.Entities.*;
import csc3202.Entities.Powerup.PType;
import csc3202.Entities.Ship.ShipState;
import csc3202.Entities.Spawners.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
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
		GAME_OVER		// The game is over (either won/lost)
	};

	private PlayState state = PlayState.STARTING;

	private Engine engine;
	private GameData data;	// Reference to the game-data-holding object
	
	// Camera variables
	private Camera camera;
	
	// X/y/z and pitch/yaw/roll vectors for different camera views
	private final Vector3f topdown_xyz = new Vector3f(-(Globals.FIELD_WIDTH / 2f), -400f, Globals.FIELD_HEIGHT / 2f);
	private final Vector3f topdown_pyr = new Vector3f(90f, 0f, 0f);
	
	
	// Different game entity types	
	private ArrayList<Laser> ship_lasers;
	private ArrayList<Laser> enemy_lasers;
	private ArrayList<Enemy> enemies;
	private ArrayList<Enemy> destroyed_enemies;
	private ArrayList<Powerup> powerups;

	// The player ship
	private Ship ship;
	
	// Key-down states for ship movement/firing
	private boolean keydown_left = false;
	private boolean keydown_right = false;
	private boolean keydown_up = false;
	private boolean keydown_down = false;
	private boolean fire = false;
	private boolean bomb = false;
	private boolean bombed = false;

	// Is the game paused?
	private boolean paused = false;
	
	// Have we shown the Game Over screen?
	private boolean game_over = false;
	private boolean game_won = false;
	private long duration;
	
	private int mouse_x; 
	private int mouse_y;

	// Audio stuff probably needs to be abstracted to another class or even thread
	private MP3ToPCM mp3;
	private SpawnControl<Enemy> enemySpawner;
	private SpawnControl<Powerup> powerupSpawner;
	private SpeedControl speedcont;
	
	
	/**************************************************************************
	 * Constructor- Initialise State and set-up entities
	 */
	public RunState(GameData data) {
		
		this.data = data;
		try {
			this.mp3 = new MP3ToPCM(data.getMp3File());
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// This is a consequence of sticking things in lists, not Maps. :/
		// This will only iterate maximum twice so it's fine
		int i=0;
		while(data.getAnalysis().get(i).getPluginName().compareTo("amplitudefollower") != 0) i++;
		
		speedcont = new SpeedControl(data.getAnalysis().get(i));
			
		// Set the length of the game session
		duration = data.getAnalysis().get(0).getDuration();
	}
	
	

	/**************************************************************************
	 * Init the state of play
	 */
	@Override
	public GameState init(Engine engine) {
		
		this.engine = engine;
		this.camera = new Camera();
		
		// Set up camera (deep copy vector so it doesn't get messed up when the camera moves)
		camera.setCameraPos(Utils.cloneVec3(topdown_xyz), Utils.cloneVec3(topdown_pyr));
		camera.lock();
		
		
		// Retrieve models we need a reference to
		OBJModel enemy_model = OBJManager.getManager().getModel(Globals.ENEMY_MODEL_PATH);
		enemy_model.setRot(new Vector3f(180,0,0));
		
		state = PlayState.STARTING; 											// Set state to starting state
		
		// Discard old entity lists so we don't get duplicates
		ship_lasers = new ArrayList<Laser>();
		enemy_lasers = new ArrayList<Laser>();
		enemies = new ArrayList<Enemy>();
		destroyed_enemies = new ArrayList<Enemy>();
		powerups = new ArrayList<Powerup>();
		
		// Create Ship
		ship = new Ship();														// Ship at beginning of round does 
		ship.setState(ShipState.OK);											//  not have timed invincibility buff
		
		data.setStartTime(System.currentTimeMillis());

		// Launch threads for spawning entities:
		enemySpawner = new SpawnControl<Enemy>();
		enemySpawner.start(new EnemySpawner());									// Start spawning enemies
		
		powerupSpawner = new SpawnControl<Powerup>();
		powerupSpawner.start(new PowerupSpawner());
		
		mp3.play();
		speedcont.start();
		
		return this; // Success
	}

	
	
	/**************************************************************************
	 * Update the state of play.
	 * Ordering of code in this method is very important!
	 * 
	 * I'd love to move this to a threaded/bucketed architecture at some point
	 * 
	 * @return SUCCESS if Success, WAIT if paused
	 * @see Engine.Interfaces.GameState#update(long)
	 */
	@Override
	public int update(long delta) {
		
//		if(state != PlayState.GAME_OVER)
//			camera.update(delta);
				
		if (paused)
			return WAIT; 														// Do nothing if paused
			
		
		enemySpawner.populate(enemies);											// Get new enemies
		powerupSpawner.populate(powerups);
		
		
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
				li.remove();													// Need to avoid ConcurrentModificationException
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
					if(e.damage()) {											// Returns true when killed
						
						if(e instanceof BlasterEnemy)							// Check for bomb and explode it
							ship_lasers.addAll(Utils.makeExplosion(
									e.getPosition(), 
									new Vector3f(1.0f, 0.2f, 1.0f),
									32
								));
						
						destroyed_enemies.add(e);								// Add to destruction animation stack
						e.destroy();
						ei.remove();											// Remove enemy
						li.remove();											// Remove laser 
						laserRemoved = true;
						data.addScore(e.getScore());							// Increment score
						break;													//  avoiding IllegalStateException
					}
				}
			}
			if(laserRemoved) {
				li = ship_lasers.iterator();
				laserRemoved = false;
			}
		}
		
		// Move on-screen powerups and check exit from arena bounds
		Iterator<Powerup> pi = powerups.iterator();
		while (pi.hasNext()) {
			Powerup p = pi.next();
			p.update(delta);
			
			if(ship.collides(p)) {												// Check ship/powerup collision
				
				if(p.getType() == PType.FIRERATE) {								// Collided with firerate powerup
					ship.powerUp();
				} else {														// with Bomb powerup
					data.incBombs();
				}
				
				data.incMultiplier();
				
				pi.remove();
			}
			else if (p.move(delta) == DONE) {
				pi.remove();													// avoid ConcurrentModificationException
			}
		}

		///////////////////////////////////////////////////////////////////////
		// Ship collision check and entity updates
		int collided = 0;
		
		li = enemy_lasers.iterator();
		while(li.hasNext())	{
		    Laser l = li.next();
		    if(l.move((delta)) == DONE) 
		    	li.remove();		
			
			// Check ship collision with fire.
			// Order important here because ship can be invincible
			if(ship.collides(l) && ship.getState() != Ship.ShipState.INVINCIBLE ) {
				l.destroy();
				li.remove();
				collided++;	// Bang! You're dead.
			}
		}
		
		ei = enemies.iterator();
		while(ei.hasNext()) {
			Enemy e = ei.next();
			
			if(e instanceof TrackingEnemy) {									// Enemy type specific code
				if(state == PlayState.GAME_OVER) {
					((TrackingEnemy) e).setShipPos(new Vector3f(Globals.FIELD_WIDTH / 2, 0, Globals.FIELD_HEIGHT)); // Send off the bottom
				} else {
					((TrackingEnemy) e).setShipPos(ship.getPosition());
				}
			} else if(e instanceof BlasterEnemy) {
				((BlasterEnemy) e).fireLaser(enemy_lasers);
				((BlasterEnemy) e).fireLaser(ship_lasers);
			}
			e.update(delta);													// General AI updates
			
			if(ship.collides(e) && ship.getState() != Ship.ShipState.INVINCIBLE ) {
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
			ship.setDirection(Utils.cloneVec3(Entity.NONE));					// Workaround for ship continuing in direction on death

			ship.destroy();														// Call destroy on ship. Note that this only triggers the destruction animation.
			data.resetMultiplier();
			
			// Death explosion! (orange)
			ship_lasers.addAll(Utils.makeExplosion(
					ship.getPosition(), 
					new Vector3f(1.0f, 0.5f, 0f),
					128
				));
		}
		
		
		
		
		///////////////////////////////////////////////////////////////////////
		// Check Win/Loose conditions
		long elapsed = (System.currentTimeMillis() - data.getStartTime());
		if(elapsed >= duration) {												// Are we Bi-Winning?
			if(!game_won && !game_over) {

				ship_lasers.addAll(Utils.makeExplosion(
						ship.getPosition(), 
						new Vector3f(1.0f, 0.1f, 0.1f),
						128
					));
				
				stopSpawners();
				data.setStartTime(0);
				data.setGameWon(true);
				engine.pushState(new GameOverState(data).init(engine));			// GameOver state also handles winning
				game_won = true;
				state = PlayState.GAME_OVER;
				emptyLists();
			}
			
			return DONE;
		}
		
		
		if(data.getLives() < 0) {
			if(!game_over) {													// Game Over, man! Game over!

				ship_lasers.addAll(Utils.makeExplosion(
						ship.getPosition(), 
						new Vector3f(1.0f, 0.1f, 0.1f),
						128
					));
				
				stopSpawners();
				data.setStartTime(0);
				data.setGameWon(false);
				engine.pushState(new GameOverState(data).init(engine));			// Create the "Game Over" state
				game_over = true;												//  and push it onto the stack - but only once!
				emptyLists();
			}
			
			return DONE; 														// Return ship destroyed if last life (0) used
		}
		

		// Stuff after this line is not evaluated when game is won/lost
		
		
		///////////////////////////////////////////////////////////////////////
		// Keypress evaluation
		Vector3f direction = new Vector3f(0,0,0);
		
		if (keydown_left && !keydown_right) {									// Translate key presses into movement
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
		
		
		///////////////////////////////////////////////////////////////////////
		// Ship Updates
		if( ship.update(delta) == DONE ) {										// Ship Updates: DONE if animation complete (respawn or game over)
			data.decrementLives();												// Decrement lives
			if(data.getLives() >= 0) {
				ship = new Ship();												// Respawn ship
			} else {
				state = PlayState.GAME_OVER;
			}
				
			return SUCCESS;														// Don't do anything else this tick
		}

		// Move the ship at correct (calculated) speed
		ship.move(delta);


			
		///////////////////////////////////////////////////////////////////////
		// Firing
		if (fire) {
			ship.fireLaser(ship_lasers);		// Ship lasers
		}
		

		if(bomb && !bombed) {
			if(data.getBombs() > 0) {
				ship_lasers.addAll(
					Utils.makeExplosion(
							ship.getPosition(), 
							new Vector3f(0.5f, 0.5f, 1.0f),
							128
						));
				data.decBombs();
			}
			bombed = true;					// Latch function
		}

		return SUCCESS;
	}

	
	private void emptyLists() {

		Iterator<Enemy> ei = enemies.iterator();								// Remove all enemies
		while(ei.hasNext()) {
			Enemy e = ei.next();
			
			destroyed_enemies.add(e);											// Add to destruction animation stack
			e.destroy();
			ei.remove();
			data.addScore(e.getScore());										// Enemies left are bonus score fodder
		}
		
		Iterator<Powerup> pi = powerups.iterator();								// Stop stray powerups floating around after game-over
		while (pi.hasNext()) {
			Powerup p = pi.next();
			p.destroy();
			pi.remove();
		}
	}
	
	
	/**************************************************************************
	 * Render the game scene
	 */
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
				
				for(Powerup p : powerups)
					p.render();
				
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
					glVertex3f(
							(ship.getPosition().x + ship.getModel().getWidth() / 2) * Globals.coord_ratio_x, 
							(-ship.getPosition().z + ship.getModel().getHeight() / 2) * Globals.coord_ratio_y, 
							0.0f 
						);
					glVertex3f(
							mouse_x, 
							(-ship.getPosition().z + ship.getModel().getHeight() / 2) * Globals.coord_ratio_y, 
							0.0f 
						);
					glVertex3f(mouse_x, mouse_y, 0.0f );
				glEnd();
			    
				glPopMatrix();
				
				glMatrixMode(GL_PROJECTION);
			glPopMatrix();
			glPopAttrib();
		}
		
		return SUCCESS;
	}



	/**************************************************************************
	 * Perform mouse input handling
	 */
	@Override
	public void mouseInput(int mouse_x, int mouse_y, boolean leftDown, boolean rightDown) {
		
		this.mouse_x = mouse_x;
		this.mouse_y = mouse_y;
		
		// Convert mouse coordinates to 2D world coordinates
		Vector2f world_mouse = new Vector2f(
				mouse_x / Globals.coord_ratio_x,
				mouse_y / Globals.coord_ratio_y
			);
		
		// Due to silly implementation, y is z for world coords.
		Vector2f ship_pos = new Vector2f(
				ship.getPosition().x + ship.getModel().getWidth() / 2,
				ship.getPosition().z - ship.getModel().getHeight() / 2			
			);
		
		Vector2f orientation = Utils.getOrientationToPoint2d(world_mouse, ship_pos);
		
		// Get the unit vector of the angle and set it on the shipda
		ship.setOrientation(
			new Vector3f( 
				orientation.x,
				0.0f, 
				orientation.y
			));
		
//		System.out.println(
//			  "_MOUSE   x:" + mouse_x + " y:" + mouse_y +
//			"\n_WORLD   x:" + world_x + " y:" + world_y +
//			"\n_SHIP    x:"+ ship.getPosition().x + " y:" + ship.getPosition().z +
//			"\n_LENGTHS o:" + (int) opposite + " a:" + (int) adjacent + " h:" + (int) hypotenuse +
//			"\n_ANGLE:  " + angle * RAD + "\n"
//		);
		
		
		if(leftDown)
			fire=true;
		else
			fire=false;
		
		if(rightDown) {
			bomb=true;
		} else {
			bomb=false;
			bombed=false;
		}
	}

	
	
	@Override
	public void keyInput(int key) {

		if(state == PlayState.GAME_OVER)
			return;
		
		camera.keyInput(key);
		
		if (Keyboard.getEventKeyState()) {

			switch (key) {														// Movement key bindings
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
			
			// DEBUG key bindings
//			case Keyboard.KEY_F3:
//				Hitbox.render = !Hitbox.render;									// Toggle hitbox rendering on/off
//				break;
			case Keyboard.KEY_F4:												// Fall through to Pause
			case Keyboard.KEY_P:
				
				if(paused) {
					this.resume();
					data.resume();
				} else {
					this.pause();												// Toggle pause for the bottom state
					data.pause();
				}
				break;
			case Keyboard.KEY_ESCAPE:											// Return to menu state
				data.reset();
				engine.changeState(new MenuState(data).init(engine));
				break;
			default:
				break;
			}

		} else {																// Key-up handling

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
			default:
				break;
			}
		}
	}
	

	
	@Override
	public void pause() {
		paused = !paused;
		stopSpawners();
		speedcont.pause();
		mp3.pause();
	}

	

	@Override
	public void resume() {
		paused = false;
		enemySpawner.start(new EnemySpawner());
		powerupSpawner.start(new PowerupSpawner());
		
		speedcont.resume();
		mp3.resume();
	}



	@Override
	public void cleanup() {
		stopSpawners();
		
		mp3.stop();
		speedcont.stop();
	}
	
	private void stopSpawners() {
		enemySpawner.stop();
		powerupSpawner.stop();
	}
}
