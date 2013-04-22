/**
 * 
 */
package csc3202.Engine.AI;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import csc3202.Entities.*;

/**
 * @author Sam
 *
 */
public class EnemySpawner {

	private volatile ArrayList<Enemy> spawnList;
	
	private Spawner spawner;
	private SpawnListener listener;
	
	/**
	 * Observer pattern spawn class
	 */
	private class SpawnListener implements Observer {
		@Override
		public void update(Observable o, Object arg) {
			if(arg instanceof Enemy[]) {
				for (Enemy e : (Enemy[]) arg) {
					pushEnemy(e);
				}
			} else {
				System.err.println("UNKNOWN CLASS TYPE IN ENEMYSPAWNER");
			}
		}
	}
	
	
	/**
	 * Instantiate a spawner and all of its architecture
	 */
	public EnemySpawner() {
		spawnList = new ArrayList<Enemy>();
	}
	
	private synchronized void pushEnemy(Enemy e) {
		spawnList.add(e);
	}
	
	
	/**
	 * Insert spawned enemies into gameplay
	 * @param enemies
	 * @return
	 */
	public synchronized ArrayList<Enemy> populate(ArrayList<Enemy> enemies) {
		
		// Create a new enemy for each in the list
		for(Enemy e : spawnList) {
			enemies.add(e);
		}
		
		spawnList.clear();
		
		return enemies;
	}
	
	
	/**
	 * Muhahahahaha!
	 */
	public void startSpawning(Spawner spawner) {
		
		if(this.spawner != null)
			spawner.stop();			// Stop any currently running threads which could get orphaned
		
		this.spawner = spawner;
		listener = new SpawnListener();
		spawner.addObserver(listener);
		Thread thread = new Thread(spawner);
		thread.start();
	}
	
	
	/**
	 * Aww.
	 */
	public void stopSpawning() {
		if(this.spawner != null)
			spawner.stop();
		spawner = null;
	}
	
}
