package csc3202.Engine.Interfaces;

import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Hitbox;
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
public abstract class Entity {
	
	/**
	 * Some static direction definitions
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
	 * The direction to move is managed by the entity implementation 
	 * @return SUCCESS if OK, DONE if object has been destroyed
	 */
	public abstract int move(float distance);
	
	
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


	public Hitbox getHitbox() {
		return hit;
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
	
	
	@Override
	public String toString() {
		return "Entity: " + super.toString()
				+ "\n\tPosition:   \t" + position
				+ "\n\tOrientation:\t" + orientation
				+ "\n\tDirection:  \t" + direction;
	}
}
