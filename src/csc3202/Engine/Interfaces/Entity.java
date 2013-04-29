package csc3202.Engine.Interfaces;

import static csc3202.Engine.Globals.DONE;
import static csc3202.Engine.Globals.SUCCESS;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import csc3202.Engine.Globals;
import csc3202.Engine.Hitbox;
import csc3202.Engine.Utils;
import csc3202.Engine.OBJLoader.OBJModel;

/**
 * A game entity which has a position in the world must
 * implement this interface.
 * 
 * Methods which return type 'int' are expected to implement some
 * form of reporting to the calling class. Generally speaking, the
 * specifics of this will be left to the implementing class to 
 * determine, but it is expected that a "0" return means everything 
 * went fine. 
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Apr 13
 */
public abstract class Entity implements Collidable {
	
	/**
	 * Some static direction definitions for convenience
	 */
	public static final Vector3f NONE = new Vector3f(0,0,0);
	public static final Vector3f ZERO = NONE;
	
	public static final Vector3f LEFT = new Vector3f(-1,0,0);
	public static final Vector3f RIGHT = new Vector3f(1,0,0);
	
	public static final Vector3f UP = new Vector3f(0,0,-1);
	public static final Vector3f DOWN = new Vector3f(0,0,1);
	
	public static final Vector3f IN = new Vector3f(0,1,0); 
	public static final Vector3f OUT = new Vector3f(0,-1,0);

	
	/** Model Loaded from file **/
	private OBJModel model;
	
	
	/**
	 * The direction in which an object (Entity, etc) is moving across the screen
	 * -also affects speed.
	 */
	private Vector3f direction;
	
	/*
	 * The position on the screen
	 */
	private Vector3f position;
	
	/*
	 * The orientation on the screen
	 */
	private Vector3f orientation;
	
	
	/**
	 * Optional vector for setting RGBA colour.
	 * Rendering left up to implementation
	 */
	protected Vector4f colour;
	
	/**
	 * OPTIONAL: The rotation in degrees per second for a tumbling entity
	 */
	private float tumble = 0f;

	
	/** A Hitbox with coordinates and vertices in 3space **/
	private Hitbox hit;
	
	/** 
	 * Set up Entity - position, etc. using init() method.
	 * Sometimes called from the constructor
	 */
	public Entity() {
		
		direction = new Vector3f();
		position = new Vector3f();
		orientation = new Vector3f();
		colour = new Vector4f(1f,1f,1f,1f);	// White
	}
	
	
	/**
	 * Update any state which the entity maintains for itself
	 * Animations are a perfect example.
	 * 
	 * @return SUCCESS if OK, DONE if this entity is destroyed
	 */
	public abstract int update(long delta);
	
	
	/** 
	 * Perform OpenGL render process for Entity verticies
	 */
	public abstract int render();
	
	
	/**
	 * Move Entity in its set direction by distance.
	 * This default implementation is provided but should be overriden if more
	 *  complex movement code is required.
	 *  
	 * @return SUCCESS if OK, DONE if object has been destroyed
	 */
	public int move(long delta) {

		// calculate new position using vector
		Vector3f position = Vector3f.add(
				this.getPosition(),
				(Vector3f) Utils.cloneVec3(this.getDirection())
					.scale(delta * Globals.game_speed), 
				null
			);

		// Check new position is within field bounds (or if it is moving towards the field)
		if((! Hitbox.checkCollision(this.getHitbox(), Globals.FIELD_HITBOX))
			&& (! Utils.movingTowardsField(this))) {
				return DONE;			// Moving away from field, destroy.
		}
	
		this.setPosition(position);		// Else apply the updated position
		return SUCCESS;
	}
	
	
	
	/**
	 * Set the position of the Entity in the game world
	 */
	public void setPosition(Vector3f position) {
		this.position = position;
		
		if(this.hit != null)
			this.hit.setPosition(position);
	}
	
	
	public Vector3f getPosition() {
		return position;
	}
	

	/*
	 * Set the direction of the Entity in the game world
	 */
	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}
	
	
	public Vector3f getDirection() {
		return direction;
	}


	/**
	 * Set the orientation of the Entity in the game world
	 */
	public void setOrientation(Vector3f orientation) {
		this.orientation = orientation;
	}
	
	
	public Vector3f getOrientation() {
		return orientation;
	}


	/** 
	 * Set the hitbox used for collision of this Entity
	 * @param hit
	 */
	public void setHitbox(Hitbox hit) {
		this.hit = hit;
	}

	@Override
	public Hitbox getHitbox() {
		return hit;
	}
	
	@Override
	public boolean collides(Collidable c) {
		return Hitbox.checkCollision(getHitbox(), c.getHitbox());
	}


	/**
	 * Set the model to render for this entity
	 * sets the hitbox as a side-effect
	 * @param model
	 */
	public void setModel(OBJModel model) {
		this.model = model;
		this.hit = Hitbox.copy(model.getDefaultHitbox());
	}


	public OBJModel getModel() {
		return model;
	}


	public float getTumble() {
		return tumble;
	}


	public void setTumble(float tumble) {
		this.tumble = tumble;
	}


	public Vector4f getColour() {
		return colour;
	}


	public void setColour(Vector4f colour) {
		this.colour = colour;
	}
	
	
	@Override
	public String toString() {
		return "Entity: " + super.toString()
				+ "\n\tPosition:   \t" + position
				+ "\n\tOrientation:\t" + orientation
				+ "\n\tDirection:  \t" + direction;
	}
}
