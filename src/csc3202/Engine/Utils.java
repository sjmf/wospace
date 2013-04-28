
package csc3202.Engine;

import static csc3202.Engine.Globals.*;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Interfaces.Entity;
import csc3202.Entities.Laser;

/**
 * A class for handy miscellaneous static functions which get re-used a lot
 * @author Sam
 */
public class Utils {

	/** Don't instantiate, silly. */
	private Utils() { }
	
	
	/**
	 * Calculate the new orientation of a to face b using simple trigonometry
	 * @param a
	 * @param b
	 * @return
	 */
	public static final Vector2f getOrientationToPoint2d(Vector2f a, Vector2f b) {
		
		double adjacent = a.x - b.x ;						// Width of triangle
		double opposite = a.y + b.y ;						// Height of triangle
		
		// TODO: consider how to do this without sqrt!
		double hypotenuse = sqrt( pow(adjacent,2) + pow(opposite,2) );			// Pythagoras finds hyp
		
		double angle = asin(opposite / hypotenuse);
		
		// Correct angle for quadrants in radians 
		// - faster to use comparisons on adj&opp than tan/cos theta
		if(adjacent < 0) {
			if(opposite < 0)					// 3rd quadrant (adj -ve, opp -ve)
				angle = PI - angle;				// -ve angle
			 else								// 2nd quadrant (adj -ve. opp +ve)
				angle = PI - angle;
			
		} else if(opposite < 0) { 				// 4th quadrant (adj +ve, opp -ve)
				angle = 2*PI + angle;
		}										// else 1st quadrant (+ve +ve) no action
		
		return new Vector2f(
				(float) cos(angle - PI/2),
				(float) sin(angle - PI/2)
			);
		
	}
	
	
	/**
	 * Make a death explosion! (abstracted from RunState)
	 * @param origin
	 * @param colour
	 * @return
	 */
	public static final ArrayList<Laser> makeExplosion(Vector3f origin,
														Vector3f colour ) {
		
		ArrayList<Laser> explosion = new ArrayList<Laser>();
		
		float inc = (float) ((2 * Math.PI) / 64);								// Spawn 64 lasers (2^6)
		for(int i=0; i<64; i++) {
			float rad = i * inc; 												// Calculate rotation in radians
			
			Vector3f dir = new Vector3f(
					(float) cos(rad - PI/2), 
					0.0f, 
					(float) sin(rad - PI/2)
				);
			
			Laser l = new Laser(origin);										// New Laser with position at origin
			l.setDirection((Vector3f) Utils.cloneVec3(dir).scale(LASER_SPEED));	// direction
			l.setOrientation(dir);												// and orientation
			l.colour(colour.x, colour.y, colour.z);
			
			explosion.add(l);
		}
		
		return explosion;
	}
	

	/** 
	 * Check if a movement vector is going towards play field
	 * @param e
	 * @return
	 */
	public static final boolean movingTowardsField(Entity e) {

		// Cast a ray and see if it intersects the field hitbox
		Vector2f start = new Vector2f(e.getPosition().x, e.getPosition().z);
		
		Vector2f scaleDir = (Vector2f) 
			new Vector2f(
				e.getDirection().x / ENEMY_SPEED, 
				e.getDirection().z / ENEMY_SPEED
			).scale(FIELD_HEIGHT);
		
		Vector2f end = Vector2f.add(Utils.cloneVec2(start), scaleDir, null);

		if (! Hitbox.checkIntersection(FIELD_HITBOX, start, end) ) {
			return false;	// if not moving towards field, return false
		}
		
		return true;
	}
	

	/**
	 * Deep-copy a Vector3f
	 * @param vector
	 * @return
	 */
	public static final Vector3f cloneVec3(Vector3f vector) {
		return new Vector3f(vector.x, vector.y, vector.z);
	}
	

	/**
	 * Deep-copy a Vector2f
	 * @param vector
	 * @return
	 */
	public static final Vector2f cloneVec2(Vector2f vector) {
		return new Vector2f(vector.x, vector.y);
	}
	

	/**
	 * Invert the x/z coordinates of a vec3
	 * @param vector
	 * @return
	 */
	public static final Vector3f invertVec3(Vector3f vector) {
		return new Vector3f(-vector.z, -vector.y, -vector.x);
	}
	

	/**
	 * Normalise a float relative to a max and min value
	 * @param e
	 * @param e_min
	 * @param e_max
	 * @return
	 */
	public static final float normalise(float e, float e_min, float e_max) {
		return (e - e_min) / (e_max - e_min);
	}
}
