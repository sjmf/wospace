package csc3202.Entities;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import csc3202.Engine.*;
import csc3202.Engine.Interfaces.*;
import csc3202.Engine.OBJLoader.OBJManager;
import csc3202.Engine.OBJLoader.OBJModel;

/**
 * Class which implements a laser (fired from spaceship)
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public class Laser extends Entity {
	
	private Vector4f colour = new Vector4f(1.0f,1.0f,1.0f,0.8f);
	
	private OBJModel model;
	
	/**
	 * Constructor taking initial parameter data
	 */
	public Laser(Vector3f position) {
		
		super();
		model = OBJManager.getManager().getModel(Globals.LASER_MODEL_PATH);
		this.setHitbox(Hitbox.copy(model.getDefaultHitbox()));
		
		this.setPosition(position);
	}
	
	
	
	/**
	 * Render a laser
	 * 
	 * Possible improvement: make laser a light source
	 */
	public int render() {
		
		glPushAttrib(GL_CURRENT_BIT | GL_LIGHTING_BIT );
		
		glDisable(GL_LIGHTING);
		glColor4f(colour.x, colour.y, colour.z, colour.w);
		
		glPushMatrix();
			glTranslatef(this.getPosition().x, this.getPosition().y, this.getPosition().z);
			glRotatef((float) Math.atan2(this.getOrientation().x, this.getOrientation().z) * RAD, 0, 1, 0);		// Rotate to face mouse
			model.render();
		glPopMatrix();
		
		glPopAttrib();
		
		getHitbox().render();
		
		return SUCCESS;
	}


	@Override
	public int update(long delta) {
		return SUCCESS;
	}



	@Override
	public int destroy() {
		return SUCCESS;
	}

	
	
	@Override
	public int move(long delta) {
		
		this.setPosition(
				Vector3f.add(this.getPosition(), (Vector3f) (new Vector3f(this.getDirection()).scale(delta)), null));

		if(! Hitbox.checkCollision(this.getHitbox(), Globals.FIELD_HITBOX)) {	// If inside hitbox for field: Check where the laser is going

			// Calculate end point of ray
			Vector2f start = new Vector2f(this.getPosition().x, this.getPosition().z);
			Vector2f scaleDir = (Vector2f) new Vector2f(this.getDirection().x / LASER_SPEED, this.getDirection().z / LASER_SPEED).scale(FIELD_HEIGHT);
			Vector2f end = Vector2f.add(Utils.cloneVec2(start), scaleDir, null);
			
			if (! Hitbox.checkIntersection(FIELD_HITBOX, start, end) ) {
				return DONE;
			}
		}
		
		return SUCCESS;
	}



	public void colour(float r, float g, float b) {
		
		this.colour.x = r;
		this.colour.y = g;
		this.colour.z = b;
	}

}
