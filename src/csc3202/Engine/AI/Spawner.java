package csc3202.Engine.AI;

import java.util.Observable;

/**
 * Write a runnable spawner with this interface and
 * pass the implementation to SpawnController to use it
 * 
 * @author sam
 * @version Apr 13
 */
public abstract class Spawner extends Observable implements Runnable {
	
    protected volatile boolean running = true;

	/* 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public abstract void run();

	/**
	 * Stop spawning enemies
	 */
	public void stop() {
		running = false;
	}
}