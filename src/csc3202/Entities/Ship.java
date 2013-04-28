
package csc3202.Entities;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import csc3202.Engine.Globals;
import csc3202.Engine.Hitbox;
import csc3202.Engine.Utils;
import csc3202.Engine.Interfaces.Collidable;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Engine.OBJLoader.OBJManager;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

/**
 * The player Ship/tank/turret/plane/whatever
 * 
 * Point is, it shoots lasers.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Ship extends Entity {

	public enum ShipState {
		OK,
		DESTROYED,
		INVINCIBLE
	}

	private static final Vector3f SHIP_START = 
			new Vector3f(FIELD_WIDTH / 2, 0.0f, -FIELD_HEIGHT / 2);
	
	// initially invulnerable to prevent death in first second or so
	private ShipState state = ShipState.INVINCIBLE;

	// Durations
	private long spawn_time;							// For invincible period after spawn
	private long last_fired;
	private int fire_rate = Globals.SHIP_FIRE_RATE;		// Powerups can increase rate of fire
	
	private long powerup_time = 0l;						// Timer for powerup duration
	
	private static final float GUN_OFFSET = 8f;			// The offset of the guns on the model
	
	private float theta;
	
	
	/**
	 * Default Constructor - not much to do here
	 */
	public Ship() {
		
		super();
		this.setModel(OBJManager.getManager().getModel(SHIP_MODEL_PATH));
		this.getHitbox().setScale(0.5f);
		this.setOrientation(NONE);
		this.setPosition(SHIP_START);
		
		// Initialise firing duration with correct time
		spawn_time = System.currentTimeMillis();
		last_fired = spawn_time;
	}
	
	
	
	/**
	 * Fire the ship's laser (if the recharge time has elapsed)
	 * @param lasers
	 */
	public void fireLaser(ArrayList<Laser> lasers) {
		
		if(state == ShipState.DESTROYED)
			return;	// Do not fire lasers if the ship is destroyed.
		
		long time = System.currentTimeMillis();
		
		if(time > last_fired + (int)(fire_rate)) {
			
			// TWO (yes TWO!) laser stacks for additional pew pew
			Laser l = null;
			Laser r = null;
			
			// Offset left and right lasers around the center of rotation
			float px_l = this.getPosition().x + GUN_OFFSET;
			float px_r = this.getPosition().x - GUN_OFFSET;
			
			float x_l = (float) Math.cos(theta) * (px_l-getPosition().x) + getPosition().x;
			float y_l = (float) Math.sin(theta) * (px_l-getPosition().x) + getPosition().z;
			
			float x_r = (float) Math.cos(theta) * (px_r-getPosition().x) + getPosition().x;
			float y_r = (float) Math.sin(theta) * (px_r-getPosition().x) + getPosition().z;
			
			l = new Laser(new Vector3f(x_l, 0, y_r));
			r = new Laser(new Vector3f(x_r, 0, y_l));
			
			l.colour(255.0f, 0.0f, 0.0f);
			l.setDirection((Vector3f) Utils.invertVec3(this.getOrientation()).scale(LASER_SPEED));
			l.setOrientation(Utils.invertVec3(this.getOrientation()));
			lasers.add( l );
			
			r.colour(255.0f, 0.0f, 0.0f);
			r.setDirection((Vector3f) Utils.invertVec3(this.getOrientation()).scale(LASER_SPEED));
			r.setOrientation(Utils.invertVec3(this.getOrientation()));
			lasers.add( r );
			
			last_fired = time;
		}
	}



	/**
	 * Get the State of the ship
	 * @return enum ShipState- one of OK, INVINCIBLE, or DESTROYED
	 */
	public ShipState getState() {
		return state;
	}
	
	
	
	/**
	 * Set the state of the ship
	 * The ship class handles state internally. Use this method
	 * to override its decision artificially.
	 * @param state
	 */
	public void setState(ShipState state) {
		this.state = state;
	}



	/**
	 * @return SUCCESS if success, 
	 * 			WAIT if destroyed and animating, 
	 * 			DONE if animation complete (respawn or game over)
	 */
	@Override
	public int update(long delta) {
		
		theta = (float) Math.atan2(this.getOrientation().z, this.getOrientation().x);
	
		if(System.currentTimeMillis() > powerup_time + POWERUP_DURATION) {		// Reset powerup after powerup time expires
			fire_rate = SHIP_FIRE_RATE;
		}
		
		if(state == ShipState.INVINCIBLE) {
			long time = System.currentTimeMillis();
			
			if(time > (spawn_time + INVINCIBILITY_DURATION)) {
				state = ShipState.OK;
			}
		}
		else if(state == ShipState.DESTROYED) {
			
			return DONE;	// Destroyed animation complete
		}
		
		return SUCCESS;
	}
	
	

	@Override
	public int move(long delta) {

//		System.out.println("\nPosition:\t" + this.getPosition() + "\n\tOrientation:\t" + this.getOrientation() + "\n\t\tDirection:\t" + this.getDirection());
		
		if(state == ShipState.DESTROYED) 
			return DONE;
		
		// calculate new movement using vector- if in bounds, apply it
		Vector3f oldPos = this.getPosition();
		this.setPosition(
				Vector3f.add(
						this.getPosition(), 
						(Vector3f) this.getDirection().scale(SHIP_SPEED * delta), 
						null
					));
		
		// Check new collision- do not apply if out of bounds
		if(! Hitbox.checkCollision(this.getHitbox(), FIELD_HITBOX)) {
			this.setPosition(oldPos);
			return DONE;
		}
		
		return SUCCESS;
	}
	
	

	@Override
	public int render() {
		
		glPushAttrib(GL_POLYGON_BIT);
		glColor4f( 1f, 1f, 1f, 1.0f );
		
		if(state == ShipState.INVINCIBLE) {
            long time = System.currentTimeMillis();

          if(time %INVINCIBILITY_FLASH_DURATION < INVINCIBILITY_FLASH_DURATION / 2) {

        	  glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);	// Debug
          }
		}
		
		glPushMatrix();
			glTranslatef(this.getPosition().x, this.getPosition().y, this.getPosition().z);
			glRotatef(180, 0, 1, 0);				// Model is facing outwards, not towards Z+
			glRotatef(theta * RAD, 0, 1, 0);		// Rotate to face mouse
			this.getModel().render();
		glPopMatrix();
		
		if(getHitbox() != null)
			getHitbox().render();
		
		glPopAttrib();
		
		return SUCCESS;
	}



	@Override
	public int destroy() {
		state = ShipState.DESTROYED;
		this.fire_rate = Globals.SHIP_FIRE_RATE;
		return SUCCESS;
	}
	

	/**
	 * @return the fire_rate
	 */
	public int getFireRate() {
		return fire_rate;
	}
	
	
	/**
	 * Power-up get!
	 */
	public void powerUp() {
		powerup_time = System.currentTimeMillis();
		if(this.fire_rate > Globals.SHIP_MAX_FIRE_RATE)
			this.fire_rate -= Globals.POWERUP_FIRE_RATE_INC;
		
		System.out.println(this.fire_rate);
	}



	@Override
	public boolean collides(Collidable c) {
		
		if(state == ShipState.OK) {
			return Hitbox.checkCollision(getHitbox(), c.getHitbox());
		} else {
			return false;	// Invincible or dead- either way we don't need to do a collision check
		}
	}
}
