/**
 * 
 */
package csc3202.Spawners;

import static csc3202.Engine.Globals.FIELD_HEIGHT;
import static csc3202.Engine.Globals.FIELD_WIDTH;
import static csc3202.Engine.Globals.Z_OFFSET;

import java.util.Random;

import org.lwjgl.util.vector.Vector3f;

import csc3202.Engine.Globals;
import csc3202.Engine.Utils;
import csc3202.Engine.AI.Spawner;
import csc3202.Engine.Interfaces.Entity;
import csc3202.Entities.Powerup;
import csc3202.Entities.Powerup.PType;

/**
 * @author sam
 *
 */
public class PowerupSpawner extends Spawner {

	private Random rand;
	
	/** Observer pattern runnable spawner */
	public PowerupSpawner() {
		rand = new Random();
	}

	@Override
	public void run() {
		System.out.println(">> Powerup Spawner thread started");
		
		while (running) {
			try {
				// Tell observers that there are changes
				setChanged();
				notifyObservers(makePowerups());
				
				Thread.sleep(Globals.POWERUP_DURATION);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
		System.out.println("<< Powerup Spawner thread exit");
	}

	/**
	 * Make some powerups!
	 * @return
	 */
	private Powerup[] makePowerups() {
		
		PType type = (rand.nextInt(2) == 0) ? PType.BOMB : PType.FIRERATE;
		
		Powerup p = new Powerup(type);
		
		// Powerups always fall from top
		p.setDirection((Vector3f) 
				Utils.cloneVec3(Entity.DOWN)
				.scale(Globals.ENEMY_SPEED)
			);
		
		p.setPosition(new Vector3f(
				rand.nextInt(FIELD_WIDTH - (Z_OFFSET * 2))
				+ Z_OFFSET, 0, -FIELD_HEIGHT - Z_OFFSET));
		
		return new Powerup[]{p};
	}
}
