/**
 * 
 */
package csc3202.Engine.AI;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import csc3202.Engine.Interfaces.Entity;

/**
 * @author Sam
 *
 */
public class SpawnControl<T extends Entity> {

	private volatile ArrayList<Entity> spawnList;
	
	private Spawner spawner;
	private SpawnListener listener;
	private Thread thread;
	
	/**
	 * Instantiate a spawner and all of its architecture
	 */
	public SpawnControl() {
		spawnList = new ArrayList<Entity>();
	}
	
	private synchronized void pushEntity(Entity e) {
		spawnList.add(e);
	}
	
	
	/**
	 * Insert spawned enemies into gameplay
	 * 
	 * Unchecked cast should be fine as T extends Entity
	 * @param entityList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized ArrayList<T> populate(ArrayList<T> entityList) {
		
		// Create a new enemy for each in the list
		for(Entity e : spawnList) {
			entityList.add((T) e);
		}
		
		spawnList.clear();
		
		return entityList;
	}
	
	
	/**
	 * Muhahahahaha!
	 */
	public void start(Spawner spawner) {
		
		if(this.spawner != null)
			spawner.stop();			// Stop any currently running threads which could get orphaned
		
		this.spawner = spawner;
		listener = new SpawnListener();
		spawner.addObserver(listener);
		thread = new Thread(spawner);
		thread.start();
	}
	
	
	/**
	 * Aww.
	 */
	public void stop() {
		if(this.spawner != null) {
			spawner.stop();
			thread.interrupt();
		}
		spawner = null;
	}
	
	
	
	
	/**************************************************************************
	 * Observer pattern spawn class
	 */
	private class SpawnListener implements Observer {
		@Override
		public void update(Observable o, Object arg) {
			if(arg instanceof Entity[]) {
				for (Entity e : (Entity[]) arg) {
					pushEntity((Entity) e);
				}
			} else {
				System.err.println("Unknown class in Spawner. Something is very wrong.");
			}
		}
	}
	
}
