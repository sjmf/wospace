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
	
    private volatile boolean running = true;
    
    /** Observer pattern spawner class  */
	public RandomSpawner() {
		rand = new Random();
	}

	/* 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println(">> Spawner thread started");
		
		while (running) {
			try {
				// Tell observers that there are changes
				setChanged();
				notifyObservers(makeEnemies());		// Don't mind me, just making enemies...
				
				Thread.sleep(Globals.spawn_rate);	// In a music-based game, this is set by beats
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		System.out.println("<< Spawner thread exit");
	}
	
	
	/* (non-Javadoc)
	 * @see Engine.AI.Spawner#makeEnemy()
	 */
	@Override
	public Enemy[] makeEnemies() {
	
		Enemy e = new Enemy();
		
		e.setScore(10);
		
		// Setup start position
		int side = rand.nextInt(4);
		
		// Set position on start side
		switch(side) {
		case 0:			// From top
			e.setDirection((Vector3f)  Vector3f.add(
					cloneVec3(Entity.DOWN),
					new Vector3f((rand.nextFloat()-0.5f), 0, 0),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition(new Vector3f(
					rand.nextInt(FIELD_WIDTH - (Z_OFFSET * 2))
					+ Z_OFFSET, 0, -FIELD_HEIGHT - Z_OFFSET));
			break;
		case 1:		// From bottom
			e.setDirection( (Vector3f) Vector3f.add(
					cloneVec3(Entity.UP),
					new Vector3f((rand.nextFloat()-0.5f), 0, 0),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition(new Vector3f(
					rand.nextInt(FIELD_WIDTH - (Z_OFFSET*2)) 
					+ Z_OFFSET, 0, +(Z_OFFSET)) );
			break;
		case 2:		// From left
			e.setDirection( (Vector3f) Vector3f.add(
					cloneVec3(Entity.RIGHT),
					new Vector3f(0, 0, (rand.nextFloat()-0.5f)),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition( new Vector3f(
					-X_OFFSET, 0, 
					-rand.nextInt(FIELD_HEIGHT - X_OFFSET/2) - X_OFFSET/2) );
			break;
		case 3:					// From right
			e.setDirection( (Vector3f) Vector3f.add(
					cloneVec3(Entity.LEFT),
					new Vector3f(0, 0, (rand.nextFloat()-0.5f)),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition( new Vector3f(
					FIELD_WIDTH + X_OFFSET, 0, 
					-rand.nextInt(FIELD_HEIGHT - X_OFFSET/2) - X_OFFSET/2) );
			break;
		default:
			break;
		}
		
		e.setTumble( rand.nextFloat()-0.5f);
		
//		System.out.println("New " + e);
		
		return new Enemy[]{e};
	}

	@Override
	public void stop() {
		running = false;
	};
}
