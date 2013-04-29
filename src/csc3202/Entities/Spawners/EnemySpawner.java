/**
 * 
 */
package csc3202.Entities.Spawners;

import static csc3202.Engine.Globals.*;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Globals;
import csc3202.Engine.Utils;
import csc3202.Engine.AI.Spawner;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Entities.BlasterEnemy;
import csc3202.Entities.Enemy;
import csc3202.Entities.TrackingEnemy;

/**
 * @author sam
 *
 */
public class EnemySpawner extends Spawner {

	/** Pseudorandom generator for enemy position on screen edge */
	private Random rand;
	
	private byte wave = 0;
	private long wave_start;
    
    /** Observer pattern spawner class  */
	public EnemySpawner() {
		rand = new Random();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println(">> Enemy Spawner thread started");
		
		wave_start = System.currentTimeMillis();
		
		long now;
		while (running) {
			try {
				Enemy[] enemies = makeEnemies();

				// Check and increment wave after wave length
				now = System.currentTimeMillis();
				if(now > wave_start + WAVE_LENGTH) {
					wave++;
					wave_start = now;
				}
				
//				System.out.println("Wave:" + wave + "\t" + wave_start + "\t" + now + "\t" + (now - wave_start));

				setChanged();													// Tell observers that there are changes
				notifyObservers(enemies);										// Don't mind me, just making enemies...
				
				Thread.sleep(Globals.spawn_rate);
			} 
			catch (InterruptedException e) {
				System.out.println("++ Spawner " + e.getMessage());
			}
		}
		
		System.out.println("<< Enemy Spawner thread exit");
	}
	
	
	/**
	 * Generate enemies based on state machine
	 * @return
	 */
	private Enemy[] makeEnemies() {
		
		Enemy e = null;
		
		switch(wave) {						// State machine stuff for waves
		case 0:
			e = new Enemy();
			e.setScore(10);
			break;
		case 1:
			if(rand.nextInt(2) == 0) {		// Chance of either tracker or regular
				e = new Enemy();
				e.setScore(10);
			} else {
				e = new TrackingEnemy();
				e.setScore(20);
			}
			break;
		case 2:
			e = new TrackingEnemy();		// Trackers only
			e.setScore(20);
			break;
		case 3:
			e = new BlasterEnemy();			// Bomber
			e.setScore(30);
			wave = 0;
			break;
		default:
			System.err.println("Spawner Thread Wave Error");
			break;
		}
		
		
		// Set start position
		int side = rand.nextInt(4);
		
		// Set position on start side
		switch(side) {
		case 0:			// From top
			e.setDirection((Vector3f)  Vector3f.add(
					Utils.cloneVec3(Entity.DOWN),
					new Vector3f((rand.nextFloat()-0.5f), 0, 0),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition(new Vector3f(
					rand.nextInt(FIELD_WIDTH - (Z_OFFSET * 2))
					+ Z_OFFSET, 0, -FIELD_HEIGHT - Z_OFFSET));
			break;
		case 1:		// From bottom
			e.setDirection( (Vector3f) Vector3f.add(
					Utils.cloneVec3(Entity.UP),
					new Vector3f((rand.nextFloat()-0.5f), 0, 0),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition(new Vector3f(
					rand.nextInt(FIELD_WIDTH - (Z_OFFSET*2)) 
					+ Z_OFFSET, 0, +(Z_OFFSET)) );
			break;
		case 2:		// From left
			e.setDirection( (Vector3f) Vector3f.add(
					Utils.cloneVec3(Entity.RIGHT),
					new Vector3f(0, 0, (rand.nextFloat()-0.5f)),
					null).scale(Globals.ENEMY_SPEED));
			
			e.setPosition( new Vector3f(
					-X_OFFSET, 0, 
					-rand.nextInt(FIELD_HEIGHT - X_OFFSET/2) - X_OFFSET/2) );
			break;
		case 3:		// From right
			e.setDirection( (Vector3f) Vector3f.add(
					Utils.cloneVec3(Entity.LEFT),
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
}
