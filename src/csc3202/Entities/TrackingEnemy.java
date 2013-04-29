/**
 * 
 */
package csc3202.Entities;

import static csc3202.Engine.Globals.*;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import csc3202.Engine.Globals;
import csc3202.Engine.Utils;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Engine.OBJLoader.OBJManager;

/**
 * An enemy which tracks and tries to kill the player
 * @author sam
 *
 */
public class TrackingEnemy extends Enemy {

	private Vector3f shipPos;
	
	/** construct an enemy which tracks the ship */
	public TrackingEnemy() {
		
		this.setModel(OBJManager.getManager().getModel(Globals.TRACKER_MODEL_PATH));
		
		this.setPosition(Utils.cloneVec3(Entity.ZERO));
		this.setDirection(Utils.cloneVec3(Entity.ZERO));
		this.setOrientation(Utils.cloneVec3(Entity.ZERO));
		
		this.setColour(new Vector4f(0.9f,0.85f,0f,1f));	// Color yellow
	}

	public void setShipPos(Vector3f pos) {
		this.shipPos = pos;
	}
	
	@Override
	public int update(long delta) {
		
		Vector2f orient = Utils.getOrientationToPoint2d(
				new Vector2f(this.shipPos.x, -this.shipPos.z),
				new Vector2f(this.getPosition().x, this.getPosition().z)
			);
		
		this.setDirection((Vector3f) 
				Utils.invertVec3(
						new Vector3f(orient.x, 0, orient.y)
					).scale(ENEMY_SPEED));
			
		return super.update(delta);
	}
}
