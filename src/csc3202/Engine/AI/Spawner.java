package csc3202.Engine.AI;
/**
 * Write a runnable enemy spawner with this interface
 */

import java.util.Observable;

import csc3202.Entities.Enemy;


public abstract class Spawner extends Observable implements Runnable {
	
    private volatile boolean running = true;

	/* 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("Spawner thread started");
		
		while (running) {
			try {
				// Tell observers that there are changes
				setChanged();
				notifyObservers(makeEnemies());		// Don't mind me, just making enemies...
				
				Thread.sleep(1000);					// In a music-based implementation, replace this with onset detection?
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Stop spawning enemies
	 */
	public void stop() {
		running = false;
	}
	
	/**
	 * Spawn an enemy or two.
	 * Override this method in a class and define custom behaviour
	 */
	public abstract Enemy[] makeEnemies();

}