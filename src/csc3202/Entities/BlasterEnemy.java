package csc3202.Entities;

import static csc3202.Engine.Globals.*;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Utils;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Engine.OBJLoader.OBJManager;

/**
 * An enemy which moves towards the middle of the screen, then 
 * anihilates everything within sight
 * 
 * @author sam
 */
public class BlasterEnemy extends Enemy {

	private static final int TOLERANCE = 10;	// px tolerance of center of screen
	
	private int firings = 2;
	private boolean reoriented = false;
	
	private boolean set = false;
	private boolean done = false;
	
	public BlasterEnemy() {
		
		this.setModel(OBJManager.getManager().getModel(BLASTER_MODEL_PATH));
		
		this.setPosition(Utils.cloneVec3(Entity.ZERO));
		this.setDirection(Utils.cloneVec3(Entity.ZERO));
		super.setOrientation(Utils.cloneVec3(Entity.ZERO));
		
		this.setColour(null);
		
		this.setHealth(5);
	}
	
	
	@Override
	public int update(long delta) {
		
		if(done)
			return DONE;
		
		if(! reoriented) {
			Vector2f orient = Utils.getOrientationToPoint2d(
					new Vector2f(FIELD_WIDTH / 2, FIELD_HEIGHT / 2),
					new Vector2f(this.getPosition().x, this.getPosition().z)
				);
			
			super.setOrientation(new Vector3f(orient.x, 0, orient.y));
			
			super.setDirection((Vector3f) 
					Utils.invertVec3(this.getOrientation()).scale(0.75f* ENEMY_SPEED)
				);
			
			reoriented = true; // Don't enter this block again
		}
		
		// Are we in the middle of the screen yet?
		if(this.getPosition().x > FIELD_WIDTH /2 - TOLERANCE
			&& this.getPosition().x < FIELD_WIDTH /2 + TOLERANCE
			&& this.getPosition().z < -(FIELD_HEIGHT /2 - TOLERANCE)
			&& this.getPosition().z > -(FIELD_HEIGHT /2 + TOLERANCE) ) {
			
			// Blast after 3 seconds
			if(!set) {
				// Stop
				this.setDirection(Utils.cloneVec3(Entity.NONE));
				
				set = true;
			} 
		}
		
		return super.update(delta);
	}
	
	/**
	 * Ooh, you are in for a whole world of pain if this goes off
	 */
	@Override
	public void fireLaser(ArrayList<Laser> lasers) {
		
		if (set) {
			
			lasers.addAll(Utils.makeExplosion(
					this.getPosition(), 
					new Vector3f(1.0f, 0.2f, 1.0f),
					32
				));
			
			if(--firings < 0)
				done = true;
		}
	}
}
