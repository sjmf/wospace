package csc3202.Entities;

import static csc3202.Engine.Globals.*;

import static org.lwjgl.opengl.GL11.*;
import csc3202.Engine.*;
import csc3202.Engine.Interfaces.*;
import csc3202.Engine.OBJLoader.OBJManager;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * An enemy which maintains its state in the game world
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Enemy extends Entity implements Collidable {
	
	/* Colour in rgb */
	private float a=1.0f;
	
	private boolean destroyed = false;
	
	float rot = 0.0f;											// Rotation angle for destruction animation
	float rot_z = (Engine.rand.nextFloat() * 2) - 1.0f;			// z vector (rotation speed)
	
	private int score=0;
	
	private int health=1;
	
	/**
	 * Default constructor
	 */
	public Enemy() {
		
		super();
		this.setModel(OBJManager.getManager().getModel(Globals.ENEMY_MODEL_PATH));
		
		this.setPosition(cloneVec3(Entity.ZERO));
		this.setDirection(cloneVec3(Entity.ZERO));
	}
	
	
	
	/**
	 * Fire the laser. Pew pew!
	 * 
	 * We don't need to care about a recharge time here as
	 * the fire rate is controlled by the calling class
	 * 
	 * @param lasers
	 */
	public void fireLaser(ArrayList<Laser> lasers) {
		
		Laser l = new Laser(Vector3f.add(getPosition(), new Vector3f(0,0,getModel().getDepth() / 2), null));
		l.colour(0.5f, 0.5f, 1.0f);
		l.setDirection((Vector3f) cloneVec3(Entity.DOWN).scale(ENEMY_LASER_SPEED));
		lasers.add( l );
	}
	
	

	@Override
	public int render() {
		
		glPushMatrix();
			glColor4f(1f,1f,1f,a);
			glTranslatef(this.getPosition().x, this.getPosition().y, this.getPosition().z);
			glRotatef(rot, 1, 1, 1);
			getModel().render();
		glPopMatrix();
		
		if(getHitbox() != null) {
			glPushMatrix();		
					getHitbox().render();
			glPopMatrix();
		}
		
		return SUCCESS;
	}



	@Override
	public int update(long delta) {
		rot += this.getTumble() * Globals.game_speed * ROTATE_SPEED * delta;

		if(destroyed) {
			a -= ALPHA_FADE * 2 * delta;
			this.getPosition().y -= FALL_SPEED * delta;

			if(a <= 0.0f) 	// If we've faded out completely, let the calling class know 
				return DONE;	// this enemy is to be removed from any holding data structure
		}


		return SUCCESS;
	}



	/**
	 * @return SUCCESS if success, DONE if enemy has gone offscreen and must be removed.
	 */
	@Override
	public int move(float delta) {
		
		// calculate new movement using vector- if in bounds, apply it
		Vector3f position = Vector3f.add(this.getPosition(),
				(Vector3f) cloneVec3(this.getDirection()).scale(delta * Globals.game_speed), null);

		if(! Hitbox.checkCollision(this.getHitbox(), Globals.FIELD_HITBOX)) {
			// Check if enemy movement vector is going towards play area

			// Calculate end point of ray
			Vector2f start = new Vector2f(this.getPosition().x,
					this.getPosition().z);
			Vector2f scaleDir = (Vector2f) new Vector2f(this.getDirection().x
					/ ENEMY_SPEED, this.getDirection().z / ENEMY_SPEED)
					.scale(FIELD_HEIGHT);
			Vector2f end = Vector2f.add(cloneVec2(start), scaleDir, null);

			if (! Hitbox.checkIntersection(FIELD_HITBOX, start, end) ) {
				return DONE;		// if not, return DONE and Destroy this enemy 
			}
		}

		this.setPosition(position);
		return SUCCESS;
	}
	

	@Override
	public int destroy() {
		
		destroyed = true;
		this.setHitbox(null);
		return SUCCESS;
	}
	

	@Override
	public boolean collides(Collidable c) {
		if (destroyed) return false;
		return Hitbox.checkCollision(getHitbox(), c.getHitbox());
	}
	
	
	
	public boolean damage() {
		return (--health < 0);
	}



	public int getScore() {
		return score;
	}



	public void setScore(int score) {
		this.score = score;
	}



	public int getHealth() {
		return health;
	}



	public void setHealth(int health) {
		this.health = health;
	}
	
}
