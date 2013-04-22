
package csc3202.Engine.Interfaces;

import csc3202.Engine.Hitbox;

/**
 * Defines an object which is collidable.
 * 
 * Collidable objects must have (and maintain position of) a hitbox.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public interface Collidable {
	
	/** 
	 * Return a hitbox describing the location of the Collidable object
	 */
	Hitbox getHitbox();
	

	/** 
	 * Check if this collidable collides with another.
	 * Uses the static utility method from the Hitbox class.
	 */
	boolean collides(Collidable c);
	
	
	/**
	 * Indicates that this object is to be destroyed and
	 * should run its destruction animation
	 * 
	 * @return SUCCESS if running destruct, DONE if destroyed/gone
	 */
	int destroy();
}
