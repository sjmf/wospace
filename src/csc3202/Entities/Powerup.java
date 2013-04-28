/**
 * 
 */
package csc3202.Entities;

import static csc3202.Engine.Globals.*;
import static org.lwjgl.opengl.GL11.*;
import csc3202.Engine.Globals;
import csc3202.Engine.Utils;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Engine.OBJLoader.OBJManager;

/**
 * @author sam
 *
 */
public class Powerup extends Entity {

	public enum PType {
		BOMB,
		FIRERATE
	}
	
	private PType type;
	
	/**
	 * Make a power-up
	 */
	public Powerup(PType type) {
		super();
		
		this.type = type;
		
		this.setModel(
				OBJManager.getManager().getModel(
					(type == PType.BOMB) 
						? Globals.BOMB_MODEL_PATH
						: Globals.POWERUP_MODEL_PATH
			));

		this.setPosition(Utils.cloneVec3(Entity.ZERO));
		this.setDirection(Utils.cloneVec3(Entity.ZERO));
	}


	public PType getType() {
		return type;
	}


	public void setType(PType type) {
		this.type = type;
	}

	
	@Override
	public int update(long delta) {
		return SUCCESS;
	}
	

	@Override
	public int render() {
		
		glPushMatrix();
			glTranslatef(this.getPosition().x, this.getPosition().y, this.getPosition().z);
			getModel().render();
		glPopMatrix();
		
		if(getHitbox() != null) {
			glPushMatrix();		
				getHitbox().render();
			glPopMatrix();
		}
		
		return SUCCESS;
	}
	

	/**
	 * @see csc3202.Engine.Interfaces.Collidable#destroy()
	 */
	@Override
	public int destroy() {
		return SUCCESS;
	}
}
