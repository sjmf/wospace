/**
 * 
 */
package csc3202.Engine.AI;

import static csc3202.Engine.Globals.*;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Globals;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Entities.Enemy;

/**
 * @author sam
 *
 */
public class RandomSpawner extends Spawner {

	/** Pseudorandom generator for enemy position on screen edge */
	private Random rand;
    
    /** Observer pattern spawner class  */
	public RandomSpawner() {
		rand = new Random();
	}
	
	
	/* (non-Javadoc)
	 * @see Engine.AI.Spawner#makeEnemy()
	 */
	@Override
	public Enemy[] makeEnemies() {
	
		Enemy e = new Enemy();
		
		// Setup start position
		int side = rand.nextInt(4);
		
		if(side == 0) {				// From top
			e.setPosition( new Vector3f(rand.nextInt(FIELD_WIDTH - (Z_OFFSET*2)) + Z_OFFSET, 0, -FIELD_HEIGHT -Z_OFFSET) );
			e.setDirection( (Vector3f) cloneVec3(Entity.DOWN).scale(Globals.ENEMY_SPEED) );
		}
		else if (side == 1) { 		// From bottom
			e.setPosition( new Vector3f(rand.nextInt(FIELD_WIDTH - (Z_OFFSET*2)) + Z_OFFSET, 0, +(Z_OFFSET)) );
			e.setDirection( (Vector3f) cloneVec3(Entity.UP).scale(Globals.ENEMY_SPEED) );
		}
		else if (side == 2) {		// From left
			e.setPosition( new Vector3f(-X_OFFSET, 0, -rand.nextInt(FIELD_HEIGHT - X_OFFSET/2) - X_OFFSET/2) );
			e.setDirection( (Vector3f) cloneVec3(Entity.RIGHT).scale(Globals.ENEMY_SPEED) );
		}
		else {						// From right
			e.setPosition( new Vector3f(FIELD_WIDTH + X_OFFSET, 0, -rand.nextInt(FIELD_HEIGHT - X_OFFSET/2) - X_OFFSET/2) );
			e.setDirection( (Vector3f) cloneVec3(Entity.LEFT).scale(Globals.ENEMY_SPEED) );
		}
		
//		System.out.println("New " + e);
		
		return new Enemy[]{e};
	};
}
